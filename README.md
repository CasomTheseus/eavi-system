# EAVI 团队数据管理系统

科研团队的数据集与文献管理平台。**前端与数据库已完成，后端待实现。**

| 层 | 技术栈 | 状态 | 负责人 |
|---|---|---|---|
| 前端 | Vue 3.3 + Vite 4 + vue-router 4 + axios | ✅ 已完成 |
| 数据库 | MySQL 8，14 张表 | ✅ 已完成 |
| **后端** | **Spring Boot 3 + MyBatis-Plus + MySQL** | ❌ **待实现** |

> 🔴 **本仓库是公开仓库。** 不要提交任何真实密码、密钥、或个人隐私数据。

---

## 0. 给 AI Agent 的任务提示（可直接复制）

如果你是用 AI 编码助手来写后端，把下面这段作为第一条指令：

```
我要在 eavi-system 仓库中实现后端，目录是 backend/。

请先按顺序读这三个文件：
1. README.md（本文件）—— 项目全貌与跨平台约定
2. docs/01-api-contract.md —— 接口契约，这是唯一权威版本
3. database/schema.sql —— 数据库表结构（14 张表，已建好，不要改）

然后读 docs/05-backend-todo.md 作为任务清单。

硬性要求：
- 技术栈固定为 Spring Boot 3.0.5 + MyBatis-Plus 3.5.3.1 + MySQL 8 + JDK 17
- 接口必须严格按 docs/01-api-contract.md 的字段名返回，不要"统一命名"
- 绝对不能硬编码盘符路径（如 e:/ 或 f:/）或数据库密码
- 参考实现在 reference/legacy-backend/，但它有已知缺陷，是为了理解数据语义，
  不是为了照抄。它的缺陷清单见 reference/README.md。
```

---

## 1. 五分钟跑起来（Windows）

> 三人的开发环境：前端同学和数据库同学用 **macOS**，后端同学（你）用 **Windows**。
> 下面以 Windows PowerShell 为主；macOS 的等价命令放在折叠说明里。

### 1.1 环境要求

| 工具 | 版本 | 说明 |
|---|---|---|
| JDK | **17** | Spring Boot 3 要求 17+。`java -version` 确认 |
| Maven | 3.8+ | 或用 IDEA 自带的 |
| MySQL | **8.0** | 必须 8.0，因为表结构用了生成列（5.7 也支持，但统一用 8） |
| Node.js | **18+** | 跑前端用。`node -v` 确认 |
| Git | 任意 | —— |

### 1.2 初始化数据库

```powershell
# 在仓库根目录执行
Get-Content .\database\schema.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
Get-Content .\database\seed.sql   -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
```

`database/schema.sql` 会 **DROP 并重建** `eavi_system` 库里的所有表。首次执行即可，之后不要重复跑（会清空数据）。

<details>
<summary>macOS / Linux 等价命令</summary>

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
mysql -u root -p eavi_system < database/check.sql   # 可选，完整性自检
```
</details>

跑完应该得到 **14 张表**，并且有 2 个演示账号：

| 用户名 | 密码 | 姓名 | 角色 |
|---|---|---|---|
| `lcy` | `123456` | 刘晨宇 | 管理员 |
| `qmn` | `123456` | 齐梦男 | 管理员 |

密码在库里存的是 MD5：`e10adc3949ba59abbe56e057f20f883e`。

### 1.3 启动前端

```powershell
cd frontend
npm install
npm run dev
```

打开 <http://localhost:3000>。**此时登录会失败**——因为后端还没写。这是预期行为。

> ⚠️ **`npm install` 必须在你的机器上跑，不要拷贝别人的 `node_modules`。**
> `node_modules` 里有平台专用二进制（esbuild 等），macOS 装好的拷到 Windows 会直接报错。

### 1.4 后端要监听在哪

```
http://localhost:8080
```

**端口必须是 8080**，因为前端所有请求都硬编码了这个地址（见 `frontend/src/composables/useAuth.js` 等）。若确实需要改端口，要同时改前端代码，请先和前端同学商量。

### 1.5 目录约定

| 目录 | 内容 | 你能改吗 |
|---|---|---|
| `frontend/` | Vue 3 前端 | ⚠️ **尽量不要改**（见第 5 节） |
| `database/` | 建库脚本 | ⚠️ 基本不改（见第 6 节） |
| `backend/` | **空目录，你的工作区** | ✅ 随便改 |
| `eavi-static/` | 后端要服务的静态资源（数据集文档、图片） | ❌ 只读 |
| `docs/` | 文档 | ✅ 可以补充，别删已有的 |
| `reference/` | 学长给的参考后端 | ❌ 只读 |

---

## 2. ⚠️ 跨平台铁律

这个项目上一版**因为硬编码路径而难以在多人之间流转**。原始代码里同时存在这些路径：

```
f:/Axidian/其他工作/UI/eavi-static/          ← 最早的机器
f:/Axidian/小组项目工作/UI/eavi-static/       ← 还是 f 盘，但换了目录
e:/UI/eavi-static/                          ← 另一台机器
```

而且这些路径**同时写在了两个地方**：`application.yaml` 配置文件和 Java 代码的 `@Value` 默认值里。改了一个忘了另一个，就会莫名其妙读到旧目录。**不要重蹈覆辙。**

### 2.1 禁止硬编码盘符路径

✅ **正确写法**——用相对路径 + 环境变量兜底：

```yaml
# backend/src/main/resources/application.yaml
eavi:
  static-root: ${EAVI_STATIC_ROOT:../eavi-static/}   # 相对 backend/ 目录
  dataset-storage: ${EAVI_STATIC_ROOT:../eavi-static/}data/datasets/
