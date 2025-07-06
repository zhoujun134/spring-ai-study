package com.zj.ai.mcp.server.webflux.config;

import com.zj.ai.mcp.server.webflux.service.ComputeService;
import com.zj.ai.mcp.server.webflux.service.FileSystemService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolConfig {
    @Bean
    public ToolCallbackProvider weatherTools(ComputeService computeService,
                                             FileSystemService fileSystemService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(computeService, fileSystemService)
                .build();
    }
}
