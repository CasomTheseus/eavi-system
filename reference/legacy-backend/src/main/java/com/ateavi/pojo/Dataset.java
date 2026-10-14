package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;
import lombok.Data;

/**
 * @TableName dataset
 */
@TableName(value ="dataset")
@Data
public class Dataset {
    @TableId(value = "dataset_id", type = IdType.AUTO)
    private Integer datasetId;

    private String datasetName;

    private String description;

    private String trueAddress;

    private Integer statusId;

    private String sizeFormatted;

    private Integer fileCount;

    private Integer userId;

    private Integer downloadcount;

    private Date createdTime;

    private Date updatedTime;

    private String baseAddress;
    @Version
    private Integer version;
    @TableLogic
    private Integer isDeleted;
}
