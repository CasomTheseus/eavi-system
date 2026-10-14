package com.ateavi.service;

import com.ateavi.pojo.DTO.DatasetSaveResult;
import com.ateavi.pojo.Dataset;
import com.ateavi.pojo.vo.DatasetVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName:HtmlToMarkdownService
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/11/1 16:40
 * @Version 1.0
 */
public interface HtmlToMarkdownService {
    /**
     * 将HTML内容转换为Markdown格式
     * @param htmlContent HTML内容
     * @param datasetName 数据集名称（用于文件夹命名）
     * @return 转换后的Markdown内容
     */
    String convertHtmlToMarkdown(String htmlContent, String datasetName);

    /**
     * 保存数据集的Markdown文件和图片
     * @param basicInfo 数据集基本信息
     * @param htmlContent HTML内容
     * @param images 图片文件列表
     * @param files 其他文件列表
     * @return 保存的文件路径信息
     */
    Dataset saveDatasetFiles(DatasetVo basicInfo, String htmlContent,
                             MultipartFile[] images, MultipartFile[] files, String token);

    Dataset saveDatasetMarkdownFile(DatasetVo basicInfo, MultipartFile markdownFile,
                                    MultipartFile[] images, MultipartFile[] files, String token);

    /**
     * 保存数据集到数据库
     * @param saveResult 文件保存结果
     * @return 保存后的数据集ID
     */
    Integer saveDatasetToDatabase(Dataset saveResult);

    /**
     * 处理HTML中的图片标签，转换为Markdown格式
     * @param htmlContent HTML内容
     * @param datasetName 数据集名称
     * @param imageMapping 图片映射关系
     * @return 处理后的HTML内容
     */
    String processImagesInHtml(String htmlContent, String datasetName, java.util.Map<String, String> imageMapping);

}
