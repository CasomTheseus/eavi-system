package com.ateavi.controller;

import com.ateavi.pojo.DTO.DatasetSaveResult;
import com.ateavi.pojo.Dataset;
import com.ateavi.pojo.DatasetTask;
import com.ateavi.pojo.vo.DatasetListVo;
import com.ateavi.pojo.vo.DatasetVo;
import com.ateavi.service.DatasetService;
import com.ateavi.service.HtmlToMarkdownService;
import com.ateavi.service.DatasetTaskService;
import com.ateavi.utils.JwtHelper;
import com.ateavi.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName:DatasetController
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/8 15:23
 * @Version 1.0
 */
@RestController
@CrossOrigin
@RequestMapping("datasets")
public class DatasetController {
    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private DatasetService datasetService;
    @Autowired
    private HtmlToMarkdownService htmlToMarkdownService;
    @Autowired
    private DatasetTaskService datasetTaskService;

    @GetMapping()
    public Result findNewsPage(DatasetListVo datasetListVo){
        Result result = datasetService.findNewsPage(datasetListVo);
        return result;
    }

    //明天实现逻辑
    @GetMapping("/{id}")
    public Result showDatasetDetail(@PathVariable Integer id){
        Result result = datasetService.findDatasetDetail(id);
        return result;
    }

    @GetMapping("/{id}/markdown-content")
    public Result getMarkdownContent(@PathVariable Integer id){
        Result result = datasetService.getMarkdownContent(id);
        return result;
    }

    @GetMapping("/{id}/images/{imagePath}")
    public ResponseEntity<Resource> getMarkdownImg(@PathVariable Integer id, @PathVariable String imagePath, HttpServletRequest request){
        try {
            // 获取图片资源
            Resource imageResource = datasetService.getDatasetImage(id, imagePath);

            if (!imageResource.exists() || !imageResource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // 确定内容类型
            String contentType = null;
            try {
                contentType = request.getServletContext().getMimeType(imageResource.getFile().getAbsolutePath());
            } catch (IOException ex) {
            }

            // 如果无法确定内容类型，则使用默认值
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // 设置缓存头
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // 缓存1小时
                    .header(HttpHeaders.ETAG, generateETag(imageResource))
                    .body(imageResource);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            if (e.getMessage().contains("不存在")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


    }
    private String generateETag(Resource resource) {
        try {
            long lastModified = resource.lastModified();
            long contentLength = resource.contentLength();
            return "\"" + lastModified + "-" + contentLength + "\"";
        } catch (IOException e) {
            return "\"" + System.currentTimeMillis() + "\"";
        }
    }


    @GetMapping("/{id}/download-links")
    public Result getDownloadLinks(@PathVariable Integer id){
        Result result = datasetService.getDownloadLinks(id);
        return result;
    }

    @PostMapping("/upload")
    public Result uploadDataset(
            @RequestParam(value = "datasetName", required = false) String datasetName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "htmlContent", required = false) String htmlContent,
            @RequestParam(value = "markdownFile", required = false) MultipartFile markdownFile,
            @RequestParam(value = "trueAddress", required = false) String trueAddress,
            @RequestParam(value = "sizeFormatted", required = false) String sizeFormatted,
            @RequestParam(value = "taskIds", required = false) String taskIds,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ){

        try {
            // 从Authorization Header获取token，去掉Bearer前缀
            if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.startsWith("Bearer ")) {
                return Result.build(null, 401, "缺少有效的Authorization Header");
            }
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            
            if(token == null || token.trim().isEmpty() || jwtHelper.isExpiration(token)){
                return Result.build(null,401,"用户未登录或token已过期");
            }
            // 验证必填参数
            if (datasetName == null || datasetName.trim().isEmpty()) {
                return Result.build(null,400,"数据集名称不能为空");
            }

            DatasetVo datasetVo = new DatasetVo();
            datasetVo.setDescription(description);
            datasetVo.setDatasetName(datasetName);
            datasetVo.setTrueAddress(trueAddress);
            datasetVo.setSizeFormatted(sizeFormatted);
            // 6. 分离图片文件和数据集文件
            List<MultipartFile> imageFiles = new ArrayList<>();
            List<MultipartFile> datasetFiles = new ArrayList<>();

            if (files != null) {
                for (MultipartFile file : files) {
                    String contentType = file.getContentType();
                    if (contentType != null && contentType.startsWith("image/")) {
                        imageFiles.add(file);
                    } else {
                        datasetFiles.add(file);
                    }
                }
            }

            // 7. 处理数据集文件保存
            Dataset result;
            if (markdownFile != null && !markdownFile.isEmpty()) {
                result = htmlToMarkdownService.saveDatasetMarkdownFile(
                        datasetVo, markdownFile,
                        imageFiles.toArray(new MultipartFile[0]),
                        datasetFiles.toArray(new MultipartFile[0]), token);
            } else {
                if (htmlContent == null || htmlContent.trim().isEmpty()) {
                    return Result.build(null,400,"HTML内容或Markdown文件至少提供一个");
                }
                result = htmlToMarkdownService.saveDatasetFiles(
                        datasetVo, htmlContent,
                        imageFiles.toArray(new MultipartFile[0]),
                        datasetFiles.toArray(new MultipartFile[0]),token);
            }

            Integer datasetId = htmlToMarkdownService.saveDatasetToDatabase(result);
            result.setDatasetId(datasetId);

            // 保存任务关联关系
            if (taskIds != null && !taskIds.trim().isEmpty() && datasetId != null) {
                String trimmed = taskIds.trim();
                List<Integer> ids = new ArrayList<>();
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode node = mapper.readTree(trimmed);
                    if (node.isArray()) {
                        for (JsonNode item : node) {
                            if (item.isNumber()) {
                                ids.add(item.intValue());
                            } else if (item.isTextual()) {
                                String num = item.asText().replaceAll("[^0-9]", "");
                                if (!num.isEmpty()) ids.add(Integer.parseInt(num));
                            }
                        }
                    } else {
                        for (String s : trimmed.split(",")) {
                            String num = s.replaceAll("[^0-9]", "").trim();
                            if (!num.isEmpty()) ids.add(Integer.parseInt(num));
                        }
                    }
                } catch (Exception e) {
                    for (String s : trimmed.split(",")) {
                        String num = s.replaceAll("[^0-9]", "").trim();
                        if (!num.isEmpty()) ids.add(Integer.parseInt(num));
                    }
                }

                if (!ids.isEmpty()) {
                    List<DatasetTask> relations = new ArrayList<>();
                    for (Integer tid : ids) {
                        DatasetTask dt = new DatasetTask();
                        dt.setDatasetId(datasetId);
                        dt.setTaskId(tid);
                        relations.add(dt);
                    }
                    datasetTaskService.saveBatch(relations);
                }
            }
            return Result.ok(result);
        }catch (Exception e){
            System.out.println(e);
            return Result.build(null,500,"数据集上传失败: " + e.getMessage());
        }
    }
}
