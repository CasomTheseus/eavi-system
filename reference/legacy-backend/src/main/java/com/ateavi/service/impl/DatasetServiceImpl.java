package com.ateavi.service.impl;

import com.ateavi.pojo.DTO.DatasetFilesScanResult;
import com.ateavi.pojo.DatasetDocument;
import com.ateavi.pojo.User;
import com.ateavi.pojo.vo.DatasetListVo;
import com.ateavi.pojo.vo.DatasetVo;
import com.ateavi.service.DatasetFilesScanService;
import com.ateavi.service.MarkdownProcessingService;
import com.ateavi.utils.Result;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ateavi.pojo.Dataset;
import com.ateavi.service.DatasetService;
import com.ateavi.mapper.DatasetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Value;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @author 联想
 * @description 针对表【dataset】的数据库操作Service实现
 * @createDate 2025-10-20 23:42:42
 */
@Service
public class DatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset>
        implements DatasetService {

    @Autowired
    private DatasetMapper datasetMapper;
    @Autowired
    private DatasetFilesScanService filesScanService;
    @Autowired
    private MarkdownProcessingService markdownProcessingService;

    @Value("${eavi.dataset.storage-path:e:/UI/eavi-static/data/datasets/}")
    private String storageRootPath;

    @Override
    public Result findNewsPage(DatasetListVo datasetListVo) {
        //这有大问题要改
        IPage<Map> page = new Page<>(datasetListVo.getPage(), datasetListVo.getPageSize());
        datasetMapper.selectMyPage(page, datasetListVo);
        List<Map> records = page.getRecords();
        Map data0 = new HashMap<>();
        data0.put("datasets", records);
        data0.put("page", page.getCurrent());
        data0.put("pageSize", page.getSize());
        data0.put("totalPages", page.getPages());
        data0.put("total", page.getTotal());
        Map pageInfo = new HashMap<>();
        pageInfo.put("pageInfo", data0);
        return Result.ok(pageInfo);
    }

    @Override
    public Result findDatasetDetail(Integer id) {
        DatasetVo datasetVo = datasetMapper.selectDataset(id);
        Map data = new HashMap<>();
        data.put("data", datasetVo);
        return Result.ok(data);
    }
    @Override
    public Result getMarkdownContent(Integer id) {
        DatasetVo datasetVo = datasetMapper.selectDataset(id);
        if(datasetVo==null){
            throw new RuntimeException("数据集文档不存在");
        }
        String basePath = datasetVo.getBaseAddress();
        DatasetFilesScanResult scanResult = filesScanService.scanDatasetFiles(basePath);

        if (scanResult.getMarkdownFile() == null) {
            throw new RuntimeException("数据集文档不存在");
        }

        try {
            // 读取Markdown文件内容以提取标题
            String markdownContent = Files.readString(Paths.get(scanResult.getMarkdownFile()));
            String title = markdownProcessingService.extractTitle(markdownContent);

            // 处理Markdown转HTML
            String htmlContent = markdownProcessingService.processMarkdownToHtml(
                    scanResult.getMarkdownFile(),
                    scanResult.getImageFiles(),
                    id,
                    basePath,
                    true
            );

            // 提取图片URL列表
            List<String> imageUrls = markdownProcessingService.extractImageUrls(htmlContent);

            // 获取文件信息
            Path markdownPath = Paths.get(scanResult.getMarkdownFile());
            LocalDateTime lastModified = Files.getLastModifiedTime(markdownPath)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            double fileSize = Files.size(markdownPath);

            // 获取相对路径
            DatasetDocument datasetDocument = new DatasetDocument(id,htmlContent,datasetVo.getDatasetName(),imageUrls,lastModified,fileSize);
            return Result.ok(datasetDocument);
        } catch (IOException e) {
            throw new RuntimeException("处理数据集文档失败", e);
        }
    }

    @Override
    public Resource getDatasetImage(Integer id, String imagePath) {
        DatasetVo datasetVo = datasetMapper.selectDataset(id);
        if(datasetVo==null){
            throw new RuntimeException("数据集文档不存在");
        }
        String basePath = datasetVo.getBaseAddress();
        DatasetFilesScanResult scanResult = filesScanService.scanDatasetFiles(basePath);

        if (scanResult.getMarkdownFile() == null) {
            throw new RuntimeException("数据集文档不存在");
        }
        try {
            String fullDatasetPath = buildDatasetFullPath(basePath);
            Path datasetBasePath = Paths.get(fullDatasetPath);
            Path imageFinalPath = datasetBasePath.resolve(imagePath).normalize();
            if (!imageFinalPath.startsWith(datasetBasePath)) {
                throw new IllegalArgumentException("非法的图片路径");
            }
            if (!Files.exists(imageFinalPath)) {
                throw new RuntimeException("图片文件不存在");
            }
            String fileName = imageFinalPath.getFileName().toString().toLowerCase();
            if (!isImageFile(fileName)) {
                throw new IllegalArgumentException("请求的文件不是图片格式");
            }
            return new FileSystemResource(imageFinalPath);
        }catch (Exception e){
            throw new RuntimeException("获取图片资源失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Result getDownloadLinks(Integer id) {
        return null;
    }

    /**
     * 构建数据集完整路径（与DatasetFilesScanService保持一致）
     * @param relativePath 数据集相对路径
     * @return 完整的文件系统路径
     */
    private String buildDatasetFullPath(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            throw new IllegalArgumentException("相对路径不能为空");
        }

        try {
            String rootPath = storageRootPath;

            // 处理classpath路径
            if (rootPath.startsWith("classpath:")) {
                return buildClasspathPath(rootPath, relativePath);
            }
            // 处理相对路径（相对于项目根目录）
            else if (rootPath.startsWith("./") || rootPath.startsWith(".\\")) {
                return buildRelativePath(rootPath, relativePath);
            }
            // 处理绝对路径
            else if (Paths.get(rootPath).isAbsolute()) {
                return buildAbsolutePath(rootPath, relativePath);
            }
            // 默认作为相对路径处理
            else {
                return buildRelativePath("./" + rootPath, relativePath);
            }

        } catch (Exception e) {
            throw new RuntimeException("构建文件路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建classpath路径
     */
    private String buildClasspathPath(String rootPath, String relativePath) {
        try {
            String classpathLocation = rootPath.substring("classpath:".length());

            // 尝试使用ResourceUtils获取文件
            try {
                File file = ResourceUtils.getFile(rootPath);
                Path basePath = file.toPath();
                Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
                return fullPath.normalize().toString();
            } catch (Exception e) {
                // 如果ResourceUtils失败，尝试使用ClassLoader
                URL resource = getClass().getClassLoader().getResource(classpathLocation);
                if (resource != null) {
                    Path basePath = Paths.get(resource.toURI());
                    Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
                    return fullPath.normalize().toString();
                }
                throw new RuntimeException("无法找到classpath资源: " + rootPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("处理classpath路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建相对路径（相对于项目根目录）
     */
    private String buildRelativePath(String rootPath, String relativePath) {
        try {
            // 获取项目根目录
            String projectRoot = System.getProperty("user.dir");

            // 移除rootPath中的"./"或".\\"前缀
            String cleanRootPath = rootPath;
            if (cleanRootPath.startsWith("./")) {
                cleanRootPath = cleanRootPath.substring(2);
            } else if (cleanRootPath.startsWith(".\\")) {
                cleanRootPath = cleanRootPath.substring(2);
            }

            // 构建完整路径
            Path basePath = Paths.get(projectRoot, cleanRootPath);
            Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
            return fullPath.normalize().toString();
        } catch (Exception e) {
            throw new RuntimeException("处理相对路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 构建绝对路径
     */
    private String buildAbsolutePath(String rootPath, String relativePath) {
        try {
            Path basePath = Paths.get(rootPath);
            Path fullPath = basePath.resolve(relativePath.replace("\\", "/"));
            return fullPath.normalize().toString();
        } catch (Exception e) {
            throw new RuntimeException("处理绝对路径失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查文件是否为图片格式
     * @param fileName 文件名
     * @return 是否为图片文件
     */
    private boolean isImageFile(String fileName) {
        String[] imageExtensions = {".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg", ".tiff", ".ico"};
        for (String ext : imageExtensions) {
            if (fileName.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }




}




