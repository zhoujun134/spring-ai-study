package com.zj.ai.mcp.server.webflux.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import com.zj.ai.mcp.sdk.gson.GsonUtils;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FileSystemService {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    @Tool(description = "列出指定目录(path)下的所有文件和子目录")
    public String listDirectory(@ToolParam(description = "目录路径(path)") String path) throws JsonProcessingException {
        try {
            File directory = new File(path);
            if (!directory.isDirectory()) {
                return objectMapper.writeValueAsString("Error: Path is not a directory");
            }
            String[] files = directory.list();
            List<String> pathList = new ArrayList<>();
            try {
                Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        pathList.add(file.toString());
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        System.err.println("Failed to access: " + file);
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            log.info("Listing directory: {}, 结果为: {}", path, GsonUtils.toJSONString(pathList));
            return objectMapper.writeValueAsString(pathList);
        } catch (Exception e) {
            return objectMapper.writeValueAsString("Error: " + e.getMessage());
        }
    }
    @Tool(description = "读取指定文件(path)的内容")
    public String readFile(@ToolParam(description = "待获取的文件(path)") String path) throws JsonProcessingException {
        try {
            String content = new String(Files.readAllBytes(Paths.get(path)));
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            return objectMapper.writeValueAsString("Error: " + e.getMessage());
        }
    }

    @Tool(description = "写入指定文件(path)的内容")
    public String writeFile(@ToolParam(description = "写入文件的全路径(path)") String path, @ToolParam(description = "写入文件的内容") String content) throws JsonProcessingException {
        try {
            log.info("写入文件: {}, 内容为: {}", path, content);
            Files.write(Paths.get(path), content.getBytes());
            return objectMapper.writeValueAsString("Success");
        } catch (Exception e) {
            return objectMapper.writeValueAsString("Error: " + e.getMessage());
        }
    }

    @Tool(description = "创建指定目录(path)")
    public String createDirectory(@ToolParam(description = "待创建的目录路径(path)") String path) throws JsonProcessingException {
        try {
            Files.createDirectories(Paths.get(path));
            return objectMapper.writeValueAsString("Success");
        } catch (Exception e) {
            return objectMapper.writeValueAsString("Error: " + e.getMessage());
        }
    }

    @Tool(description = "删除指定文件(path)")
    public String deleteFile(@ToolParam(description = "待删除的文件全路径(path)") String path) throws JsonProcessingException {
        try {
            Files.delete(Paths.get(path));
            return objectMapper.writeValueAsString("Success");
        } catch (Exception e) {
            return objectMapper.writeValueAsString("Error: " + e.getMessage());
        }
    }
}
