package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName dataset_task
 */
@TableName(value ="dataset_task")
@Data
public class DatasetTask {
    private Integer datasetId;

    private Integer taskId;
}