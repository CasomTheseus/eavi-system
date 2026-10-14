package com.ateavi.service.impl;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

/**
 * 测试路径匹配逻辑
 */
public class PathMatchingTest {

    @Test
    public void testPathMatching() {
        // 模拟DatasetFilesScanServiceImpl扫描到的图片文件路径（绝对路径）
        List<String> imageFiles = Arrays.asList(
            "f:\\Axidian\\其他工作\\UI\\EAVI_system\\src\\main\\resources\\data\\datasets\\VOC2007\\VOC_example.png",
            "f:\\Axidian\\其他工作\\UI\\EAVI_system\\src\\main\\resources\\data\\datasets\\VOC2007\\VOC_classes.png"
        );
        
        // 模拟Markdown中的图片引用路径
        String originalPath1 = "VOC_example.png";
        String originalPath2 = "VOC_classes.png";
        String originalPath3 = "./VOC_example.png";
        String originalPath4 = "images/VOC_example.png";
        
        String basePath = "f:\\Axidian\\其他工作\\UI\\EAVI_system\\src\\main\\resources\\data\\datasets\\VOC2007";
        
        MarkdownProcessingServiceImpl service = new MarkdownProcessingServiceImpl();
        
        System.out.println("测试路径匹配:");
        System.out.println("图片文件列表:");
        imageFiles.forEach(System.out::println);
        System.out.println();
        
        System.out.println("测试1 - 直接文件名: " + originalPath1);
        // 这里需要使用反射来调用私有方法，或者将方法改为public进行测试
        
        System.out.println("测试2 - 直接文件名: " + originalPath2);
        System.out.println("测试3 - 相对路径: " + originalPath3);
        System.out.println("测试4 - 子目录路径: " + originalPath4);
    }
}