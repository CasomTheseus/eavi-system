package com.ateavi.mapper;

import com.ateavi.pojo.Paper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
* @author 联想
* @description 针对表【paper】的数据库操作Mapper
* @createDate 2025-11-27 21:43:30
* @Entity com.ateavi.pojo.Paper
*/
public interface PaperMapper extends BaseMapper<Paper> {
    Map<String, Object> selectPaperDetail(@Param("id") Long id);
    List<Map<String, Object>> selectPaperDatasets(@Param("id") Long id);
    List<Map<String, Object>> selectPaperCodes(@Param("id") Long id);
    List<Map<String, Object>> selectPaperResults(@Param("id") Long id);
    List<String> selectPaperTasks(@Param("id") Long id);
}



