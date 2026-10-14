package com.ateavi.service;

import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName:MarkdownProcessingService
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/31 16:04
 * @Version 1.0
 */
public interface MarkdownProcessingService {
    String processMarkdownToHtml(String markdownFilePath, List<String> imageFiles,
                                 Integer datasetId, String basePath, boolean includeImages);
    List<String> extractImageUrls(String htmlContent);
    String extractTitle(String markdownContent);
}
