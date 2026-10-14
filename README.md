# EAVI 团队数据管理系统

科研团队的数据集与文献管理平台。

| 层 | 技术栈 | 状态 |
|---|---|---|
| 前端 | Vue 3.3 / Vite 4 / vue-router 4 / axios | 已完成 |
| 数据库 | MySQL 8，14 张表 | 已完成 |
| 后端 | Spring Boot 3 / MyBatis-Plus / MySQL | 待实现 |

仓库是公开的，**不要提交真实密码、密钥或个人隐私数据**。

---

## 关于本文件

后端部分目前是空的，`backend/` 下只有一个 README。接手时建议按这个顺序读：

1. 本文件——项目全貌、启动方式、跨平台注意事项
2. `docs/01-api-contract.md`——接口契约，字段名以它为准
3. `docs/05-backend-todo.md`——任务清单，按阶段推进
4. `database/schema.sql`——表结构，已建好，不要改动

如果打算用 AI 助手来做，下面这段可以直接作为第一条指令：

```
我要在 eavi-system 仓库中实现后端，代码放在 backend/ 目录。

请先按顺序读这几个文件：
1. README.md              项目全貌与跨平台约定
2. docs/01-api-contract.md 接口契约，这是唯一权威版本
3. database/schema.sql     数据库表结构，14 张表，已建好，不要改
4. docs/05-backend-todo.md 任务清单

几条要求：
- 技术栈固定为 Spring Boot 3.0.5 + MyBatis-Plus 3.5.3.1 + MySQL 8 + JDK 17
- 接口返回的字段名必须和 docs/01-api-contract.md 完全一致，不要自行统一命名
- 不要硬编码盘符路径（如 e:/ 或 f:/），也不要写死数据库密码
- reference/legacy-backend/ 是学长给的参考实现，用来理解数据语义，
  不要照抄。它的已知问题列在 reference/README.md
```

---

## 一、把环境跑起来

### 环境要求

| 工具 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | Spring Boot 3 要求 17 及以上，用 `java -version` 确认 |
| Maven | 3.8+ | 用 IDEA 自带的也可以 |
| MySQL | 8.0 | 表结构里用了生成列，建议统一 8.0 |
| Node.js | 18+ | 跑前端用，`node -v` 确认 |
| Git | 任意 | |

### 初始化数据库

在仓库根目录执行：

```powershell
Get-Content .\database\schema.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
Get-Content .\database\seed.sql   -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
```

macOS 或 Linux 下等价的做法：

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
mysql -u root -p eavi_system < database/check.sql   # 可选，完整性自检
```

`schema.sql` 会 DROP 并重建 `eavi_system` 库里的所有表，跑一次就够，之后重复执行会清空数据。

完成后应该有 14 张表，以及两个演示账号：

| 用户名 | 密码 | 姓名 | 角色 |
|---|---|---|---|
| lcy | 123456 | 刘晨宇 | 管理员 |
| qmn | 123456 | 齐梦男 | 管理员 |

库里存的是密码的 MD5 值 `e10adc3949ba59abbe56e057f20f883e`。

### 启动前端

```powershell
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:3000。这时候登录会失败，因为后端还没写，属于正常现象。

`npm install` 要在自己机器上跑，不要拷贝别人的 `node_modules`。里面有平台相关的二进制文件（esbuild 之类），在 macOS 上装好再拷到 Windows 会直接报错。

### 后端端口

后端监听 `http://localhost:8080`。

这个端口不要改。前端所有请求都硬编码了这个地址（见 `frontend/src/composables/useAuth.js` 等），改端口就得同步改前端。确实需要改的话，先和相关负责人确认。

### 各目录的改动权限

| 目录 | 内容 | 是否可改 |
|---|---|---|
| `frontend/` | Vue 3 前端 | 尽量不要改，原因见第五节 |
| `database/` | 建库脚本 | 基本不改，规则见第六节 |
| `backend/` | 空目录，工作区 | 随便改 |
| `eavi-static/` | 后端要服务的静态资源 | 只读 |
| `docs/` | 文档 | 可以补充，不要删已有的 |
| `reference/` | 参考实现 | 只读 |

---

## 二、跨平台注意事项

项目上一版在几个人之间流转的时候出过不少问题，原因集中在两处：路径写死、配置里带密码。这一节是踩过的坑，接手时注意避开。

### 路径

原始代码里同时存在过这些绝对路径：

```
f:/Axidian/其他工作/UI/eavi-static/       最早的机器
f:/Axidian/小组项目工作/UI/eavi-static/   还是 f 盘，但换了目录
e:/UI/eavi-static/                        另一台机器
```

更麻烦的是这些路径写在了两个地方：`application.yaml` 配置文件和 Java 代码的 `@Value` 默认值。改了一处忘了另一处，就会读到旧目录。

正确做法是配置只写一处，路径用相对路径，允许环境变量覆盖：

```yaml
# backend/src/main/resources/application.yaml
eavi:
  static-root: ${EAVI_STATIC_ROOT:../eavi-static/}   # 相对 backend/ 目录解析
```