```

```java
// Java 里也不要写死默认值
@Value("${eavi.static-root}")
private String staticRoot;      // ✅ 只从配置读，不给 f:/ 这种默认值
```

❌ **错误写法**（原始代码的样子）：

```java
@Value("${eavi.dataset.storage-path:e:/UI/eavi-static/data/datasets/}")  // ❌
private String storageRootPath;
```

如果某台机器上相对路径不好使，用**环境变量**覆盖，不要改代码：

```powershell
$env:EAVI_STATIC_ROOT = "D:\workspace\eavi-system\eavi-static\"
```

**验证方法**：让数据库同学在自己的 macOS 上 `git clone` 后不改任何代码也能跑起来。做不到就说明还有硬编码。

### 2.2 禁止硬编码密码

数据库密码**只写在本机**，二选一：

**方式 A（推荐）**：环境变量
```powershell
$env:MYSQL_PASSWORD = "你的密码"
```
```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/eavi_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      username: root
      password: ${MYSQL_PASSWORD}
```

**方式 B**：本机私有配置文件（`.gitignore` 已排除，不会被提交）
```yaml
# backend/src/main/resources/application-local.yaml
spring:
  datasource:
    druid:
      password: 你的密码
```
启动时加 `--spring.profiles.active=local`。

> ⚠️ **历史教训**：这个项目的旧版本把明文密码提交进了 `application.yaml`，
> 甚至在 PowerShell 脚本里以 `-p密码` 的形式硬编码。本仓库已清理，**请不要再次引入**。
> 提交前自查：`git diff --cached | Select-String "password"`

### 2.3 换行符

已在 `.gitattributes` 里统一：**版本库中一律存 LF**，只有 `*.ps1` / `*.bat` 强制 CRLF（PowerShell 对纯 LF 的脚本会解析异常）。

你不需要做任何事。但如果你的编辑器弹窗问"要不要转换换行符"，**选不要**。

### 2.4 其他跨平台注意事项

| 事项 | 要求 |
|---|---|
| 路径拼接 | 用 `Paths.get(a, b)` / `new File(parent, child)`，**不要**手写 `"/"` 或 `"\\"` |
| 上传文件的文件名 | **不要用中文名**。旧代码里有个 `1768635308827_合成疼痛表情数据….pdf`，中文名在跨系统传输和 URL 编码时容易出问题。统一用 `时间戳_原文件名`（参考 `PaperCodeServiceImpl` 的做法） |
| 源码编码 | 全项目 UTF-8。IDEA 里确认 `File Encodings` 三处都是 UTF-8 |
| 时间 | JDBC URL 带 `serverTimezone=Asia/Shanghai` |
| 大小写敏感 | macOS 文件系统默认**不区分**大小写，Windows 也不区分，但**Linux 服务器区分**。文件名不要只靠大小写区分 |

---

## 3. 数据库已建好了什么

`database/schema.sql` 已定义 **14 张表**，全部 `InnoDB` + `utf8mb4`，含主外键、唯一约束、索引和中文注释。

```
role ──< user ──< dataset >── status
                │
                ├──< dataset_task >── task
                ├──< paper_task   >── task
                ├──< paper_code
                ├──< paper_dataset >── dataset
                ├──< result >── task, dataset
                └──< folder >──< folder_paper >── paper
