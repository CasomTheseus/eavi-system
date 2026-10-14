package com.ateavi.controller;

import com.ateavi.service.FolderPaperService;
import com.ateavi.service.FolderService;
import com.ateavi.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("folders")
public class FolderController {
    @Autowired
    private FolderService folderService;
    @Autowired
    private FolderPaperService folderPaperService;

    @GetMapping("/tree")
    public Result tree(@RequestParam(value = "userId", required = false) Integer userId) {
        return folderService.getTree(userId);
    }

    @PostMapping()
    public Result create(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        Integer parentId = body.get("parentId") == null ? null : (Integer) body.get("parentId");
        Integer userId = body.get("userId") == null ? null : (Integer) body.get("userId");
        return folderService.createFolder(name, parentId, userId);
    }

    @PostMapping("/{folderId}/papers")
    public Result addPaper(@PathVariable Integer folderId, @RequestBody Map<String, Object> body) {
        Long paperId = body.get("paperId") instanceof Integer ? Long.valueOf((Integer) body.get("paperId")) : (Long) body.get("paperId");
        return folderPaperService.addPaper(folderId, paperId);
    }

    @DeleteMapping("/{folderId}/papers/{paperId}")
    public Result removePaper(@PathVariable Integer folderId, @PathVariable Long paperId) {
        return folderPaperService.removePaper(folderId, paperId);
    }

    @DeleteMapping("/{folderId}")
    public Result delete(@PathVariable Integer folderId) {
        return folderService.deleteFolder(folderId);
    }

    @PostMapping("/{folderId}/delete")
    public Result deleteCompat(@PathVariable Integer folderId) {
        return folderService.deleteFolder(folderId);
    }

    @PostMapping("/delete")
    public Result deleteBody(@RequestBody Map<String, Object> body) {
        Integer folderId = body.get("folderId") instanceof Integer ? (Integer) body.get("folderId") : null;
        return folderService.deleteFolder(folderId);
    }
}
