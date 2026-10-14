package com.ateavi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.PaperCode;
import com.ateavi.service.PaperCodeService;
import com.ateavi.mapper.PaperCodeMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ateavi.utils.Result;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* @author 联想
* @description 针对表【paper_code】的数据库操作Service实现
* @createDate 2025-12-02 20:21:50
*/
@Service
public class PaperCodeServiceImpl extends ServiceImpl<PaperCodeMapper, PaperCode>
    implements PaperCodeService{

    @Value("${eavi.paper-code.storage-path:e:/UI/eavi-static/data/paper_code/}")
    private String storageRootPath;

    @Override
    public Result saveCode(Integer paperId, String codeName, String codeUrl) {
        LambdaQueryWrapper<PaperCode> qw = new LambdaQueryWrapper<>();
        qw.eq(PaperCode::getPaperId, paperId).eq(PaperCode::getCodeName, codeName);
        if (this.baseMapper.exists(qw)) {
            return Result.build(null, 500, "代码名称已存在");
        }
        PaperCode pc = new PaperCode();
        pc.setPaperId(paperId);
        pc.setCodeName(codeName);
        pc.setCodeUrl(codeUrl);
        this.baseMapper.insert(pc);
        return Result.ok(null);
    }

    @Override
    public Result removeCode(Integer paperId, String codeName) {
        LambdaQueryWrapper<PaperCode> qw = new LambdaQueryWrapper<>();
        qw.eq(PaperCode::getPaperId, paperId).eq(PaperCode::getCodeName, codeName);
        this.baseMapper.delete(qw);
        return Result.ok(null);
    }

    @Override
    public Result uploadCodeFiles(Integer paperId, MultipartFile[] files, java.util.List<String> names) {
        if (files == null || files.length == 0) {
            return Result.build(null, 500, "未选择文件");
        }
        List<Map<String, Object>> saved = new ArrayList<>();
        try {
            Path root = Paths.get(storageRootPath);
            if (!Files.exists(root)) Files.createDirectories(root);
            int idx = 0;
            for (MultipartFile file : files) {
                if (file == null || file.isEmpty()) continue;
                String original = file.getOriginalFilename();
                if (original == null) continue;
                String safeName = System.currentTimeMillis() + "_" + original;
                Path target = root.resolve(safeName);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                String url = safeName;
                String codeName = (names != null && idx < names.size() && names.get(idx) != null && !names.get(idx).trim().isEmpty()) ? names.get(idx).trim() : original;
                this.saveCode(paperId, codeName, url);
                Map<String, Object> item = new HashMap<>();
                item.put("codeName", codeName);
                item.put("codeUrl", url);
                item.put("size", file.getSize());
                saved.add(item);
                idx++;
            }
            return Result.ok(saved);
        } catch (Exception e) {
            return Result.build(null, 500, "上传失败");
        }
    }
}




