package com.ateavi.service;

import com.ateavi.pojo.User;
import com.ateavi.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 联想
* @description 针对表【user】的数据库操作Service
* @createDate 2025-10-07 20:41:36
*/
public interface UserService extends IService<User> {

    Result login(User user);

    Result logout(String token);
}
