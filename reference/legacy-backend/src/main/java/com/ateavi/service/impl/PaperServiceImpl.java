package com.ateavi.service.impl;

import com.ateavi.mapper.*;
import com.ateavi.pojo.*;
import com.ateavi.service.PaperService;
import com.ateavi.utils.ResultCodeEnum;
import org.commonmark.node.Code;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.utils.Result;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 联想
* @description 针对表【paper】的数据库操作Service实现
* @createDate 2025-11-27 21:43:30
*/
@Service
public class PaperServiceImpl extends ServiceImpl<PaperMapper, Paper>
    implements PaperService{
    @Autowired
    private PaperMapper paperMapper;
    @Autowired
    private PaperTaskMapper paperTaskMapper;
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private PaperDatasetMapper paperDatasetMapper;
    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private PaperCodeMapper paperCodeMapper;
    @Autowired
    private com.ateavi.service.PaperCodeService paperCodeService;

    @Override
    public Result listPapers(Integer page, Integer pageSize, String search, String sortBy, String sortOrder) {
        Page<Paper> p = new Page<>(page == null ? 1 : page, pageSize == null ? 20 : pageSize);
        LambdaQueryWrapper<Paper> qw = new LambdaQueryWrapper<>();
        if (search != null && !search.trim().isEmpty()) {
            String q = search.trim();
            qw.and(w -> w.like(Paper::getTitle, q)
                        .or().like(Paper::getAuthors, q)
                        .or().like(Paper::getVenue, q)
                        .or().like(Paper::getPaperAbstract, q));
        }
        if ("citations".equalsIgnoreCase(sortBy)) {
            qw.orderBy(true, "asc".equalsIgnoreCase(sortOrder), Paper::getCitations);
        } else if ("uploadTime".equalsIgnoreCase(sortBy)
                || "date".equalsIgnoreCase(sortBy)
                || "year".equalsIgnoreCase(sortBy)) {
            qw.orderBy(true, "asc".equalsIgnoreCase(sortOrder), Paper::getUploadTime);
        } else {
            qw.orderBy(true, false, Paper::getUploadTime);
        }
        IPage<Paper> pageData = this.page(p, qw);
        Map<String, Object> data = new HashMap<>();
        data.put("papers", pageData.getRecords());
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", pageData.getCurrent());
        pageInfo.put("pageSize", pageData.getSize());
        pageInfo.put("totalPages", pageData.getPages());
        pageInfo.put("total", pageData.getTotal());
        data.put("pageInfo", pageInfo);
        return Result.ok(data);
    }

    @Override
    public Result getPaperDetail(Long id) {
        Map<String, Object> paper = paperMapper.selectPaperDetail(id);
        List<Map<String, Object>> datasets = paperMapper.selectPaperDatasets(id);
        List<Map<String, Object>> codes = paperMapper.selectPaperCodes(id);
        List<String> tasks = paperMapper.selectPaperTasks(id);
        List<Map<String, Object>> results = paperMapper.selectPaperResults(id);
        Map<String, Object> data = new HashMap<>();
        data.put("paper", paper);
        data.put("datasets", datasets);
        data.put("repositories", codes);
        data.put("tasks", tasks);
        data.put("results", results);
        return Result.ok(data);
    }

    @Override
    public Result getPaperCodes(Long id) {
        List<Map<String, Object>> codes = paperMapper.selectPaperCodes(id);
        Map<String, Object> data = new HashMap<>();
        data.put("repositories", codes);
        return Result.ok(data);
    }

    @Override
    public Result getPaperDatasets(Long id) {
        List<Map<String, Object>> datasets = paperMapper.selectPaperDatasets(id);
        Map<String, Object> data = new HashMap<>();
        data.put("datasets", datasets);
        return Result.ok(data);
    }

    @Override
    public Result getPaperResults(Long id) {
        List<Map<String, Object>> results = paperMapper.selectPaperResults(id);
        Map<String, Object> data = new HashMap<>();
        data.put("results", results);
        return Result.ok(data);
    }

    @Override
    public Result addPaperTask(Long id, String taskName) {
        if (taskName == null || taskName.trim().isEmpty()) return Result.build(null, 500, "任务名称不能为空");
        // Check if task exists
        LambdaQueryWrapper<Task> tqw = new LambdaQueryWrapper<>();
        tqw.eq(Task::getName, taskName);
        Task task = taskMapper.selectOne(tqw);
        if (task == null) {
            return Result.build(null, 500, "任务不存在，请先创建任务");
        }
        
        // Check if relationship exists
        LambdaQueryWrapper<PaperTask> ptqw = new LambdaQueryWrapper<>();
        ptqw.eq(PaperTask::getPaperId, id).eq(PaperTask::getTaskId, task.getId());
        if (paperTaskMapper.exists(ptqw)) {
            return Result.ok("任务已关联");
        }
        
        PaperTask pt = new PaperTask();
        pt.setPaperId(id);
        pt.setTaskId(task.getId());
        paperTaskMapper.insert(pt);
        return Result.ok(null);
    }

    @Override
    public Result removePaperTask(Long id, String taskName) {
        LambdaQueryWrapper<Task> tqw = new LambdaQueryWrapper<>();
        tqw.eq(Task::getName, taskName);
        Task task = taskMapper.selectOne(tqw);
        if (task != null) {
            LambdaQueryWrapper<PaperTask> ptqw = new LambdaQueryWrapper<>();
            ptqw.eq(PaperTask::getPaperId, id).eq(PaperTask::getTaskId, task.getId());
            paperTaskMapper.delete(ptqw);
        }
        return Result.ok(null);
    }

    @Override
    public Result addPaperDataset(Long id, Integer datasetId, String datasetUrl, String displayName) {
        if ((datasetId == null) && (datasetUrl == null || datasetUrl.trim().isEmpty())) {
            return Result.build(null, 500, "缺少数据集信息");
        }
        if (datasetId != null) {
            LambdaQueryWrapper<PaperDataset> pdqw = new LambdaQueryWrapper<>();
            pdqw.eq(PaperDataset::getPaperId, id).eq(PaperDataset::getDatasetId, datasetId);
            if (paperDatasetMapper.exists(pdqw)) {
                return Result.ok("数据集已关联");
            }
            PaperDataset pd = new PaperDataset();
            pd.setPaperId(id);
            pd.setDatasetId(datasetId);
            paperDatasetMapper.insert(pd);
            return Result.ok(null);
        } else {
            PaperDataset pd = new PaperDataset();
            pd.setPaperId(id);
            pd.setDatasetUrl(datasetUrl.trim());
            if (displayName != null && !displayName.trim().isEmpty()) {
                pd.setDisplayName(displayName.trim());
            }
            LambdaQueryWrapper<PaperDataset> pdqw = new LambdaQueryWrapper<>();
            pdqw.eq(PaperDataset::getPaperId, id).eq(PaperDataset::getDatasetUrl, pd.getDatasetUrl());
            if (paperDatasetMapper.exists(pdqw)) {
                return Result.ok("数据集已关联");
            }
            paperDatasetMapper.insert(pd);
            return Result.ok(null);
        }
    }

    @Override
    public Result removePaperDataset(Long id, Integer datasetId, String datasetUrl) {
        if (datasetId != null) {
            LambdaQueryWrapper<PaperDataset> pdqw = new LambdaQueryWrapper<>();
            pdqw.eq(PaperDataset::getPaperId, id).eq(PaperDataset::getDatasetId, datasetId);
            paperDatasetMapper.delete(pdqw);
        } else if (datasetUrl != null && !datasetUrl.trim().isEmpty()) {
            LambdaQueryWrapper<PaperDataset> pdqw = new LambdaQueryWrapper<>();
            pdqw.eq(PaperDataset::getPaperId, id).eq(PaperDataset::getDatasetUrl, datasetUrl.trim());
            paperDatasetMapper.delete(pdqw);
        }
        return Result.ok(null);
    }

    @Override
    public Result addPaperCode(Long id, String codeName, String codeUrl) {
        if (codeName == null || codeName.trim().isEmpty()) return Result.build(null, 500, "代码名称不能为空");
        return paperCodeService.saveCode(id.intValue(), codeName, codeUrl);
    }

    @Override
    public Result removePaperCode(Long id, String codeName) {
        return paperCodeService.removeCode(id.intValue(), codeName);
    }

    @org.springframework.beans.factory.annotation.Value("${eavi.paper-note.storage-path:e:/UI/eavi-static/data/paper_note/}")
    private String noteStoragePath;

    @Override
    public Result uploadPaperNote(Long id, org.springframework.web.multipart.MultipartFile file) {
        if (file == null || file.isEmpty()) return Result.build(null, 500, "文件为空");
        Paper paper = paperMapper.selectById(id);
        if (paper == null) return Result.build(null, 404, "文献不存在");
        
        try {
            java.nio.file.Path root = java.nio.file.Paths.get(noteStoragePath);
            if (!java.nio.file.Files.exists(root)) java.nio.file.Files.createDirectories(root);
            
            String original = file.getOriginalFilename();
            String safeName = System.currentTimeMillis() + "_" + original;
            java.nio.file.Path target = root.resolve(safeName);
            java.nio.file.Files.copy(file.getInputStream(), target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Delete old note if exists
            if (paper.getNoteUrl() != null && !paper.getNoteUrl().isEmpty()) {
                try {
                    java.nio.file.Files.deleteIfExists(root.resolve(paper.getNoteUrl()));
                } catch (Exception ignore) {}
            }
            
            paper.setNoteUrl(safeName);
            paperMapper.updateById(paper);
            
            Map<String, Object> data = new HashMap<>();
            data.put("noteUrl", safeName);
            return Result.ok(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.build(null, 500, "上传失败: " + e.getMessage());
        }
    }

    @Override
    public Result removePaperNote(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) return Result.build(null, 404, "文献不存在");
        
        if (paper.getNoteUrl() != null && !paper.getNoteUrl().isEmpty()) {
            try {
                java.nio.file.Path root = java.nio.file.Paths.get(noteStoragePath);
                java.nio.file.Files.deleteIfExists(root.resolve(paper.getNoteUrl()));
            } catch (Exception ignore) {}
            
            paper.setNoteUrl(null); // Or empty string? Null is better for MP
            // Since POJO defines it as String, MP handles null.
            // But wait, if I set it to null, will MP update it to NULL in DB? 
            // Default update strategy is usually NOT_NULL.
            // I should use UpdateWrapper to force set null if needed, or check MP config.
            // Safe bet: use UpdateWrapper or set empty string if DB allows.
            // Let's try UpdateWrapper to be safe.
            
            LambdaQueryWrapper<Paper> qw = new LambdaQueryWrapper<>();
            qw.eq(Paper::getId, id);
            
            // To update a field to null, we can just use update(entity) if strategy allows,
            // or use UpdateWrapper.set("note_url", null).
            // Let's use custom update or just set empty string if that's acceptable, 
            // but `noteUrl` is nullable in DB usually.
            // Let's try setting to null and using `updateById`. If it fails to update null, we'll fix.
            // Actually, for MP, `updateById` ignores nulls by default. 
            // So we must use UpdateWrapper.
            
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Paper> uw = new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            uw.eq(Paper::getId, id).set(Paper::getNoteUrl, null);
            paperMapper.update(null, uw);
        }
        
        return Result.ok(null);
    }
}
