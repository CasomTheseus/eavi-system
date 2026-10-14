package com.ateavi.service.impl;

import com.ateavi.pojo.DTO.DatasetFilesScanResult;
import com.ateavi.service.DatasetFilesScanService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ResourceUtils;

/**
 * ClassName:DatasetFilesScanServiceImpl
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/31 16:02
 * @Version 1.0
 */
@Service
public class DatasetFilesScanServiceImpl implements DatasetFilesScanService {
    private static final Set<String> MARKDOWN_EXTENSIONS = Set.of(".md", ".markdown", ".mdown", ".mkd");

    // 支持的图片文件扩展名
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            ".png", ".jpg", ".jpeg", ".gif", ".svg", ".bmp", ".webp", ".ico"
    );
    @Value("${eavi.dataset.storage-path:e:/UI/eavi-static/data/datasets/}")
    private String storageRootPath;

    public DatasetFilesScanResult scanDatasetFiles(String relativePath){
        // 构建完整路径，包含错误处理
        String fullPath;
        try {
            fullPath = buildFullPath(relativePath);
        } catch (Exception e) {
            throw new RuntimeException("构建数据集路径失败: " + relativePath, e);
        }

        // 创建Path对象，包含错误处理
        Path baseDir;
        try {
            baseDir = Paths.get(fullPath);
        } catch (Exception e) {
            throw new RuntimeException("无效的路径格式: " + fullPath, e);
        }

        // 检查目录存在性和类型
        if (!Files.exists(baseDir)) {
            throw new IllegalArgumentException("数据集目录不存在: " + fullPath);
        }
        if (!Files.isDirectory(baseDir)) {
            throw new IllegalArgumentException("指定路径不是目录: " + fullPath);
        }

        List<String> markdownFiles = new ArrayList<>();
        List<String> imageFiles = new ArrayList<>();

        try {
            Files.walkFileTree(baseDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        String fileName = file.getFileName().toString().toLowerCase();
                        String relativePath = baseDir.relativize(file).toString();

                        // 检查是否为Markdown文件
                        if (MARKDOWN_EXTENSIONS.stream().anyMatch(fileName::endsWith)) {
                            markdownFiles.add(file.toString());
                        }

                        // 检查是否为图片文件
                        if (IMAGE_EXTENSIONS.stream().anyMatch(fileName::endsWith)) {
                            imageFiles.add(file.toString());
                        }

                        return FileVisitResult.CONTINUE;
                    } catch (Exception e) {
                        // 记录单个文件处理失败，但继续处理其他文件
                        System.err.println("处理文件失败: " + file + ", 错误: " + e.getMessage());
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    // 记录访问失败的文件，但继续处理
                    System.err.println("访问文件失败: " + file + ", 错误: " + exc.getMessage());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("扫描数据集文件失败: " + fullPath, e);
        }

        // 选择主要的Markdown文件（优先选择README.md或与数据集名称相同的文件）
        String primaryMarkdownFile = selectPrimaryMarkdownFile(markdownFiles, fullPath);

        return new DatasetFilesScanResult(primaryMarkdownFile, imageFiles, fullPath);
    }

    private String selectPrimaryMarkdownFile(List<String> markdownFiles, String basePath) {
        if (markdownFiles.isEmpty()) {
            return null;
        }

        if (markdownFiles.size() == 1) {
            return markdownFiles.get(0);
        }

        // 优先级排序：README.md > 与目录名相同的.md > 第一个.md文件
        String dirName = Paths.get(basePath).getFileName().toString().toLowerCase();

        // 查找README.md
        Optional<String> readmeFile = markdownFiles.stream()
                .filter(file -> file.toLowerCase().contains("readme"))
                .findFirst();

        if (readmeFile.isPresent()) {
            return readmeFile.get();
        }

        // 查找与目录名相同的文件
        Optional<String> namedFile = markdownFiles.stream()
                .filter(file -> {
                    String fileName = Paths.get(file).getFileName().toString().toLowerCase();
                    return fileName.startsWith(dirName);
                })
                .findFirst();

        if (namedFile.isPresent()) {
            return namedFile.get();
        }

        // 返回第一个文件
        return markdownFiles.get(0);
    }
    private String buildFullPath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new IllegalArgumentException("相对路径不能为空");
        }

        try {
            String rootPath = storageRootPath;

            // 处理classpath路径
            if (rootPath.startsWith("classpath:")) {
                return buildClasspathPath(rootPath, relativePath);
            }
            // 处理相对路径（相对于项目根目录）
            else if (rootPath.startsWith("./") || rootPath.startsWith(".\\")) {
                return buildRelativePath(rootPath, relativePath);
            }
            // 处理绝对路径
            else if (Paths.get(rootPath).isAbsolute()) {
                return buildAbsolutePath(rootPath, relativePath);
            }
            // 默认作为相对路径处理
            else {
                return buildRelativePath("./" + rootPath, relativePath);
            }

        } catch (Exception e) {
            throw new RuntimeException("构建文件路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建classpath路径
     */
    private String buildClasspathPath(String rootPath, String relativePath) {
        try {
            String classpathLocation = rootPath.substring("classpath:".length());
            
            // 尝试使用ResourceUtils获取文件
            try {
                File file = ResourceUtils.getFile(rootPath);
                Path basePath = file.toPath();
                Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
                return fullPath.normalize().toString();
            } catch (Exception e) {
                // 如果ResourceUtils失败，尝试使用ClassLoader
                URL resource = getClass().getClassLoader().getResource(classpathLocation);
                if (resource != null) {
                    Path basePath = Paths.get(resource.toURI());
                    Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
                    return fullPath.normalize().toString();
                }
                throw new RuntimeException("无法找到classpath资源: " + rootPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("处理classpath路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建相对路径（相对于项目根目录）
     */
    private String buildRelativePath(String rootPath, String relativePath) {
        try {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");
            
            // 移除rootPath中的"./"或".\\"前缀
            String cleanRootPath = rootPath;
            if (cleanRootPath.startsWith("./")) {
                cleanRootPath = cleanRootPath.substring(2);
            } else if (cleanRootPath.startsWith(".\\")) {
                cleanRootPath = cleanRootPath.substring(2);
            }
            
            // 构建完整路径
            Path basePath = Paths.get(projectRoot, cleanRootPath);
            Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
            return fullPath.normalize().toString();
        } catch (Exception e) {
            throw new RuntimeException("处理相对路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建绝对路径
     */
    private String buildAbsolutePath(String rootPath, String relativePath) {
        try {
            Path basePath = Paths.get(rootPath);
            Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
            return fullPath.normalize().toString();
        } catch (Exception e) {
            throw new RuntimeException("处理绝对路径失败: " + e.getMessage(), e);
        }
    }

    public String getRelativePath(String filePath, String basePath) {
        System.out.println("getRelativePath: 开始计算相对路径");
        System.out.println("getRelativePath: filePath = " + filePath);
        System.out.println("getRelativePath: basePath = " + basePath);
        
        // 输入验证
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("文件路径不能为空");
        }
        if (basePath == null || basePath.trim().isEmpty()) {
            throw new IllegalArgumentException("基础路径不能为空");
        }

        try {
            // 标准化路径分隔符
            String normalizedFilePath = filePath.replace("\\", "/");
            String normalizedBasePath = basePath.replace("\\", "/");
            
            System.out.println("getRelativePath: normalizedFilePath = " + normalizedFilePath);
            System.out.println("getRelativePath: normalizedBasePath = " + normalizedBasePath);

            Path base = Paths.get(normalizedBasePath).normalize();
            Path file = Paths.get(normalizedFilePath).normalize();
            
            System.out.println("getRelativePath: base (normalized) = " + base);
            System.out.println("getRelativePath: file (normalized) = " + file);

            // 检查文件路径是否在基础路径下
            if (!file.startsWith(base)) {
                // 如果不在基础路径下，尝试获取文件名作为降级处理
                String fileName = file.getFileName() != null ? file.getFileName().toString() : "unknown_file";
                System.err.println("getRelativePath: 警告: 文件路径不在基础路径下，返回文件名: " + fileName);
                System.err.println("getRelativePath: file.startsWith(base) = false");
                return fileName;
            }

            // 计算相对路径
            Path relativePath = base.relativize(file);
            String result = relativePath.toString().replace("\\", "/");
            
            System.out.println("getRelativePath: 计算得到的相对路径: " + relativePath);
            System.out.println("getRelativePath: 最终结果: " + result);
            
            return result;

        } catch (Exception e) {
            System.err.println("getRelativePath: 计算相对路径时发生异常: " + e.getMessage());
            e.printStackTrace();
            
            // 降级处理：如果所有方法都失败，尝试返回文件名
            try {
                Path file = Paths.get(filePath);
                String fileName = file.getFileName() != null ? file.getFileName().toString() : "unknown_file";
                System.err.println("计算相对路径失败，返回文件名: " + fileName + ", 错误: " + e.getMessage());
                return fileName;
            } catch (Exception fallbackException) {
                System.err.println("获取文件名也失败，返回默认值, 错误: " + fallbackException.getMessage());
                return "unknown_file";
            }
        }
    }

}