```java
@Value("${eavi.static-root}")
private String staticRoot;    // 不给默认值，默认值只放 yaml 里
```

不要这样写：

```java
@Value("${eavi.dataset.storage-path:e:/UI/eavi-static/data/datasets/}")   // 盘符写死
private String storageRootPath;
```

某台机器上相对路径不好用的话，用环境变量覆盖，不要改代码：

```powershell
$env:EAVI_STATIC_ROOT = "D:\workspace\eavi-system\eavi-static\"
```

验证方法：在一台干净的 macOS 或 Windows 机器上 clone 下来，不改任何代码就能启动。做不到就说明还有硬编码。
### 密码

数据库密码只放在本机，两种方式选一种。

用环境变量：

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

或者用本机私有配置文件，`.gitignore` 已经排除了它：

```yaml
# backend/src/main/resources/application-local.yaml
spring:
  datasource:
    druid:
      password: 你的密码
```

启动时加 `--spring.profiles.active=local`。

旧版本把明文密码提交进了 `application.yaml`，PowerShell 脚本里也有 `-p密码` 的写法，这些都已经清理掉了。提交前可以自查一下：

```powershell
git diff --cached | Select-String "password"
```

### 换行符

`.gitattributes` 里已经统一好了：版本库中一律存 LF，只有 `*.ps1` 和 `*.bat` 用 CRLF，因为 Windows PowerShell 解析纯 LF 的脚本会出问题。

不需要额外做什么。如果编辑器弹窗问要不要转换换行符，选不要。

### 其他