```

一句话业务：**用户**管理**文献**与**数据集**；文献按 AI **任务**归类，关联**数据集**与**代码仓库**，并在数据集上记录 **Benchmark 结果**；用户可把文献放进树形**文件夹**收藏。

要点：

- `paper_code` 支持**一篇文献挂多个代码仓库**，唯一键 `(paper_id, code_name)`。
- `paper_dataset` 同时支持**库内数据集**（`dataset_id`）和**外部链接**（`dataset_url`），用生成列 `ref_code` + 唯一键在数据库层防止重复关联。
- `dataset.base_address` **可以为 NULL**，表示"该数据集只有元数据、本地文档未上架"。详情接口必须处理这种情况。
- `user` / `dataset` / `folder` 有 **逻辑删除**字段 `is_deleted`；`user` / `dataset` 有**乐观锁**字段 `version`。

完整设计说明见 **[docs/02-database-design.md](docs/02-database-design.md)**。

---

## 4. 后端要做什么

完整清单见 **[docs/05-backend-todo.md](docs/05-backend-todo.md)**，接口细节见 **[docs/01-api-contract.md](docs/01-api-contract.md)**。

摘要：

| 模块 | 端点数 | 说明 |
|---|---|---|
| `/auth` | 3 | 登录、登出、获取用户信息（**获取用户信息是新增的**） |
| `/datasets` | 5 | 列表、详情、Markdown 内容、图片、上传 |
| `/papers` | 12 | 列表、详情 + 任务/数据集/代码/笔记的增删 |
| `/folders` | 6 | 目录树、新建、加文献、删除（3 个兼容端点） |
| `/tasks` | 1 | 任务字典 |
| 静态资源 | 3 | 论文笔记 / 论文 / 代码文件的下载 |

技术栈建议沿用：**Spring Boot 3.0.5 + MyBatis-Plus 3.5.3.1 + Druid + jjwt + Jsoup + commonmark**（与参考实现一致，避免重新踩坑）。

---

## 5. 你会遇到的前端已知问题（不是你的锅，但会干扰你调试）

前端已冻结，有以下已知缺陷。**调试时如果发现是这些问题，请记录、不要自己去改前端**：

| 现象 | 原因 | 处理 |
|---|---|---|
| 数据集卡片状态显示英文 `undefined` | 前端漏映射了 `status` 字段 | 你正常返回 `status` 即可，前端同学后续修 |
| 登出时后端报 400 | 前端登出不发请求体 | 按契约把 `@RequestBody` 改成非必填 |
| 数据集列表页的分类/状态筛选点了没反应 | 前端筛选 UI 没接线 | 不用管 |
| 文献列表页"新增文献"按钮报路由错误 | 前端跳 `/upload`，路由不存在 | 不用管 |
| 资源页"查看详情"报错 | 前端路由名拼写不一致 | 不用管 |
| 面板暗色主题部分页面不生效 | 前端主题实现有三套 | 不用管 |

完整清单见 **[docs/04-frontend-known-issues.md](docs/04-frontend-known-issues.md)**。

---

## 6. ⚠️ 关于 `database/` 目录：只能有一份权威脚本

这个项目历史上有**四份互相矛盾的建库脚本**，导致"用哪份初始化"长期扯皮。**现在只认一份**：

```
✅ database/schema.sql   ← 唯一权威版本（14 张表）
✅ database/seed.sql     ← 配套演示数据
✅ database/check.sql    ← 完整性自检

