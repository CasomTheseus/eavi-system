# eavi-static/ —— 后端要服务的静态资源目录

这个目录**不属于任何一层代码**，而是运行时被后端读取的数据目录。

```
eavi-static/
└── data/
    ├── datasets/                     数据集文档（Markdown + 图片）
    │   ├── VOC2007/                  ← 唯一真正上架的数据集
    │   │   ├── VOC2007.md
    │   │   ├── VOC_classes.png
    │   │   └── VOC_example.png
    │   └── 123/                      ← 测试遗留，可忽略
    │       ├── 123.md                （内容是空模板）
    │       ├── VOC_classes.png
    │       └── VOC_example.png
    ├── paper_note/                   论文阅读笔记（用户上传）
    ├── paper/                        论文文件（预留）
    └── paper_code/                   文献代码文件（用户上传）
```

## 后端如何使用

1. **数据集详情**：`dataset.base_address` 存的是 `datasets/` 下的**目录名**
   （例如 `VOC2007`）。后端拼接 `datasets/<base_address>/` 读取里面的 `.md` 和图片。
2. **静态资源映射**：把本目录挂成 Spring 的静态资源根，让
   `/data/paper_note/xxx.pdf` 这类 URL 能直接取到文件。

```yaml
spring:
  web:
    resources:
      static-locations:
        - file:${EAVI_STATIC_ROOT:../eavi-static/}
        - classpath:/static/
```

## ⚠️ 关于路径

**不要修改代码里指向本目录的写法为绝对路径。**
原始代码把路径写死成 `e:/UI/eavi-static/`，导致换台机器就跑不起来。
正确做法是相对路径 + 环境变量覆盖，详见 [../docs/03-cross-platform.md](../docs/03-cross-platform.md)。

## 关于测试遗留目录

以下目录**在数据库中没有任何数据集的 `base_address` 指向它们**，属于孤儿目录：

| 目录 | 状态 |
|---|---|
| `datasets/123/` | `123.md` 内容只是空模板，图片是从 VOC2007 复制过去的 |
| `datasets/1/` | 完全空目录 |
| `data/docs/` | 完全空目录（原始配置里有一个未被使用的 `docs-path`） |

保留它们是为了**忠实反映真实数据目录的状态**，不是设计意图。
如果确认无用，可以删除，不会影响任何功能。

## 为什么不收录论文笔记 PDF

`data/paper_note/` 原本有一个 748 KB 的论文 PDF
（`1768635308827_合成疼痛表情数据：AI驱动临床评估新范式.pdf`），
属于团队测试上传的**已发表论文**。

**本仓库是公开仓库，收录他人已发表的论文 PDF 有版权风险，因此未入库。**
详见 [data/paper_note/README.md](data/paper_note/README.md)。

后端同学测试笔记上传功能时，请自行用一个无版权顾虑的文件。
