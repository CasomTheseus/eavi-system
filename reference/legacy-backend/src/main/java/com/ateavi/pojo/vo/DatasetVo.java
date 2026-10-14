package com.ateavi.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * ClassName:DatasetVo
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/23 10:56
 * @Version 1.0
 */
@Data
public class DatasetVo {
    private Integer datasetId;

    private String datasetName;

    private String description;

    private String status;

    private Double size;

    private String sizeFormatted;

    private Integer fileCount;

    private String taskNames;

    private String trueAddress;

    private String name;

    private Integer downloadcount;

    private String baseAddress;

    private Date createdTime;

    private Date updatedTime;

}
