package com.ateavi.service.impl;

import com.ateavi.mapper.DatasetMapper;
import com.ateavi.pojo.DTO.DatasetSaveResult;
import com.ateavi.pojo.Dataset;
import com.ateavi.pojo.vo.DatasetVo;
import com.ateavi.service.HtmlToMarkdownService;
import com.ateavi.utils.JwtHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ClassName:HtmlToMarkdownServiceImpl
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/11/1 16:41
 * @Version 1.0
 */
@Service
public class HtmlToMarkdownServiceImpl implements HtmlToMarkdownService {
    @Autowired
    private JwtHelper jwtHelper;

    @Value("${eavi.dataset.storage-path:e:/UI/eavi-static/data/datasets/}")
    private String storageRootPath;

    @Autowired
    private DatasetMapper datasetMapper;
    @Override
    public String convertHtmlToMarkdown(String htmlContent, String datasetName) {
        try {
            // 解析HTML
            Document doc = Jsoup.parse(htmlContent);

            // 转换为Markdown
            StringBuilder markdown = new StringBuilder();

            // 处理文档结构
            processElement(doc.body(), markdown, 0);

            // 清理和格式化Markdown
            String result = cleanupMarkdown(markdown.toString());

            return result;

        } catch (Exception e) {
            throw new RuntimeException("HTML转Markdown转换失败: " + e.getMessage());
        }
    }

    @Override
    public Dataset saveDatasetFiles(DatasetVo basicInfo, String htmlContent, MultipartFile[] images, MultipartFile[] files, String token) {
        String datasetName = basicInfo.getDatasetName();
        try {
            // 创建数据集目录
            Path datasetDir = createDatasetDirectory(datasetName);

            // 保存图片文件并建立映射关系
            Map<String, String> imageMapping = saveImages(images, datasetDir, datasetName);

            // 保存其他数据集文件
            List<String> datasetFiles = saveDatasetFiles(files, datasetDir, datasetName);

            // 处理HTML中的图片引用
            String processedHtml = processImagesInHtml(htmlContent, datasetName, imageMapping);

            // 转换为Markdown
            String markdownContent = convertHtmlToMarkdown(processedHtml, datasetName);

            // 在Markdown开头添加基本信息
            String markdownWithMeta = addDatasetMetadata(markdownContent, basicInfo);

            // 保存Markdown文件
            Path markdownFile = saveMarkdownFile(markdownWithMeta, datasetDir, datasetName);

            Integer userId = Math.toIntExact(jwtHelper.getUserId(token));
            // 构建返回结果
            Dataset result = new Dataset();
            result.setDatasetId(null); // 需要在保存到数据库后设置
            result.setDatasetName(datasetName);
            result.setDescription(basicInfo.getDescription());
            result.setBaseAddress(datasetDir.getFileName().toString()); // 只返回基地址（目录名）
            result.setTrueAddress(basicInfo.getTrueAddress());
            result.setSizeFormatted(basicInfo.getSizeFormatted());
            result.setCreatedTime(new Date());
            result.setUpdatedTime(new Date());
            result.setUserId(userId);

            return result;

        } catch (Exception e) {
            throw new RuntimeException("保存数据集文件失败: " + e.getMessage());
        }
    }

