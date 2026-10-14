# reference/ —— 参考实现（⚠️ 读它，别抄它）

## 这是什么

`legacy-backend/` 是学长提供的 **Spring Boot 后端实现**，写于本项目的前一阶段。

它**大部分接口都能跑**，对于理解"数据该怎么组装成前端要的 JSON"很有价值 ——
本项目数据库的表结构就是对照它整理出来的。

**但它与当前前端并不完全一致，而且带有若干已知缺陷。**
它不是"标准答案"，只是"一份能跑的旧代码"。

---

## ⚠️ 使用边界

| 可以 | 不可以 |
|---|---|
| 读它理解数据语义（哪个查询返回什么结构） | 直接整包拷进 `backend/` |
| 参考它的依赖清单（`pom.xml`） | 沿用它的硬编码路径 |
| 参考它对 HTML→Markdown 的处理思路（`HtmlToMarkdownServiceImpl`） | 沿用它的实体类（缺主键注解） |
| 参考它对图片路径穿越的防护（`DatasetServiceImpl.getDatasetImage`） | 沿用它的权限逻辑（有越权） |

**一句话**：把它当"能跑的文档"，不是当"能用的代码"。

---

## 🔴 6 处与前端契约的硬冲突

这些是"照抄就会出错"的地方，每一处都在 `docs/01-api-contract.md` 第 6 节有对应条目。

| # | 冲突 | 参考实现的写法 | 你应该怎么做 |
|---|---|---|---|
| 1 | **`GET /auth/profile` 不存在** | 全项目搜不到 `profile` | **必须新增**：从 JWT 解析 userId 查库返回 |
| 2 | **登出 400** | `@RequestBody String token` 必填，前端不发 body | 改成 `required = false`，或从请求头取 |
| 3 | **报错是英文** | `ResultCodeEnum` 返回 `"usernameError"` / `"passwordError"` | 改为中文，否则会原样显示在中文界面上 |
| 4 | **`base_address` 为 NULL 时 500** | `buildFullPath()` 抛 `IllegalArgumentException("相对路径不能为空")` | 加 NULL 判断，返回 `code:404` + 中文提示 |
| 5 | **目录接口越权** | `if (userId != null) qw.eq(Folder::getUserId, userId)` —— userId 为 null 时返回**全部用户**的目录 | 从 JWT 强制取 userId |
| 6 | **MySQL 8 下 SQL 报错** | `DatasetMapper.xml` 的 `group by dataset.dataset_id`，select 列表含非聚合列 | 补全 `GROUP BY` 或用 `ANY_VALUE()` |

---

## 其他已知缺陷

### 硬编码路径（最要命）

```java
@Value("${eavi.dataset.storage-path:e:/UI/eavi-static/data/datasets/}")   // ❌
```

`application.yaml` 和 Java `@Value` 默认值**两处都写了路径**，改一处忘一处就会读到旧目录。
而且历史上有过三种不同的盘符路径。**新代码必须用相对路径 + 环境变量覆盖。**

### 打包会失败

```
mvn package -DskipTests
→ BUILD FAILURE
```

原因：`-DskipTests` **仍然会编译测试代码**，而
`src/test/java/.../MarkdownProcessingServiceImplTest.java` 调用的
`processImagePaths(...)` 方法签名已经变了（测试用 3 个参数，实际方法是 4 个参数且为 `private`）。

**解法**：用 `mvn package -Dmaven.test.skip=true`，或把测试改对。

### 实体类缺主键注解

7 个实体类既没有 `@TableId` 也没有名为 `id` 的字段：
`DatasetTask`、`FolderPaper`、`PaperCode`、`PaperDataset`、`PaperTask`、`Role`、`Status`。

MyBatis-Plus 启动时会打印 WARN（**不是异常**），`insert` 仍可工作但不回填主键；
**`selectById` / `updateById` / `deleteById` / `deleteBatchIds` 会不可用**。

详见 `docs/02-database-design.md` 第 6.4 节。

### 遗留表

`repository` 表与 `paper_code` 表功能重复。
`RepositoryServiceImpl` **没有被任何类注入**，`/papers/{id}/repositories`
实际走的是 `paper_code` 表。属于死代码。

### 无用的配置

`application.yaml` 里的 `table-prefix: news_` 是教学项目遗留。
所有实体都用 `@TableName` 显式指定了表名，所以不影响运行，但会误导。

---

## 凭据已清理

本目录下的 `src/main/resources/application.yaml` 原本包含**明文数据库密码**和
**JWT 签名密钥**，已替换为环境变量占位符：

```yaml
password: ${MYSQL_PASSWORD:CHANGE_ME}
tokenSignKey: ${JWT_SIGN_KEY:CHANGE_ME_TO_A_RANDOM_STRING}
```

**它现在不能直接启动**（这是刻意的）。如果你确实想在本地把这份旧代码跑起来做对照，
请自行提供这两个环境变量，**并且不要把你的真实密码提交回来**。

---

## ⚖️ 版权说明

这份代码是学长提供给本团队学习使用的，版权归原作者。
本仓库将其置于 `reference/` 目录下**仅作团队内部学习参考**，不代表团队的作品。

**如果你（后端同学）的实现是照抄它的，请在提交信息里注明来源。**
如果要对外展示或发布，建议先与学长确认。
