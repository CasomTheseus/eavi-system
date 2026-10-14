package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName folder_paper
 */
@TableName(value ="folder_paper")
@Data
public class FolderPaper {
    private Integer folderId;

    private Long paperId;
}