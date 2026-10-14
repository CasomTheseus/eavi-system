package com.ateavi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.FolderPaper;
import com.ateavi.service.FolderPaperService;
import com.ateavi.mapper.FolderPaperMapper;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ateavi.utils.Result;
import java.util.HashMap;
import java.util.Map;

/**
* @author 联想
* @description 针对表【folder_paper】的数据库操作Service实现
* @createDate 2025-11-28 16:56:35
*/
@Service
public class FolderPaperServiceImpl extends ServiceImpl<FolderPaperMapper, FolderPaper>
    implements FolderPaperService{
    @Override
    public Result addPaper(Integer folderId, Long paperId) {
        LambdaQueryWrapper<FolderPaper> qw = new LambdaQueryWrapper<>();
        qw.eq(FolderPaper::getFolderId, folderId).eq(FolderPaper::getPaperId, paperId);
        FolderPaper exists = this.getOne(qw);
        Map<String, Object> data = new HashMap<>();
        data.put("folderId", folderId);
        data.put("paperId", paperId);
        if (exists != null) {
            data.put("exists", true);
            return Result.ok(data);
        }
        FolderPaper fp = new FolderPaper();
        fp.setFolderId(folderId);
        fp.setPaperId(paperId);
        this.save(fp);
        data.put("exists", false);
        return Result.ok(data);
    }

    @Override
    public Result removePaper(Integer folderId, Long paperId) {
        LambdaQueryWrapper<FolderPaper> qw = new LambdaQueryWrapper<>();
        qw.eq(FolderPaper::getFolderId, folderId).eq(FolderPaper::getPaperId, paperId);
        this.remove(qw);
        Map<String, Object> data = new HashMap<>();
        data.put("folderId", folderId);
        data.put("paperId", paperId);
        return Result.ok(data);
    }
}