| 事项 | 要求 |
|---|---|
| 路径拼接 | 用 `Paths.get(a, b)` 或 `new File(parent, child)`，不要手写 `/` 或 `\` |
| 上传文件名 | 不要用中文名。旧代码里出现过 `1768635308827_合成疼痛表情数据….pdf`，中文名在跨系统传输和 URL 编码时容易出问题。统一用 `时间戳_原文件名`，参考 `PaperCodeServiceImpl` 的写法 |
| 源码编码 | 全项目 UTF-8，IDEA 里确认 File Encodings 三处都是 UTF-8 |
| 时间 | JDBC URL 带 `serverTimezone=Asia/Shanghai` |
| 大小写 | macOS 和 Windows 的文件系统默认不区分大小写，Linux 区分。文件名不要只靠大小写区别 |

---

## 三、数据库现状

`database/schema.sql` 定义了 14 张表，都是 InnoDB + utf8mb4，带主外键、唯一约束、索引和中文注释。

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

业务关系大致是：用户管理文献和数据集；文献按 AI 任务归类，关联数据集和代码仓库，并在数据集上记录 Benchmark 结果；用户可以把文献放进树形文件夹收藏。

几处值得留意的地方：

- `paper_code` 支持一篇文献挂多个代码仓库，靠唯一键 `(paper_id, code_name)` 约束。
- `paper_dataset` 同时支持库内数据集（`dataset_id`）和外部链接（`dataset_url`），用生成列 `ref_code` 加唯一键在数据库层防重复关联。
- `dataset.base_address` 可以为 NULL，表示这个数据集只有元数据、本地文档没上架。详情接口必须处理这种情况。
- `user`、`dataset`、`folder` 有逻辑删除字段 `is_deleted`；`user` 和 `dataset` 有乐观锁字段 `version`。

完整设计说明在 `docs/02-database-design.md`。

---

## 四、后端范围

任务清单见 `docs/05-backend-todo.md`，接口细节见 `docs/01-api-contract.md`。

| 模块 | 端点数 | 说明 |
|---|---|---|
| `/auth` | 3 | 登录、登出、获取用户信息 |
| `/datasets` | 5 | 列表、详情、Markdown 内容、图片、上传 |
| `/papers` | 12 | 列表、详情，以及任务/数据集/代码/笔记的增删 |
| `/folders` | 6 | 目录树、新建、加文献、删除 |
| `/tasks` | 1 | 任务字典 |
| 静态资源 | 3 | 论文笔记、论文、代码文件的下载 |

技术栈建议沿用参考实现的组合：Spring Boot 3.0.5 + MyBatis-Plus 3.5.3.1 + Druid + jjwt + Jsoup + commonmark。

---

## 五、前端遗留问题

前端部分已经完成并且冻结。调试后端时如果碰到下面这些现象，属于前端已有的问题，记录下来即可，不要顺手去改前端：

| 现象 | 原因 | 处理 |
|---|---|---|
| 数据集卡片状态显示英文 `undefined` | 前端漏映射了 `status` 字段 | 后端正常返回 `status` 就行，前端负责人后续会修 |
| 登出时后端返回 400 | 前端登出不发请求体 | 按契约把 `@RequestBody` 改成非必填 |
| 数据集列表页的分类和状态筛选点了没反应 | 前端筛选 UI 没接线 | 不用管 |
| 文献列表页"新增文献"按钮报路由错误 | 前端跳 `/upload`，该路由不存在 | 不用管 |
| 资源页"查看详情"报错 | 前端路由名拼写不一致 | 不用管 |
| 部分页面暗色主题不生效 | 前端主题有三套实现 | 不用管 |

完整清单在 `docs/04-frontend-known-issues.md`。

---

## 六、`database/` 目录的使用规则

这个项目历史上出现过四份互相矛盾的建库脚本，导致长时间搞不清该用哪一份初始化。现在只保留一份：

```
database/schema.sql                        权威版本，14 张表
database/seed.sql                          配套演示数据
database/check.sql                          完整性自检
database/legacy/schema_v1_original.sql      历史备份，仅供追溯
database/legacy/seed_v1_original.sql        历史备份，仅供追溯
```

`legacy/` 里那份是已知有缺陷的旧版，只有 9 张表，缺 `role`、`user`、`status`、`dataset_task`、`paper_code`，用它初始化连登录都跑不通。留着只是方便查历史差异。

需要改表结构的时候（比如给项目模块加表），直接改 `database/schema.sql`，不要新建 `schema_v2.sql`、`schema_new.sql`、`schema_final.sql` 这类文件。同时在提交信息里写清楚改了什么，并同步更新 `docs/02-database-design.md`。

---

## 七、目录结构

```
eavi-system/
├── README.md
├── frontend/                    Vue 3 前端（已完成，冻结）
│   ├── src/
│   │   ├── composables/useAuth.js      认证逻辑，调 /auth/*
│   │   ├── views/                      12 个页面
│   │   ├── components/                 Navbar、富文本编辑器
│   │   └── data/projects.js            项目模块的假数据 + localStorage CRUD
│   ├── public/                         静态资源（markdownTemplate.md 等）
│   ├── package.json / vite.config.js
│   └── node_modules/                   不入库，自己 npm install
│
├── backend/                     后端工作区（当前只有 README）
│
├── database/                    MySQL 脚本
│   ├── schema.sql                       14 张表，权威版本
│   ├── seed.sql                         演示数据
│   ├── check.sql                        完整性自检
│   └── legacy/                          旧版备份，不要用
│
├── eavi-static/                 后端要服务的静态资源（只读）
│   └── data/
│       ├── datasets/
│       │   ├── VOC2007/                 唯一真正上架的数据集文档和图片
│       │   └── 123/                     测试遗留，可忽略
│       └── paper_note/                  论文笔记上传目录（内容不入库）
│
├── docs/
│   ├── 01-api-contract.md       接口契约（权威版本）
│   ├── 02-database-design.md    数据库设计说明
│   ├── 03-cross-platform.md     跨平台约定详版
│   ├── 04-frontend-known-issues.md  前端已知问题
│   └── 05-backend-todo.md       后端任务清单
│
├── reference/
│   ├── README.md                先读这个：参考实现的使用边界
│   └── legacy-backend/          学长给的 Spring Boot 实现（有缺陷，勿照抄）
│
└── scripts/                     建库和启动脚本，macOS + Windows 双版本
```

---

## 八、自测清单

后端写完后，按这个顺序走一遍。前 12 条在前端界面上就能验证，第 13 条要换台机器。

```
1.  全新 clone，不改任何代码，能启动
2.  用 lcy / 123456 从前端登录页登录成功
3.  刷新页面后仍是登录状态（/auth/profile 生效）
4.  数据集列表出现 5 条，PASCAL VOC 2007 能点进详情，
    正文和图片都渲染出来
5.  点击 ImageNet（base_address 为 NULL）不报 500，
    而是显示"尚未上架"的中文提示
6.  上传一个新数据集（含图片），列表和详情里都能看到
7.  文献列表出现 2 条，详情页的任务、代码、数据集、结果四个区块有数据
8.  能给文献加任务、加代码、加数据集关联，也能删掉
9.  文献目录树显示"我的文库 → 精读/略读 → 计算机视觉/自然语言处理"三层
10. 删除一个子目录后，其中的文献回到"我的文库"
11. 用 qmn 登录，只看得到 qmn 自己的目录，看不到 lcy 的
12. 上传一次论文阅读笔记，能通过 /data/paper_note/xxx 打开
13. 在另一台机器（macOS 或 Windows）clone 一次，重复第 1 步
```

第 11 条是权限验证，最容易漏掉。参考实现里就有这个问题——`userId` 为空时会返回全部用户的目录，**务必测**。

---

## 九、协作约定

- 不要提交 `node_modules/`、`dist/`、`target/`、`*.log`、`.idea/`、`application-local.yaml`，`.gitignore` 已经配好。
- 提交信息中英文都行，说明改了什么、为什么。
- 前端部分由其他成员负责且已完成，改动前先沟通。
- 契约有疑问时以 `docs/01-api-contract.md` 为准。如果发现契约本身有问题，先在 Issues 里提出来讨论，不要单方面改前端。
- 有问题记到仓库 Issues 里，比在群里刷消息好追溯。
