package com.ateavi.pojo.DTO;

import lombok.Data;

import java.util.List;

/**
 * ClassName:DatasetFilesScanResult
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/31 15:47
 * @Version 1.0
 */
@Data
public class DatasetFilesScanResult {
    private String markdownFile;
    private List<String> imageFiles;
    private String basePath;

    public DatasetFilesScanResult(String markdownFile, List<String> imageFiles, String basePath) {
        this.markdownFile = markdownFile;
        this.imageFiles = imageFiles;
        this.basePath = basePath;
    }

    public DatasetFilesScanResult() {
    }
}
