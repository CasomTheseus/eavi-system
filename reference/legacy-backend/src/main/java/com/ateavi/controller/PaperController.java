package com.ateavi.controller;

import com.ateavi.service.PaperService;
import com.ateavi.service.PaperCodeService;
import com.ateavi.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("papers")
public class PaperController {
    @Autowired
    private PaperService paperService;
    @Autowired
    private PaperCodeService paperCodeService;

    @GetMapping()
    public Result list(@RequestParam(value = "page", required = false) Integer page,
                       @RequestParam(value = "pageSize", required = false) Integer pageSize,
                       @RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "sortBy", required = false) String sortBy,
                       @RequestParam(value = "sortOrder", required = false) String sortOrder) {
        return paperService.listPapers(page, pageSize, search, sortBy, sortOrder);
    }

    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        return paperService.getPaperDetail(id);
    }

    @GetMapping("/{id}/repositories")
    public Result repositories(@PathVariable Long id) {
        return paperService.getPaperCodes(id);
    }

    @GetMapping("/{id}/datasets")
    public Result datasets(@PathVariable Long id) {
        return paperService.getPaperDatasets(id);
    }

    @GetMapping("/{id}/results")
    public Result results(@PathVariable Long id) {
        return paperService.getPaperResults(id);
    }

    @PostMapping("/{id}/tasks")
    public Result addTask(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String taskName = (String) body.get("taskName");
        return paperService.addPaperTask(id, taskName);
    }

    @DeleteMapping("/{id}/tasks")
    public Result removeTask(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String taskName = (String) body.get("taskName");
        return paperService.removePaperTask(id, taskName);
    }

    @PostMapping("/{id}/datasets")
    public Result addDataset(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer datasetId = null;
        Object did = body.get("datasetId");
        if (did instanceof Integer) datasetId = (Integer) did;
        else if (did instanceof Long) datasetId = ((Long) did).intValue();
        String datasetUrl = (String) body.get("datasetUrl");
        String displayName = (String) body.get("displayName");
        return paperService.addPaperDataset(id, datasetId, datasetUrl, displayName);
    }

    @DeleteMapping("/{id}/datasets")
    public Result removeDataset(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Integer datasetId = null;
        Object did = body.get("datasetId");
        if (did instanceof Integer) datasetId = (Integer) did;
        else if (did instanceof Long) datasetId = ((Long) did).intValue();
        String datasetUrl = (String) body.get("datasetUrl");
        return paperService.removePaperDataset(id, datasetId, datasetUrl);
    }

    @PostMapping("/{id}/codes")
    public Result addCode(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String codeName = (String) body.get("codeName");
        String codeUrl = (String) body.get("codeUrl");
        return paperService.addPaperCode(id, codeName, codeUrl);
    }

    @DeleteMapping("/{id}/codes")
    public Result removeCode(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String codeName = (String) body.get("codeName");
        return paperService.removePaperCode(id, codeName);
    }

    @PostMapping("/{id}/codes/upload")
    public Result uploadCodes(@PathVariable Long id,
                              @RequestParam("files") org.springframework.web.multipart.MultipartFile[] files,
                              @RequestParam(value = "names", required = false) java.util.List<String> names) {
        return paperCodeService.uploadCodeFiles(id.intValue(), files, names);
    }

    @PostMapping("/{id}/note")
    public Result uploadNote(@PathVariable Long id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return paperService.uploadPaperNote(id, file);
    }

    @DeleteMapping("/{id}/note")
    public Result removeNote(@PathVariable Long id) {
        return paperService.removePaperNote(id);
    }
}
