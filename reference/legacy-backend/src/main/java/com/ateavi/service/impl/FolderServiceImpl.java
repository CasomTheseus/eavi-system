package com.ateavi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.Folder;
import com.ateavi.service.FolderService;
import com.ateavi.mapper.FolderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ateavi.utils.Result;
import java.util.*;
import com.ateavi.mapper.FolderPaperMapper;
import com.ateavi.pojo.FolderPaper;

/**
* @author 联想
* @description 针对表【folder】的数据库操作Service实现
* @createDate 2025-11-28 16:56:26
*/
@Service
public class FolderServiceImpl extends ServiceImpl<FolderMapper, Folder>
    implements FolderService{
    @Autowired
    private FolderMapper folderMapper;
    @Autowired
    private FolderPaperMapper folderPaperMapper;

    @Override
    public Result getTree(Integer userId) {
        LambdaQueryWrapper<Folder> qw = new LambdaQueryWrapper<>();
        qw.eq(Folder::getIsDeleted, 0);
        if (userId != null) qw.eq(Folder::getUserId, userId);
        List<Folder> folders = folderMapper.selectList(qw);
        List<FolderPaper> links = folderPaperMapper.selectList(null);
        Map<Integer, List<Long>> folderPapers = new HashMap<>();
        for (FolderPaper fp : links) {
            folderPapers.computeIfAbsent(fp.getFolderId(), k -> new ArrayList<>()).add(fp.getPaperId());
        }
        Map<Integer, Map<String, Object>> nodeMap = new HashMap<>();
        for (Folder f : folders) {
            Map<String, Object> node = new HashMap<>();
            node.put("id", f.getFolderId());
            node.put("name", f.getName());
            node.put("parentId", f.getParentId());
            node.put("paperIds", folderPapers.getOrDefault(f.getFolderId(), Collections.emptyList()));
            node.put("children", new ArrayList<>());
            nodeMap.put(f.getFolderId(), node);
        }
        List<Map<String, Object>> roots = new ArrayList<>();
        for (Folder f : folders) {
            Map<String, Object> node = nodeMap.get(f.getFolderId());
            Integer pid = f.getParentId();
            if (pid == null) {
                roots.add(node);
            } else {
                Map<String, Object> parent = nodeMap.get(pid);
                if (parent != null) {
                    ((List<Map<String, Object>>) parent.get("children")).add(node);
                } else {
                    roots.add(node);
                }
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("folders", roots);
        return Result.ok(data);
    }

    @Override
    public Result createFolder(String name, Integer parentId, Integer userId) {
        Folder f = new Folder();
        f.setName(name);
        f.setParentId(parentId);
        f.setUserId(userId);
        f.setIsDeleted(0);
        this.save(f);
        Map<String, Object> data = new HashMap<>();
        data.put("folderId", f.getFolderId());
        data.put("name", f.getName());
        data.put("parentId", f.getParentId());
        return Result.ok(data);
    }

    @Override
    @Transactional
    public Result deleteFolder(Integer folderId) {
        Folder target = folderMapper.selectById(folderId);
        if (target == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("deleted", false);
            data.put("message", "not-found");
            return Result.ok(data);
        }
        if ("我的文库".equals(target.getName())) {
            Map<String, Object> data = new HashMap<>();
            data.put("deleted", false);
            data.put("message", "cannot-delete-root");
            return Result.ok(data);
        }

        LambdaQueryWrapper<Folder> rootQw = new LambdaQueryWrapper<>();
        rootQw.eq(Folder::getName, "我的文库");
        if (target.getUserId() != null) rootQw.eq(Folder::getUserId, target.getUserId());
        rootQw.eq(Folder::getIsDeleted, 0);
        Folder myLib = folderMapper.selectOne(rootQw);
        if (myLib == null) {
            myLib = new Folder();
            myLib.setName("我的文库");
            myLib.setParentId(null);
            myLib.setUserId(target.getUserId());
            myLib.setIsDeleted(0);
            folderMapper.insert(myLib);
        }

        LambdaQueryWrapper<Folder> allQw = new LambdaQueryWrapper<>();
        if (target.getUserId() != null) allQw.eq(Folder::getUserId, target.getUserId());
        allQw.eq(Folder::getIsDeleted, 0);
        List<Folder> all = folderMapper.selectList(allQw);
        Map<Integer, List<Integer>> childrenMap = new HashMap<>();
        for (Folder f : all) {
            Integer pid = f.getParentId();
            if (pid != null) {
                childrenMap.computeIfAbsent(pid, k -> new ArrayList<>()).add(f.getFolderId());
            }
        }
        List<Integer> toDelete = new ArrayList<>();
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(target.getFolderId());
        while (!stack.isEmpty()) {
            Integer id = stack.pop();
            toDelete.add(id);
            List<Integer> kids = childrenMap.getOrDefault(id, Collections.emptyList());
            for (Integer k : kids) stack.push(k);
        }

        LambdaQueryWrapper<com.ateavi.pojo.FolderPaper> fpQw = new LambdaQueryWrapper<>();
        fpQw.in(com.ateavi.pojo.FolderPaper::getFolderId, toDelete);
        List<com.ateavi.pojo.FolderPaper> links = folderPaperMapper.selectList(fpQw);
        int moved = 0;
        for (com.ateavi.pojo.FolderPaper link : links) {
            Long pid = link.getPaperId();
            LambdaQueryWrapper<com.ateavi.pojo.FolderPaper> existsQw = new LambdaQueryWrapper<>();
            existsQw.eq(com.ateavi.pojo.FolderPaper::getFolderId, myLib.getFolderId())
                    .eq(com.ateavi.pojo.FolderPaper::getPaperId, pid);
            com.ateavi.pojo.FolderPaper exists = folderPaperMapper.selectOne(existsQw);
            if (exists == null) {
                com.ateavi.pojo.FolderPaper fp = new com.ateavi.pojo.FolderPaper();
                fp.setFolderId(myLib.getFolderId());
                fp.setPaperId(pid);
                folderPaperMapper.insert(fp);
                moved++;
            }
        }

        // 清理子树中的映射
        folderPaperMapper.delete(fpQw);
        // 删除所有子目录
        if (!toDelete.isEmpty()) {
            folderMapper.deleteBatchIds(toDelete);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("deleted", true);
        data.put("movedCount", moved);
        data.put("targetFolderId", target.getFolderId());
        data.put("myLibraryId", myLib.getFolderId());
        return Result.ok(data);
    }
}



