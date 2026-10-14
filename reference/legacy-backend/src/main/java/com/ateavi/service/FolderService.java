package com.ateavi.service;

import com.ateavi.pojo.Folder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 联想
* @description 针对表【folder】的数据库操作Service
* @createDate 2025-11-28 16:56:26
*/
public interface FolderService extends IService<Folder> {

    com.ateavi.utils.Result getTree(Integer userId);

    com.ateavi.utils.Result createFolder(String name, Integer parentId, Integer userId);

    com.ateavi.utils.Result deleteFolder(Integer folderId);
}
