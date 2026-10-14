package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName paper_task
 */
@TableName(value ="paper_task")
@Data
public class PaperTask {
    private Long paperId;

    private Long taskId;
}