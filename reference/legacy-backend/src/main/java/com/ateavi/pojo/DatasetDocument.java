package com.ateavi.pojo;


import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * ClassName:DatasetDocument
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/30 16:14
 * @Version 1.0
 */
@Data
public class DatasetDocument {
    private Integer datasetId;

    private String htmlContent;

    private String title;

    private List<String> imageUrls;

    private LocalDateTime lastModified;

    private Double fileSize;

    public DatasetDocument() {
    }

    public DatasetDocument(Integer datasetId, String htmlContent, String title, List<String> imageUrls, LocalDateTime lastModified, Double fileSize) {
        this.datasetId = datasetId;
        this.htmlContent = htmlContent;
        this.title = title;
        this.imageUrls = imageUrls;
        this.lastModified = lastModified;
        this.fileSize = fileSize;
    }
}