    @Override
    public Dataset saveDatasetMarkdownFile(DatasetVo basicInfo, MultipartFile markdownFile,
                                           MultipartFile[] images, MultipartFile[] files, String token) {
        String datasetName = basicInfo.getDatasetName();
        try {
            Path datasetDir = createDatasetDirectory(datasetName);
            Map<String, String> imageMapping = saveImages(images, datasetDir, datasetName);
            List<String> datasetFiles = saveDatasetFiles(files, datasetDir, datasetName);

            String fileName = datasetName + ".md";
            Path markdownPath = datasetDir.resolve(fileName);
            if (markdownFile != null && !markdownFile.isEmpty()) {
                Files.copy(markdownFile.getInputStream(), markdownPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.write(markdownPath, "".getBytes("UTF-8"));
            }

            if (imageMapping != null && !imageMapping.isEmpty() && Files.exists(markdownPath)) {
                String original = Files.readString(markdownPath);
                Pattern imgPattern = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = imgPattern.matcher(original);
                StringBuffer processed = new StringBuffer();
                while (matcher.find()) {
                    String alt = matcher.group(1);
                    String path = matcher.group(2);
                    String normalized = path.replace("\\\\", "/");
                    int last = normalized.lastIndexOf('/');
                    String fileBase = last >= 0 ? normalized.substring(last + 1) : normalized;
                    String mapped = imageMapping.get(fileBase);
                    if (mapped != null && !mapped.isEmpty()) {
                        String replacement = "![" + alt + "](" + mapped + ")";
                        matcher.appendReplacement(processed, Matcher.quoteReplacement(replacement));
                    } else {
                        matcher.appendReplacement(processed, Matcher.quoteReplacement(matcher.group(0)));
                    }
                }
                matcher.appendTail(processed);
                Files.write(markdownPath, processed.toString().getBytes("UTF-8"));
            }

            Integer userId = Math.toIntExact(jwtHelper.getUserId(token));
            Dataset result = new Dataset();
            result.setDatasetId(null);
            result.setDatasetName(datasetName);
            result.setDescription(basicInfo.getDescription());
            result.setBaseAddress(datasetDir.getFileName().toString());
            result.setTrueAddress(basicInfo.getTrueAddress());
            result.setSizeFormatted(basicInfo.getSizeFormatted());
            result.setCreatedTime(new Date());
            result.setUpdatedTime(new Date());
            result.setUserId(userId);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("保存Markdown文件失败: " + e.getMessage());
        }
    }

    @Override
    public Integer saveDatasetToDatabase(Dataset saveResult) {
        try {
            // 检查基地址是否已存在
            if (datasetMapper.existsByBaseAddress(saveResult.getBaseAddress())) {
                throw new RuntimeException("数据集基地址已存在，请使用不同的名称");
            }

            // 计算文件大小和数量（这里需要根据实际文件计算）
            calculateDatasetMetrics(saveResult, saveResult.getBaseAddress());

            // 保存到数据库
            int insertResult = datasetMapper.insert(saveResult);
            if (insertResult > 0) {
                return saveResult.getDatasetId();
            } else {
                throw new RuntimeException("数据集保存到数据库失败");
            }

        } catch (Exception e) {
            throw new RuntimeException("保存数据集到数据库失败: " + e.getMessage());
        }
    }

    /**
     * 计算数据集的文件大小和数量
     * @param dataset 数据集实体
     * @param baseAddress 基地址
     */
    private void calculateDatasetMetrics(Dataset dataset, String baseAddress) {
        try {
            Path datasetPath = Paths.get(storageRootPath, baseAddress);
            if (Files.exists(datasetPath)) {
                final long[] totalSize = {0};
                final int[] fileCount = {0};

                // 遍历目录计算大小和文件数量
                Files.walk(datasetPath)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            try {
                                long size = Files.size(file);
                                totalSize[0] += size;
                                fileCount[0]++;
                            } catch (IOException e) {

                            }
                        });

                if (dataset.getSizeFormatted() == null || dataset.getSizeFormatted().trim().isEmpty()) {
                    dataset.setSizeFormatted(formatFileSize(totalSize[0]));
                }
                dataset.setFileCount(fileCount[0]);

            } else {
                if (dataset.getSizeFormatted() == null || dataset.getSizeFormatted().trim().isEmpty()) {
                    dataset.setSizeFormatted("0 B");
                }
                dataset.setFileCount(0);
            }
        } catch (Exception e) {
            if (dataset.getSizeFormatted() == null || dataset.getSizeFormatted().trim().isEmpty()) {
                dataset.setSizeFormatted("0 B");
            }
            dataset.setFileCount(0);
        }
    }

    /**
     * 格式化文件大小显示
     * @param size 文件大小（字节）
     * @return 格式化后的大小字符串
     */
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }

    @Override
    public String processImagesInHtml(String htmlContent, String datasetName, Map<String, String> imageMapping) {

        Document doc = Jsoup.parse(htmlContent);
        Elements images = doc.select("img");

        for (Element img : images) {
            String src = img.attr("src");
            String alt = img.attr("alt");

            // 如果是base64图片或临时图片，替换为实际文件路径
            if (src.startsWith("data:image/") || src.startsWith("blob:")) {
                // 查找对应的图片文件
                String imageName = findImageByAlt(alt, imageMapping);
                if (imageName != null) {
                    img.attr("src", imageName);
                }
            } else if (imageMapping.containsKey(src)) {
                // 直接映射替换
                img.attr("src", imageMapping.get(src));
            }
        }

        return doc.html();
    }


    /**
     * 处理HTML元素，转换为Markdown
     */
    private void processElement(Element element, StringBuilder markdown, int depth) {
        for (Node node : element.childNodes()) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                String text = textNode.text().trim();
                if (!text.isEmpty()) {
                    markdown.append(text);
                }
            } else if (node instanceof Element) {
                Element childElement = (Element) node;
                processHtmlElement(childElement, markdown, depth);
            }
        }
    }

    private void processHtmlElement(Element element, StringBuilder markdown, int depth) {
        String tagName = element.tagName().toLowerCase();

        switch (tagName) {
            case "h1":
                markdown.append("\n# ").append(element.text()).append("\n\n");
                break;
            case "h2":
                markdown.append("\n## ").append(element.text()).append("\n\n");
                break;
            case "h3":
                markdown.append("\n### ").append(element.text()).append("\n\n");
                break;
            case "h4":
                markdown.append("\n#### ").append(element.text()).append("\n\n");
                break;
            case "h5":
                markdown.append("\n##### ").append(element.text()).append("\n\n");
                break;
            case "h6":
                markdown.append("\n###### ").append(element.text()).append("\n\n");
                break;
            case "p":
                markdown.append("\n").append(processInlineElements(element)).append("\n\n");
                break;
            case "strong":
            case "b":
                markdown.append("**").append(element.text()).append("**");
                break;
            case "em":
            case "i":
                markdown.append("*").append(element.text()).append("*");
                break;
            case "u":
                markdown.append("<u>").append(element.text()).append("</u>");
                break;
            case "strike":
            case "s":
                markdown.append("~~").append(element.text()).append("~~");
                break;
            case "code":
                if (element.parent().tagName().equals("pre")) {
                    // 代码块
                    String language = element.attr("class").replaceFirst("language-", "");
                    markdown.append("\n```").append(language).append("\n")
                            .append(element.text()).append("\n```\n\n");
                } else {
                    // 内联代码
                    markdown.append("`").append(element.text()).append("`");
                }
                break;
            case "pre":
                if (!element.select("code").isEmpty()) {
                    // 已在code标签中处理
                    processElement(element, markdown, depth);
                } else {
                    markdown.append("\n```\n").append(element.text()).append("\n```\n\n");
                }
                break;
            case "blockquote":
                String[] lines = element.text().split("\n");
                for (String line : lines) {
                    markdown.append("\n> ").append(line);
                }
                markdown.append("\n\n");
                break;
            case "ul":
                processListElement(element, markdown, depth, false);
                break;
            case "ol":
                processListElement(element, markdown, depth, true);
                break;
            case "li":
                // 在processListElement中处理
                break;
            case "a":
                String href = element.attr("href");
                String text = element.text();
                markdown.append("[").append(text).append("](").append(href).append(")");
                break;
            case "img":
                String src = element.attr("src");
                String alt = element.attr("alt");
                markdown.append("![").append(alt).append("](").append(src).append(")");
                break;
            case "br":
                markdown.append("  \n");
                break;
            case "hr":
                markdown.append("\n---\n\n");
                break;
            case "table":
                processTableElement(element, markdown);
                break;
            case "span":
                // 处理颜色标签
                String style = element.attr("style");
                if (style.contains("color:")) {
                    String color = extractColor(style);
                    if (color != null) {
                        markdown.append("<span style=\"color:").append(color).append("\">")
                                .append(element.text()).append("</span>");
                    } else {
                        markdown.append(element.text());
                    }
                } else {
                    markdown.append(element.text());
                }
                break;
            case "div":
                // 检查是否是文件树结构
                if (element.hasClass("file-tree") || element.attr("data-type").equals("file-tree")) {
                    processFileTreeElement(element, markdown);
                } else {
                    markdown.append("\n");
                    processElement(element, markdown, depth);
                    markdown.append("\n");
                }
                break;
            default:
                // 其他元素，递归处理子元素
                processElement(element, markdown, depth);
                break;
        }
    }

    /**
     * 处理内联元素
     */
    private String processInlineElements(Element element) {
        StringBuilder result = new StringBuilder();

        for (Node node : element.childNodes()) {
            if (node instanceof TextNode) {
                result.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                Element childElement = (Element) node;
                String tagName = childElement.tagName().toLowerCase();

                switch (tagName) {
                    case "strong":
                    case "b":
                        result.append("**").append(childElement.text()).append("**");
                        break;
                    case "em":
                    case "i":
                        result.append("*").append(childElement.text()).append("*");
                        break;
                    case "u":
                        result.append("<u>").append(childElement.text()).append("</u>");
                        break;
                    case "strike":
                    case "s":
                        result.append("~~").append(childElement.text()).append("~~");
                        break;
                    case "code":
                        result.append("`").append(childElement.text()).append("`");
                        break;
                    case "a":
                        String href = childElement.attr("href");
                        result.append("[").append(childElement.text()).append("](").append(href).append(")");
                        break;
                    case "img":
                        String src = childElement.attr("src");
                        String alt = childElement.attr("alt");
                        result.append("![").append(alt).append("](").append(src).append(")");
                        break;
                    case "span":
                        String style = childElement.attr("style");
                        if (style.contains("color:")) {
                            String color = extractColor(style);
                            if (color != null) {
                                result.append("<span style=\"color:").append(color).append("\">")
                                        .append(childElement.text()).append("</span>");
                            } else {
                                result.append(childElement.text());
                            }
                        } else {
                            result.append(childElement.text());
                        }
                        break;
                    case "br":
                        result.append("  \n");
                        break;
                    default:
                        result.append(childElement.text());
                        break;
                }
            }
        }

        return result.toString();
    }

    /**
     * 处理列表元素
     */
    private void processListElement(Element listElement, StringBuilder markdown, int depth, boolean ordered) {
        Elements listItems = listElement.children();
        int counter = 1;

        for (Element li : listItems) {
            markdown.append("\n");

            // 添加缩进
            for (int i = 0; i < depth; i++) {
                markdown.append("  ");
            }

            if (ordered) {
                markdown.append(counter++).append(". ");
            } else {
                markdown.append("- ");
            }

            // 处理列表项内容
            processElement(li, markdown, depth + 1);
        }

        markdown.append("\n");
    }

    /**
     * 处理表格元素
     */
    private void processTableElement(Element tableElement, StringBuilder markdown) {
        Elements rows = tableElement.select("tr");

        if (rows.isEmpty()) {
            return;
        }

        markdown.append("\n");

        // 处理表头
        Element headerRow = rows.first();
        Elements headerCells = headerRow.select("th, td");

        markdown.append("|");
        for (Element cell : headerCells) {
            markdown.append(" ").append(cell.text()).append(" |");
        }
        markdown.append("\n");

        // 添加分隔行
        markdown.append("|");
        for (int i = 0; i < headerCells.size(); i++) {
            markdown.append(" --- |");
        }
        markdown.append("\n");

        // 处理数据行
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td");

            markdown.append("|");
            for (Element cell : cells) {
                markdown.append(" ").append(cell.text()).append(" |");
            }
            markdown.append("\n");
        }

        markdown.append("\n");
    }

    /**
     * 处理文件树元素
     */
    private void processFileTreeElement(Element treeElement, StringBuilder markdown) {
        markdown.append("\n```\n");

        // 递归处理文件树节点
        processFileTreeNode(treeElement, markdown, 0);

        markdown.append("```\n\n");
    }

    /**
     * 递归处理文件树节点
     */
    private void processFileTreeNode(Element node, StringBuilder markdown, int depth) {
        Elements children = node.children();

        for (int i = 0; i < children.size(); i++) {
            Element child = children.get(i);
            String nodeName = child.attr("data-name");
            String nodeType = child.attr("data-type");
            boolean isLast = (i == children.size() - 1);

            if (nodeName != null && !nodeName.isEmpty()) {
                // 添加缩进
                for (int j = 0; j < depth; j++) {
                    markdown.append("│   ");
                }

                // 添加树形符号，最后一个子节点使用└──，其他使用├──
                String treeSymbol = isLast ? "└── " : "├── ";

                if ("folder".equals(nodeType)) {
                    markdown.append(treeSymbol).append(nodeName).append("/");
                } else {
                    markdown.append(treeSymbol).append(nodeName);
                }

                // 添加注释（如果有）
                String comment = child.attr("data-comment");
                if (comment != null && !comment.isEmpty()) {
                    markdown.append("\t# ").append(comment);
                }

                markdown.append("\n");

                // 如果不是最后一个节点且有子节点，添加分隔线
                if (!isLast && child.children().size() > 0) {
                    for (int j = 0; j <= depth; j++) {
                        markdown.append("│");
                        if (j < depth) {
                            markdown.append("   ");
                        }
                    }
                    markdown.append("\n");
                }

                // 递归处理子节点
                processFileTreeNode(child, markdown, depth + 1);
            }
        }
    }

    /**
     * 提取颜色值
     */
    private String extractColor(String style) {
        Pattern colorPattern = Pattern.compile("color:\\s*([^;]+)");
        Matcher matcher = colorPattern.matcher(style);

        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    /**
     * 清理和格式化Markdown
     */
    private String cleanupMarkdown(String markdown) {
        // 移除多余的空行
        markdown = markdown.replaceAll("\n{3,}", "\n\n");

        // 确保标题前后有空行
        markdown = markdown.replaceAll("([^\n])\n(#{1,6}\\s)", "$1\n\n$2");
        markdown = markdown.replaceAll("(#{1,6}\\s[^\n]+)\n([^\n#])", "$1\n\n$2");

        // 清理首尾空白
        markdown = markdown.trim();

        return markdown;
    }

    /**
     * 创建数据集目录
     */
    private Path createDatasetDirectory(String datasetName) throws IOException {
        // 解析存储根路径
        String rootPath = resolveStoragePath();
        Path datasetDir = Paths.get(rootPath, datasetName);

        // 创建目录
        Files.createDirectories(datasetDir);

        return datasetDir;
    }

    /**
     * 保存图片文件
     */
    private Map<String, String> saveImages(MultipartFile[] images, Path datasetDir, String datasetName) throws IOException {
        Map<String, String> imageMapping = new HashMap<>();

        if (images == null || images.length == 0) {
            return imageMapping;
        }

        for (MultipartFile image : images) {
            if (image.isEmpty()) {
                continue;
            }

            String originalFilename = image.getOriginalFilename();
            if (originalFilename == null) {
                continue;
            }

            // 生成唯一文件名
            String fileName = generateUniqueFileName(originalFilename, datasetDir);
            Path imagePath = datasetDir.resolve(fileName);

            // 保存文件
            Files.copy(image.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);

            // 建立映射关系
            imageMapping.put(originalFilename, fileName);

        }

        return imageMapping;
    }

    /**
     * 保存其他数据集文件
     */
    private List<String> saveDatasetFiles(MultipartFile[] files, Path datasetDir, String datasetName) throws IOException {
        List<String> savedFiles = new ArrayList<>();

        if (files == null || files.length == 0) {
            return savedFiles;
        }

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                continue;
            }

            // 生成唯一文件名
            String fileName = generateUniqueFileName(originalFilename, datasetDir);
            Path filePath = datasetDir.resolve(fileName);

            // 保存文件
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            savedFiles.add(fileName);

        }

        return savedFiles;
    }

    /**
     * 在Markdown内容开头添加数据集元数据
     */
    private String addDatasetMetadata(String markdownContent, DatasetVo basicInfo) {
        // 直接返回原始markdown内容，不添加任何metadata头部
        return markdownContent;
    }

    /**
     * 保存Markdown文件
     */
    private Path saveMarkdownFile(String markdownContent, Path datasetDir, String datasetName) throws IOException {
        String fileName = datasetName + ".md";
        Path markdownPath = datasetDir.resolve(fileName);

        Files.write(markdownPath, markdownContent.getBytes("UTF-8"));

        return markdownPath;
    }

    /**
     * 生成唯一文件名
     */
    private String generateUniqueFileName(String originalFilename, Path directory) {
        String baseName = getFileBaseName(originalFilename);
        String extension = getFileExtension(originalFilename);

        Path targetPath = directory.resolve(originalFilename);
        if (!Files.exists(targetPath)) {
            return originalFilename;
        }

        int counter = 1;
        String newFileName;
        do {
            newFileName = baseName + "_" + counter + "." + extension;
            targetPath = directory.resolve(newFileName);
            counter++;
        } while (Files.exists(targetPath));

        return newFileName;
    }

    /**
     * 获取文件基名
     */
    private String getFileBaseName(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(0, lastDotIndex);
        }
        return filename;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    /**
     * 根据alt属性查找图片
     */
    private String findImageByAlt(String alt, Map<String, String> imageMapping) {
        for (Map.Entry<String, String> entry : imageMapping.entrySet()) {
            if (entry.getKey().contains(alt) || alt.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * 解析存储路径
     */
    private String resolveStoragePath() {
        if (storageRootPath.startsWith("classpath:")) {
            // 处理classpath路径
            String relativePath = storageRootPath.substring("classpath:".length());
            return System.getProperty("user.dir") + "/src/main/resources/" + relativePath;
        } else {
            return storageRootPath;
        }
    }
}
