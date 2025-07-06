package com.zj.ai.mcp.server.webflux.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ComputeService {

    @Tool(description = "计算两个数 a 和 b 的乘积")
    public Double multiply(@ToolParam(description = "参数 a") Double a, @ToolParam(description = "参数 b") Double b) {
        log.info("multiply a: {}, b: {}", a, b);
        return a * b;
    }
}
