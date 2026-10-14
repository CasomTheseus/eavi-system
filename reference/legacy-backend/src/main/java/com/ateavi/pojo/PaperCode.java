package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName paper_code
 */
@TableName(value ="paper_code")
@Data
public class PaperCode {
    private Integer paperId;

    private String codeUrl;

    private String codeName;
}