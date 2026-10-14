package com.ateavi.service;

import com.ateavi.pojo.PaperCode;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 联想
* @description 针对表【paper_code】的数据库操作Service
* @createDate 2025-12-02 20:21:50
*/
public interface PaperCodeService extends IService<PaperCode> {
    com.ateavi.utils.Result saveCode(Integer paperId, String codeName, String codeUrl);
    com.ateavi.utils.Result removeCode(Integer paperId, String codeName);
    com.ateavi.utils.Result uploadCodeFiles(Integer paperId, org.springframework.web.multipart.MultipartFile[] files, java.util.List<String> names);
}
