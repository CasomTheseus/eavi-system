package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName result
 */
@TableName(value ="result")
@Data
public class Result {
    private Long id;

    private Long paperId;

    private Long taskId;

    private Integer datasetId;

    private String modelName;

    private String metricName;

    private String metricValue;

    private Integer isSota;

    private Date createdAt;
}