package com.ateavi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;

/**
 * MarkdownProcessingServiceImpl 测试类
 * 主要测试路径处理功能
 */
@SpringBootTest
@TestPropertySource(properties = {
    "storage.root-path=f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets"
})
public class MarkdownProcessingServiceImplTest {

    private MarkdownProcessingServiceImpl markdownProcessingService;

    @BeforeEach
    void setUp() {
        markdownProcessingService = new MarkdownProcessingServiceImpl();
    }

    @Test
    void testProcessImagePathsWithVOC2007() {
        // 模拟VOC2007的Markdown内容
        String markdownContent = "# PASCAL VOC 2007\n\n" +
                "![VOC 数据集示例](VOC_example.png)\n\n" +
                "![VOC2007类别层级](VOC_classes.png)\n\n" +
                "这是一些测试内容。";

        // 模拟图片文件列表
        List<String> imageFiles = Arrays.asList(
                "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/VOC2007/VOC_example.png",
                "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/VOC2007/VOC_classes.png"
        );

        String basePath = "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/VOC2007";

        // 测试处理图片路径
        String result = markdownProcessingService.processImagePaths(markdownContent, imageFiles, basePath);

        System.out.println("原始内容:");
        System.out.println(markdownContent);
        System.out.println("\n处理后内容:");
        System.out.println(result);

        // 验证结果应该包含API路径
        assert result.contains("/api/dataset/image?path=");
    }

    @Test
    void testProcessImagePathsWithChinesePaths() {
        // 测试包含中文路径的情况
        String markdownContent = "![测试图片](测试图片.png)";

        List<String> imageFiles = Arrays.asList(
                "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/测试/测试图片.png"
        );

        String basePath = "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/测试";

        String result = markdownProcessingService.processImagePaths(markdownContent, imageFiles, basePath);

        System.out.println("中文路径测试:");
        System.out.println("原始: " + markdownContent);
        System.out.println("结果: " + result);
    }

    @Test
    void testProcessImagePathsWithMixedSeparators() {
        // 测试混合路径分隔符的情况
        String markdownContent = "![测试](images\\test.png)";

        List<String> imageFiles = Arrays.asList(
                "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/test/images/test.png"
        );

        String basePath = "f:/Axidian/其他工作/UI/EAVI_system/src/main/resources/data/datasets/test";

        String result = markdownProcessingService.processImagePaths(markdownContent, imageFiles, basePath);

        System.out.println("混合分隔符测试:");
        System.out.println("原始: " + markdownContent);
        System.out.println("结果: " + result);
    }
}