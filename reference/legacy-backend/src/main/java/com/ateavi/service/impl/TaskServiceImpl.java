package com.ateavi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.Task;
import com.ateavi.service.TaskService;
import com.ateavi.mapper.TaskMapper;
import org.springframework.stereotype.Service;

/**
* @author 联想
* @description 针对表【task】的数据库操作Service实现
* @createDate 2025-11-27 21:43:43
*/
@Service
public class TaskServiceImpl extends ServiceImpl<TaskMapper, Task>
    implements TaskService{

}




