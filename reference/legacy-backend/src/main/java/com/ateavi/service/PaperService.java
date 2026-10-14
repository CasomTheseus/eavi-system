package com.ateavi.service;

import com.ateavi.pojo.Paper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ateavi.utils.Result;
import com.ateavi.pojo.vo.DatasetListVo;
import java.util.Map;

/**
* @author 联想
* @description 针对表【paper】的数据库操作Service
* @createDate 2025-11-27 21:43:30
*/
public interface PaperService extends IService<Paper> {

    Result listPapers(Integer page, Integer pageSize, String search, String sortBy, String sortOrder);

    Result getPaperDetail(Long id);

    Result getPaperCodes(Long id);

    Result getPaperDatasets(Long id);

    Result getPaperResults(Long id);

    Result addPaperTask(Long id, String taskName);

    Result removePaperTask(Long id, String taskName);

    Result addPaperDataset(Long id, Integer datasetId, String datasetUrl, String displayName);

    Result removePaperDataset(Long id, Integer datasetId, String datasetUrl);

    Result addPaperCode(Long id, String codeName, String codeUrl);

    Result removePaperCode(Long id, String codeName);

    Result uploadPaperNote(Long id, org.springframework.web.multipart.MultipartFile file);

    Result removePaperNote(Long id);
}