📦 database/legacy/schema_v1_original.sql   ← 历史备份，仅供追溯，不要用
📦 database/legacy/seed_v1_original.sql     ← 历史备份，仅供追溯，不要用
```

`legacy/` 里那份是**已知有缺陷的旧版**（只有 9 张表，缺 `role` / `user` / `status` / `dataset_task` / `paper_code`，
用它初始化会**连登录都跑不通**）。保留它只是为了能查历史差异。

**如果你需要改表结构**（比如给项目模块加表），请：
1. 直接改 `database/schema.sql`，不要新建 `schema_v2.sql` / `schema_new.sql` / `schema_final.sql`；
2. 在提交信息里写清楚改了什么；
3. 同步更新 `docs/02-database-design.md`；
4. 知会数据库同学。

---

## 7. 目录结构

```
eavi-system/
├── README.md                    ← 本文件
├── frontend/                    ← Vue 3 前端（已完成，冻结）
│   ├── src/
│   │   ├── composables/useAuth.js      认证逻辑，调 /auth/*
│   │   ├── views/                      12 个页面
│   │   ├── components/                 Navbar、富文本编辑器
│   │   └── data/projects.js            项目模块的假数据 + localStorage CRUD
│   ├── public/                         静态资源（markdownTemplate.md 等）
│   ├── package.json / vite.config.js
│   └── node_modules/                    ⛔ 不入库，自己 npm install
│
├── backend/                     ← 📌 你的工作区（当前只有 README）
│
├── database/                    ← MySQL 脚本
│   ├── schema.sql                       14 张表，权威版本
│   ├── seed.sql                         演示数据
│   ├── check.sql                        完整性自检
│   └── legacy/                          旧版备份，不要用
│
├── eavi-static/                 ← 后端要服务的静态资源（只读）
│   └── data/
│       ├── datasets/
│       │   ├── VOC2007/                 唯一真正上架的数据集文档 + 图片
│       │   └── 123/                     测试遗留，可忽略
│       └── paper_note/                  论文笔记上传目录（内容不入库）
│
├── docs/
│   ├── 01-api-contract.md       ⭐ 接口契约（唯一权威）
│   ├── 02-database-design.md      数据库设计说明
│   ├── 03-cross-platform.md       跨平台约定详版
│   ├── 04-frontend-known-issues.md 前端已知问题
│   └── 05-backend-todo.md       ⭐ 你的任务清单
│
├── reference/
│   ├── README.md                ⭐ 先读这个：参考后端的使用边界
│   └── legacy-backend/          学长给的 Spring Boot 实现（有缺陷，勿照抄）
│
└── scripts/                     建库 / 启动脚本（macOS + Windows 双版本）
```

---

## 8. 验收标准

后端做完后，用这些步骤自测（**必须全部通过**）：

```
1. 全新 clone → 不改任何代码 → 能启动
2. 用 lcy / 123456 从前端登录页登录成功
3. 刷新页面后仍是登录状态（/auth/profile 生效）
4. 数据集列表出现 5 条，其中 PASCAL VOC 2007 能点进详情并渲染出 Markdown 正文和图片
5. 点击 ImageNet（base_address 为 NULL）不报 500，而是显示"尚未上架"的中文提示
6. 上传一个新数据集（含图片），能在列表和详情里看到
7. 文献列表出现 2 条，能进入详情看到任务/代码/数据集/结果四个区块
8. 能给文献加任务、加代码、加数据集关联，并能删除
9. 文献目录树正确显示"我的文库 → 精读/略读 → 计算机视觉/自然语言处理"三层结构
10. 删除一个子目录后，其中的文献回到"我的文库"
11. 用 qmn 登录，只能看到 qmn 自己的目录（不能看到 lcy 的）
12. 上传一次论文阅读笔记，能通过 /data/paper_note/xxx 打开
13. 让数据库同学在 macOS 上 clone 一次，重复第 1 步能启动
```

第 11 条是**权限验收**，最容易漏，务必测。

---

## 9. 协作约定

- **不要提交**：`node_modules/`、`dist/`、`target/`、`*.log`、`.idea/`、`application-local.yaml`（`.gitignore` 已配置）
- **提交信息**：中文/英文都行，但要说明改了什么、为什么
- **改动前端前**：先和前端同学沟通。前端是别人的工作成果，且已完成。
- **遇到契约不明**：以 `docs/01-api-contract.md` 为准；如果发现契约本身有问题，**先提出来讨论再改**，不要单方面改前端。
- **有疑问**：在仓库 Issues 里记录，比在群里刷消息更好追溯。
