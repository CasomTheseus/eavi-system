package com.ateavi.pojo.DTO;

import lombok.Data;

import java.util.List;

/**
 * ClassName:DatasetSaveResult
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/11/1 16:31
 * @Version 1.0
 */
@Data
public class DatasetSaveResult {

    private Integer datasetId;
    private String datasetName;
    private String baseAddress;  // 数据集基地址，用于读取数据集
    private String description;
    private String taskNames;
    private String status;
    private String createdTime;
    private String updatedTime;
    private Integer userId;
}
