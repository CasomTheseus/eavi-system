package com.ateavi.controller;

import com.ateavi.mapper.DatasetTaskMapper;
import com.ateavi.pojo.Task;
import com.ateavi.service.TaskService;
import com.ateavi.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private DatasetTaskMapper datasetTaskMapper;

    @GetMapping()
    public Result listAllTasks(){
        List<Task> tasks = taskService.list();
        return Result.ok(tasks);
    }

    @GetMapping("/by-dataset/{datasetId}")
    public Result listTasksByDataset(@PathVariable Integer datasetId){
        List<Task> tasks = datasetTaskMapper.selectTasksByDatasetId(datasetId);
        return Result.ok(tasks);
    }
}

