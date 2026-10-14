package com.ateavi.service;

import com.ateavi.pojo.DTO.DatasetFilesScanResult;
import org.springframework.stereotype.Service;

/**
 * ClassName:DatasetFilesScanService
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/31 15:59
 * @Version 1.0
 */

public interface DatasetFilesScanService {
    DatasetFilesScanResult scanDatasetFiles(String basePath);
    String getRelativePath(String filePath, String basePath);
}
