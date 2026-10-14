package com.ateavi.pojo.vo;

import com.ateavi.pojo.Role;
import lombok.Data;

/**
 * ClassName:UserVo
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/8 13:27
 * @Version 1.0
 */
@Data
public class UserVo {
    private int id;
    private String username;
    private String email;
    private String name;
    private String role;
    private String department;
    private String avatar;
    private String lastLoginTime;

    public UserVo() {
    }

    public UserVo(int id, String username, String email, String name, String role, String department, String avatar, String lastLoginTime) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
        this.role = role;
        this.department = department;
        this.avatar = avatar;
        this.lastLoginTime = lastLoginTime;
    }
}
