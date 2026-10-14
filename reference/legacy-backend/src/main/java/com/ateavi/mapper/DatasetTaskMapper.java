package com.ateavi.mapper;

import com.ateavi.pojo.DatasetTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author 联想
* @description 针对表【dataset_task】的数据库操作Mapper
* @createDate 2025-12-01 20:47:51
* @Entity com.ateavi.pojo.DatasetTask
*/
public interface DatasetTaskMapper extends BaseMapper<DatasetTask> {
    java.util.List<com.ateavi.pojo.Task> selectTasksByDatasetId(Integer datasetId);
}




