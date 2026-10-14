package com.ateavi.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.ateavi.mapper.RoleMapper;
import com.ateavi.pojo.Dataset;
import com.ateavi.pojo.Role;
import com.ateavi.pojo.vo.UserVo;
import com.ateavi.service.RoleService;
import com.ateavi.utils.JwtHelper;
import com.ateavi.utils.MD5Util;
import com.ateavi.utils.Result;
import com.ateavi.utils.ResultCodeEnum;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.User;
import com.ateavi.service.UserService;
import com.ateavi.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 联想
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2025-10-07 20:41:36
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RoleMapper roleMapper;

    /**
     * 登录业务实现
     *
     * @param user
     * @return result封装
     */
    @Override
    public Result login(User user) {
        //根据账号查询
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Role> roleLambdaQueryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, user.getUsername());
        User loginUser = userMapper.selectOne(queryWrapper);
        //账号判断
        if (loginUser == null) {
            //账号错误
            return Result.build(null, ResultCodeEnum.USERNAME_ERROR);
        }
        //判断密码
        if (!StringUtils.isEmpty(user.getPassword())
                && loginUser.getPassword().equals(MD5Util.encrypt(user.getPassword()))) {
            //账号密码正确
            //根据用户唯一标识生成token
            String token = jwtHelper.createToken(Long.valueOf(loginUser.getUserId()));
            String refreshToken = null;
            roleLambdaQueryWrapper.eq(Role::getRoleId, loginUser.getRoleId());
            Role role = roleMapper.selectOne(roleLambdaQueryWrapper);
            UserVo userVo = new UserVo(
                    loginUser.getUserId(),
                    loginUser.getUsername(),
                    loginUser.getEmail(),
                    loginUser.getName(),
                    role.getRole(),
                    "EAVI团队",
                    loginUser.getAvatar(),
                    new Date().toString());
            Map data = new HashMap();
            data.put("token", token);
            if (!jwtHelper.isExpiration(token)) {
                refreshToken = jwtHelper.createToken(Long.valueOf(loginUser.getUserId()));
            } else {
                refreshToken = token;
            }
            data.put("refreshToken", refreshToken);
            data.put("expiresIn", jwtHelper.getTokenExpiration() * 1000 * 60);
            data.put("user", userVo);
            return Result.ok(data);
        }
        //密码错误
        Map data = new HashMap();
//        data.put("")
        return Result.build(null, ResultCodeEnum.PASSWORD_ERROR);
    }

    @Override
    public Result logout(String token) {
        jwtHelper.isLogout(token);
        return Result.build(null, ResultCodeEnum.SUCCESS);
    }
}




