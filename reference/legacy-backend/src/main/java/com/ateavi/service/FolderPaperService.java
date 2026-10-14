package com.ateavi.service;

import com.ateavi.pojo.FolderPaper;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 联想
* @description 针对表【folder_paper】的数据库操作Service
* @createDate 2025-11-28 16:56:35
*/
public interface FolderPaperService extends IService<FolderPaper> {

    com.ateavi.utils.Result addPaper(Integer folderId, Long paperId);

    com.ateavi.utils.Result removePaper(Integer folderId, Long paperId);
}
