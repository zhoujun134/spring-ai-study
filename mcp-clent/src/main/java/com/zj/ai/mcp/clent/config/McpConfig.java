package com.zj.ai.mcp.clent.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.zj.ai.mcp.sdk.gson.GsonUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import cn.hutool.core.map.MapUtil;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * @author zhoujun134
 * Created on 2025-05-07
 */
@EnableScheduling
@Configuration
@EnableConfigurationProperties
@Slf4j
public class McpConfig {
    /**
     * MCP客户端的配置属性，用于存储MCP客户端的相关配置信息。
     * key : 服务名称-版本号-服务器地址
     */
    private final Map<String, McpSyncClient> mcpSyncClientMap = new HashMap<>();

    private final Map<String, List<ToolCallback>> toolCallbacksMap = new HashMap<>();

    @Autowired
    private McpSseClientProperties mcpSseClientProperties;

    /**
     * 创建或重建MCP客户端。该方法会关闭旧客户端实例（如果存在），并初始化新的MCP客户端及关联的工具回调。
     * <p>
     * 同步方法确保客户端创建过程的线程安全。
     */
    private synchronized void pingAndCheckMcpClient() {
        if (MapUtil.isNotEmpty(this.mcpSyncClientMap)) {
            final List<String> disableServers = new ArrayList<>();
            this.mcpSyncClientMap.forEach((serverUrl, client) -> {
                try {
                    // 检查与服务端的连接状态
                    client.ping();
                } catch (final Exception e) {
                    // 捕获连接异常，记录错误日志并重建客户端
                    log.error("<---------------->MCP ping error: {}", e.getLocalizedMessage());
                    boolean createResult = this.createClientByServerUrl(serverUrl);
                    if (!createResult) {
                        disableServers.add(serverUrl);
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(disableServers)) {
                disableServers.forEach(serverUrl -> {
                    if (MapUtil.isNotEmpty(this.mcpSyncClientMap)) {
                        this.mcpSyncClientMap.remove(serverUrl);
                    }
                    if (MapUtil.isNotEmpty(this.toolCallbacksMap)) {
                        this.toolCallbacksMap.remove(serverUrl);
                    }
                });
                log.info("<---------------->MCP server 状态检测完成, 当前存活{}个MCP客户端, urls={},"
                                + " 已失效 {} 个 MCP 客户端，失效 urls={}",
                        this.mcpSyncClientMap.size(), GsonUtils.toJSONString(this.mcpSyncClientMap.keySet()),
                        disableServers.size(), GsonUtils.toJSONString(disableServers));
            }
            // 找出 mcpSyncClientMap 中不存在，配置文件中存在的连接信息，并再次重建 MCP 客户端
            Map<String, SseParameters> connections = this.mcpSseClientProperties.getConnections();
            if (MapUtil.isEmpty(connections)) {
                log.warn("<---------------->MCP 没有配置连接信息，请检查配置文件");
                return;
            }
            connections.forEach((key, sseParameter) -> {
                String serverUrl = sseParameter.url();
                if (this.mcpSyncClientMap.containsKey(serverUrl) || disableServers.contains(serverUrl)) {
                    return;
                }
                // 不在 mcpSyncClientMap 和 disableServers 中，则重建 MCP 客户端
                this.createClientByServerUrl(serverUrl);
            });
        } else {
            this.initMcpClient();
        }
    }

    private boolean createClientByServerUrl(String serverUrl) {
        if (StringUtils.isBlank(serverUrl)) {
            log.warn("serverUrl is blank, please check the configuration file.");
            return false;
        }
        try {
            final HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(serverUrl).build();
            final McpSyncClient newClient = McpClient.sync(transport).build();
            // 初始化新客户端并记录结果
            final McpSchema.InitializeResult init = newClient.initialize();
            log.info("<----------------> sync MCP Initialized 完成: {}", init.serverInfo());
            final List<ToolCallback> curToolCallbacks =
                    List.of(new SyncMcpToolCallbackProvider(newClient).getToolCallbacks());
            this.toolCallbacksMap.put(serverUrl, curToolCallbacks);
            this.mcpSyncClientMap.put(serverUrl, newClient);
        } catch (Exception ex) {
            log.error("<----------------> createClientByServerUrl 链接 MCP 客户端失败: {}， serverUrl={}",
                    ex.getLocalizedMessage(), serverUrl);
            return false;
        }
        return true;
    }

    private synchronized void initMcpClient() {
        Map<String, SseParameters> connectionsMap = mcpSseClientProperties.getConnections();
        if (MapUtil.isEmpty(connectionsMap)) {
            log.warn("initMcpClient <---------------->MCP 没有配置连接信息，请检查配置文件");
            return;
        }
        connectionsMap.forEach((key, sseParameter) -> {
            String serverUrl = sseParameter.url();
            this.createClientByServerUrl(serverUrl);
        });
        log.info("initMcpClient <----------------> end tools:{}", GsonUtils.toJSONString(this.getToolCallback()));
    }

    /**
     * 获取工具回调列表。该方法会确保MCP客户端处于有效连接状态，若连接异常则自动重建客户端。
     *
     * @return 已配置的工具回调列表
     */
    public List<ToolCallback> getToolCallback() {
        if (MapUtil.isNotEmpty(this.toolCallbacksMap)) {
            return this.toolCallbacksMap.values().stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 每秒检查一次MCP客户端的状态，并重建异常的客户端。
     */
    @Scheduled(cron = "* * * * * ?")
    public void checkMcpClient() {
        this.pingAndCheckMcpClient();
    }

    /**
     * 在Bean初始化时创建MCP客户端。
     */
    @PostConstruct
    public void init() {
        log.info("<---------------->MCP Initializing...");
        // 在Bean初始化时强制执行
        initMcpClient();
        log.info("<---------------->MCP Initialized 完成, 共初始化{}个MCP客户端, urls={}",
                this.mcpSyncClientMap.size(), GsonUtils.toJSONString(this.mcpSyncClientMap.keySet()));
    }
}
