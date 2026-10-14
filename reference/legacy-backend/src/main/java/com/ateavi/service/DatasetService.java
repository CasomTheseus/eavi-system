package com.ateavi.service;

import com.ateavi.pojo.Dataset;
import com.ateavi.pojo.vo.DatasetListVo;
import com.ateavi.utils.Result;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.core.io.Resource;

/**
* @author 联想
* @description 针对表【dataset】的数据库操作Service
* @createDate 2025-10-20 23:42:42
*/
public interface DatasetService extends IService<Dataset> {

    Result findNewsPage(DatasetListVo datasetListVo);

    Result findDatasetDetail(Integer id);

    Result getMarkdownContent(Integer id);

    Resource getDatasetImage(Integer id, String imagePath);

    Result getDownloadLinks(Integer id);
}
