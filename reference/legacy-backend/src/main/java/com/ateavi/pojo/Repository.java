package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName repository
 */
@TableName(value ="repository")
@Data
public class Repository {
    private Long id;

    private Long paperId;

    private String name;

    private String url;

    private String description;

    private Date createdAt;
}