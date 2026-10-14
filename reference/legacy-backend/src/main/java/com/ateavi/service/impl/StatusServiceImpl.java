package com.ateavi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.Status;
import com.ateavi.service.StatusService;
import com.ateavi.mapper.StatusMapper;
import org.springframework.stereotype.Service;

/**
* @author 联想
* @description 针对表【status】的数据库操作Service实现
* @createDate 2025-10-07 17:42:05
*/
@Service
public class StatusServiceImpl extends ServiceImpl<StatusMapper, Status>
    implements StatusService{

}




