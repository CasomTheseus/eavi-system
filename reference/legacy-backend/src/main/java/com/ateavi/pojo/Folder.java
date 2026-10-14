package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName folder
 */
@TableName(value ="folder")
@Data
public class Folder {
    @TableId(value = "folder_id", type = IdType.AUTO)
    private Integer folderId;

    private String name;

    private Integer parentId;

    private Integer userId;

    private Date createdAt;

    private Date updatedAt;

    private Integer isDeleted;
}
