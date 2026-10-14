package com.ateavi.mapper;

import com.ateavi.pojo.Dataset;
import com.ateavi.pojo.DatasetDocument;
import com.ateavi.pojo.vo.DatasetListVo;
import com.ateavi.pojo.vo.DatasetVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

/**
* @author 联想
* @description 针对表【dataset】的数据库操作Mapper
* @createDate 2025-10-20 23:42:42
* @Entity com.ateavi.pojo.Dataset
*/
public interface DatasetMapper extends BaseMapper<Dataset> {

    IPage<Map> selectMyPage(@Param("page") IPage<Map> page, @Param("datasetListVo") DatasetListVo datasetListVo);

    DatasetVo selectDataset(@Param("datasetId") Integer datasetId);

    @Select("SELECT COUNT(*) > 0 FROM dataset WHERE base_address = #{baseAddress} AND is_deleted = 0")
    boolean existsByBaseAddress(@Param("baseAddress") String baseAddress);
}




