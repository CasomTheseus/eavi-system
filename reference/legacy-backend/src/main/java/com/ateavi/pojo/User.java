package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User {
    @TableId
    private Integer userId;

    private String username;

    private String email;

    private String name;

    private Integer roleId;

    private String avatar;

    private String password;
    @Version
    private Integer version;
    @TableLogic
    private Integer isDeleted;

    private static final long serialVersionUID = 1L;
}