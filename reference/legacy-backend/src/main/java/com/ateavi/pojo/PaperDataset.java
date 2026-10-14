package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName paper_dataset
 */
@TableName(value ="paper_dataset")
@Data
public class PaperDataset {
    private Long paperId;

    private Integer datasetId;

    private String datasetUrl;

    private String displayName;
}