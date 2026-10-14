package com.ateavi.service.impl;

import com.ateavi.service.DatasetFilesScanService;
import com.ateavi.service.MarkdownProcessingService;
import com.ateavi.utils.JwtHelper;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName:MarkdownProcessingServiceImpl
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/31 16:10
 * @Version 1.0
 */@Service
public class MarkdownProcessingServiceImpl implements MarkdownProcessingService {
    private final Parser parser;
    private final HtmlRenderer renderer;

    @Autowired
    private DatasetFilesScanService filesScanService;

    // 图片标签正则表达式
    private static final Pattern IMG_PATTERN = Pattern.compile(
            "!\\[([^\\]]*)\\]\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE
    );

    public MarkdownProcessingServiceImpl() {
        this.parser = Parser.builder().build();
        this.renderer = HtmlRenderer.builder().build();
    }

    /**
     * 处理Markdown文件，转换为HTML并处理图片路径
     * @param markdownFilePath Markdown文件路径
     * @param imageFiles 图片文件列表
     * @param datasetId 数据集ID
     * @param basePath 基地址
     * @param includeImages 是否处理图片路径
     * @return 处理后的HTML内容
     */
    public String processMarkdownToHtml(String markdownFilePath, List<String> imageFiles,
                                        Integer datasetId, String basePath, boolean includeImages) {
        try {

            // 读取Markdown文件内容
            Path path = Paths.get(markdownFilePath);
            String markdownContent = Files.readString(path);

            // 处理图片路径
            if (includeImages) {
                markdownContent = processImagePaths(markdownContent, imageFiles, datasetId, basePath);
            }

            // 转换为HTML
            Node document = parser.parse(markdownContent);
            String htmlContent = renderer.render(document);

            // 添加代码高亮样式
            htmlContent = addCodeHighlighting(htmlContent);

            return htmlContent;

        } catch (IOException e) {
            throw new RuntimeException("读取Markdown文件失败", e);
        }
    }

    /**
     * 处理Markdown中的图片路径
     * @param markdownContent Markdown内容
     * @param imageFiles 图片文件列表
     * @param datasetId 数据集ID
     * @param basePath 基地址
     * @return 处理后的Markdown内容
     */
    private String processImagePaths(String markdownContent, List<String> imageFiles,
                                     Integer datasetId, String basePath) {
        
        if (markdownContent == null || markdownContent.trim().isEmpty() || 
            imageFiles == null || imageFiles.isEmpty()) {
            return markdownContent;
        }

        Matcher matcher = IMG_PATTERN.matcher(markdownContent);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String altText = matcher.group(1);
            String originalPath = matcher.group(2);

            // 查找匹配的图片文件
            String matchedImageFile = findMatchingImageFile(originalPath, imageFiles, basePath);

            if (matchedImageFile != null) {
                // 获取相对路径
                String relativePath = filesScanService.getRelativePath(matchedImageFile, basePath);
                
                // 标准化相对路径（确保使用正斜杠）
                relativePath = relativePath.replace("\\", "/");
                
                // 如果相对路径已经以 "images/" 开头，则直接使用；否则添加 "images/" 前缀
                String imagePath;
                if (relativePath.startsWith("images/")) {
                    // 去掉 "images/" 前缀，因为API路径会自动添加
                    imagePath = relativePath.substring(7); // 移除 "images/" (7个字符)
                } else {
                    // 如果不是以 "images/" 开头，直接使用相对路径
                    imagePath = relativePath;
                }
                
                // 生成API访问路径（符合API接口文档规范：http://localhost:8080/datasets/{id}/images/{imagePath}）
                String apiPath = String.format("http://localhost:8080/datasets/%d/images/%s", datasetId, imagePath);
                String replacement = String.format("![%s](%s)", altText, apiPath);
                
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {
                // 没有找到匹配的图片文件，保持原路径
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 查找匹配的图片文件
     * @param originalPath 原始路径
     * @param imageFiles 图片文件列表
     * @param basePath 基地址
     * @return 匹配的图片文件路径
     */
    private String findMatchingImageFile(String originalPath, List<String> imageFiles, String basePath) {
        if (originalPath == null || originalPath.trim().isEmpty() || imageFiles == null || imageFiles.isEmpty()) {
            return null;
        }

        // 提取文件名
        String fileName = extractFileName(originalPath);
        if (fileName == null) {
            return null;
        }

        // 在图片文件列表中查找匹配的文件（按文件名匹配）
        return imageFiles.stream()
                .filter(imageFile -> {
                    String imageFileName = extractFileName(imageFile);
                    return imageFileName != null && imageFileName.equalsIgnoreCase(fileName);
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * 从路径中提取文件名
     * @param filePath 文件路径
     * @return 文件名
     */
    private String extractFileName(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        
        try {
            return Paths.get(filePath).getFileName().toString();
        } catch (Exception e) {
            // 如果路径解析失败，使用字符串操作提取文件名
            String normalizedPath = filePath.replace("\\", "/");
            int lastSlash = normalizedPath.lastIndexOf('/');
            return lastSlash >= 0 && lastSlash < normalizedPath.length() - 1 
                ? normalizedPath.substring(lastSlash + 1) 
                : normalizedPath;
        }
    }

    /**
     * 添加代码高亮样式
     * @param htmlContent HTML内容
     * @return 添加样式后的HTML内容
     */
    private String addCodeHighlighting(String htmlContent) {
        // 为代码块添加复制按钮和样式类
        htmlContent = htmlContent.replaceAll(
                "<pre><code>",
                "<pre class=\"code-block\"><code>"
        );

        htmlContent = htmlContent.replaceAll(
                "</code></pre>",
                "</code><button class=\"copy-btn\" onclick=\"copyCodeBlock(this.previousElementSibling.textContent)\">📋 复制</button></pre>"
        );

        return htmlContent;
    }

    /**
     * 提取HTML中的图片URL列表
     * @param htmlContent HTML内容
     * @return 图片URL列表
     */
    public List<String> extractImageUrls(String htmlContent) {
        List<String> imageUrls = new ArrayList<>();
        
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return imageUrls;
        }
        
        Pattern htmlImgPattern = Pattern.compile(
            "<img[^>]*\\s+src\\s*=\\s*[\"']([^\"']+)[\"'][^>]*>", 
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = htmlImgPattern.matcher(htmlContent);

        while (matcher.find()) {
            String src = matcher.group(1);
            // 标准化路径并添加到结果中
            String normalizedSrc = src.replace("\\", "/");
            imageUrls.add(normalizedSrc);
        }

        return imageUrls;
    }

    /**
     * 从Markdown内容中提取标题
     * @param markdownContent Markdown内容
     * @return 标题
     */
    public String extractTitle(String markdownContent) {
        // 查找第一个一级标题
        Pattern titlePattern = Pattern.compile("^#\\s+(.+)$", Pattern.MULTILINE);
        Matcher matcher = titlePattern.matcher(markdownContent);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // 如果没有找到一级标题，查找二级标题
        Pattern h2Pattern = Pattern.compile("^##\\s+(.+)$", Pattern.MULTILINE);
        Matcher h2Matcher = h2Pattern.matcher(markdownContent);

        if (h2Matcher.find()) {
            return h2Matcher.group(1).trim();
        }

        return "数据集文档";
    }
}
