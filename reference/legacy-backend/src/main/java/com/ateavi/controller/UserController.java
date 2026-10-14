package com.ateavi.controller;

import com.ateavi.pojo.User;
import com.ateavi.service.UserService;
import com.ateavi.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.function.Predicate;

/**
 * ClassName:UserController
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/7 21:01
 * @Version 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("auth")

public class UserController {
    @Autowired
    private UserService userService;

    /**
     * **请求信息**:
     *
     * - **URL**: `/auth/login`
     * - **Method**: `POST`
     * - **Content-Type**: `application/json`
     *
     * **请求参数**:
     *
     * {
     *   "username": "string",  // 用户名或邮箱，必填
     *   "password": "string"   // 密码，必填
     * }
     *
     * **响应示例**:
     *{
     *   "code": 200,
     *   "message": "登录成功",
     *   "data": {
     *     "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *     "expiresIn": 3600,
     *     "user": {
     *       "id": 1001,
     *       "username": "admin",
     *       "email": "admin@eavi.xidian.edu.cn",
     *       "name": "管理员",
     *       "role": "admin",
     *       "department": "EAVI团队",
     *       "avatar": "https://api.eavi.xidian.edu.cn/avatars/admin.jpg",
     *       "lastLoginTime": "2024-01-15T10:30:00Z"
     *     }
     *   },
     *   "timestamp": "2024-01-15T10:30:00Z"
     * }
     *
     */

    @PostMapping("login")
    public Result login(@RequestBody User user){
        Result result = userService.login(user);
        System.out.println("result = " + result);
        return result;
    }

    @PostMapping("logout")
    public Result logout(@RequestBody String token){
        Result result = userService.logout(token);
        return result;
    }
}
