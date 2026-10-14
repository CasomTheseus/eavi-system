# 前端已知问题清单

> **本文档只做记录，不做修改。**
>
> 前端是前端同学的独立工作成果，本次统合**原样入库、未改动任何一行**。
> 下面这些问题在调试后端时可能被误认为是后端 bug，所以集中记录在此，
> 便于后端同学区分"是谁的问题"，也便于前端同学后续整改。

## 问题分级

| 级别 | 含义 |
|---|---|
| 🔴 **阻塞** | 会让用户用不了某个功能，或影响后端对接 |
| 🟡 **干扰** | 会误导调试，或界面显示不对 |
| ⚪ **清理** | 死代码、调试残留，不影响运行 |

---

## A. 后端对接相关（后端同学重点看）

| # | 级别 | 问题 | 位置 | 对后端的影响 |
|---|---|---|---|---|
| A1 | 🔴 | 调用**不存在的** `GET /auth/profile` | `composables/useAuth.js:110` | **后端必须实现它** |
| A2 | 🔴 | 登出**不发请求体**，但后端 `@RequestBody` 必填 → HTTP 400 | `useAuth.js:85` | 后端必须把 `@RequestBody` 改成非必填 |
| A3 | 🔴 | 下载链接指向不存在的域名 `https://api.eavi.xidian.edu.cn/v1/...`，且调用点被注释 | `views/DatasetDetail.vue:237`（定义）、`:263,265`（已注释） | 后端**不必实现**该接口 |
| A4 | 🟡 | `status` 字段在列表映射中被漏掉 → 卡片显示英文 `undefined` | `views/DatasetList.vue:271-284` | 后端正常返回 `status` 即可，等前端修 |
| A5 | 🟡 | 文献 `status` 只认中文（`已阅读`/`未阅读`/`正在阅读`/`重要文献`） | `views/LiteratureDetail.vue:410` 直接绑定模板 | 后端必须返回中文状态 |
| A6 | 🟡 | `codeUrl` 字段在 `paper` 表已不存在，永远为 `null` | `views/LiteratureManagement.vue:454` | 后端返回 `null`，**不要为它给 `paper` 表加回列** |
| A7 | 🟡 | `field:'ml'`、`category:'journal'` 在 API 映射时被**硬编码成固定值** | `views/LiteratureManagement.vue:450-451` | 前端这两维筛选永远失真，与后端无关 |
| A8 | 🟡 | 项目/资源/首页共 **7 个页面零网络请求** | 见下方 D 节 | 这 6 个模块**没有任何后端契约** |

### A1/A2/A3 的说明

这三条是前后端契约的硬冲突，已在 `docs/01-api-contract.md` 第 6 节汇总。
**处理原则**：前端已冻结，后端按前端的调用来实现（A1 要实现，A2 要兼容，A3 不用管）。

---

## B. 会导致用户可见故障的

| # | 级别 | 问题 | 位置 |
|---|---|---|---|
| B1 | 🔴 | "新增文献"按钮跳 `/upload`，但**该路由不存在** → 功能完全不可用 | `views/LiteratureManagement.vue:577` vs `main.js` 路由表 |
| B2 | 🔴 | 资源"查看详情"push 的 `name:'ResourceDetails'`，实际注册的是 `'ResourceDetail'`（无 s）→ 点击必报路由错误 | `views/ResourceManagement.vue:251` vs `main.js:104` |
| B3 | 🟡 | 项目页侧栏分类点击无效：`selectCategory` 改的是 `selectedCategory`，筛选读的是 `activeCategory`（两个独立状态） | `views/ProjectManagement.vue:210-212 / 255-261 / 316` |
| B4 | 🟡 | 资源页对 `.theme-toggle` 元素**未判空**就改 DOM → 元素不存在时抛 `Cannot set properties of null` | `views/ResourceManagement.vue:288-293` |
| B5 | 🟡 | 资源详情页 `mockData` 只有 `server-01` / `display-01` 两个 key，其余 id 一律回退 → 永远显示"服务器-01" | `views/ResourceDetail.vue:183-221` |
| B6 | 🟡 | 项目文件"下载"用 `Blob` 生成的是一段自述文本，内容写着"这是…模拟下载文件" | `views/ProjectDetail.vue:608-614` |
| B7 | ⚪ | 上传页在编辑模式下可提交空文档 | `views/DatasetUpload.vue:1084` |
| B8 | ⚪ | `resetForm` 只清 `section.blocks`，漏掉 `subsections[].blocks` → 子章节内容提交后残留 | `views/DatasetUpload.vue:3066-3081` |

---

## C. 代码质量（⚪ 清理类，不影响后端）

| # | 问题 | 位置 |
|---|---|---|
| C1 | **方法重复定义**，后者覆盖前者（`addBlock` 1338/1390、`removeBlock` 1350/1447、`updateBlockContent` 2725/2754） | `views/DatasetUpload.vue` |
| C2 | `updateBlockContent` 的 **3 参调用与 4 参调用混用** → 4 参版本下 `content` 为 `undefined`，会把块内容写坏 | `DatasetUpload.vue:2595-2695` vs 模板 `:169,:574` |
| C3 | 引用**未定义函数**：`insertHeading` 内部调用 `insertLink`/`insertCode`/`insertQuote` | `views/DatasetUpload.vue:2606` |
| C4 | 引用**不存在的 data 属性** `this.uploadedNotes` | `views/LiteratureDetail.vue:625` |
| C5 | 写入**未声明的** `this.sections`（Vue 3 下非响应式，不会渲染） | `views/DatasetUpload.vue:1284` |
| C6 | 约 15 个方法/属性无任何调用点（`formatText`、`insertLink`、`saveTextContent`、`onBlockTypeChange`、`handleFileSelect`、`templateContent` 等） | `DatasetUpload.vue`、`LiteratureDetail.vue` |
| C7 | 整套 HTML→Markdown 转换代码是**死代码**（`generateStandardMarkdown`、`htmlToStandardMarkdown` 等），实际提交的是 HTML | `DatasetUpload.vue:2027/2050/2770/2886/3022` |
| C8 | 富文本编辑器全程使用**已废弃的** `document.execCommand` / `queryCommandState` | `components/RichTextEditor.vue:122/145/153/157` |
| C9 | `mounted` 加的 `selectionchange` 监听在 `beforeUnmount` **未移除** → 监听器泄漏 | `components/RichTextEditor.vue:95 / 101` |
| C10 | `marked.setOptions({highlight, sanitize:false})` —— 这两个选项在 **marked v5+ 已移除**，而 `package.json` 锁的是 `^16.3` → 代码高亮无效，且**没有任何 HTML 消毒**，而详情页用 `v-html` 渲染后端返回的 HTML | `views/DatasetUpload.vue:1057-1069` |
| C11 | `main.js` 里 `import { useAuth }` 导入后从未使用 | `main.js:8` |
| C12 | 多处**空 `catch`**，接口失败完全静默 | `LiteratureManagement.vue:402/514/532/626/694`、`DatasetList.vue:439`、`DatasetUpload.vue:1173`、`LiteratureDetail.vue:906` |
| C13 | 主题实现**三套并存且根元素不同**（`documentElement` vs `body` vs `body.dark-theme`）→ 4 个页面暗色模式不生效 | `Navbar.vue:75,85` vs `ResourceManagement.vue:285`、`LiteratureDetail.vue:724`、`ResourceDetail.vue:237`、`ResourceApplication.vue:303` |
| C14 | 生产代码里残留调试输出：调试面板（`DatasetUpload.vue:855-867`）、70 行 emoji 日志的 `processColorTags`（`:1187-1270`）、"测试颜色标签"按钮（`:850`）、多处 `console.log` | 多处 |
| C15 | `App.vue` 里针对颜色的 `!important` 全局覆盖，注释明确写着"测试用" | `App.vue:32-63` |
| C16 | 搜索无防抖，每次按键都发一次请求 | `LiteratureManagement.vue:405` |

---

## D. 零后端契约的模块（7 个页面）

这些页面**不发出任何网络请求**，数据全部来自硬编码或浏览器 localStorage：

| 页面 | 路由 | 数据来源 |
|---|---|---|
| 首页 | `/home` | 全部硬编码（投稿时间线只含 2025 年，约 30 条） |
| 项目管理 | `/projects` | `src/data/projects.js` 的 6 条假项目 + localStorage |
| 项目新建 | `/projects/create` | 只写 localStorage，文件本体不落盘 |
| 项目详情 | `/projects/:id` | localStorage；含上面 B6 的假下载 |
| 资源管理 | `/resources` | 8 条硬编码资源 |
| 资源详情 | `/resources/:id` | 2 个 key 的 mock |
| 资源申请 | `/resource-application` | 表单不发请求，只 `sleep(2s)` + alert |

**如果后续要给这些模块做后端**，注意：

- 项目模块的字段结构可以拿 `frontend/src/data/projects.js` 当 schema 草案（每个项目约 30 个字段，
  含 `members[]` / `milestones[]` / `files[]` / `activities[]` / `description{...}` / `techStack[]`）。
- 资源模块的字段结构参考 `views/ResourceManagement.vue:124-205`。
- **必须重新和前端同学对齐接口**，不能照搬前端现有的假数据结构（其中 `file.type` 枚举
  在 `ProjectCreate.vue:285-297` 和 `ProjectDetail.vue:645-655` 就有**两套不一致的实现**）。

---

## E. 数据可信度提醒

以下是**编造的演示数据**，如果被截图进汇报材料可能被误当成真实结果：

| 内容 | 位置 |
|---|---|
| 6 篇文献，含**编造的 arXiv 编号** `2306.00001`、`2006.00002`、`2012.00003`、`1812.00004` | `views/LiteratureManagement.vue:227-342` |
| 分类/状态筛选的计数写死 `45/28/12/3/2`、`32/8/5`，永不更新 | `LiteratureManagement.vue:214-226` |
| 项目活动流中的性能数字："CTR 提升 8.3%"、"Recall@100 达到 0.92"、"F1 达到 0.91"、"启动时间由 2.8s 降至 1.4s" | `src/data/projects.js` |
| `relatedDatasets` 默认 MS MARCO / BEIR；`lit1`/`lit2` 两篇假论文 | `views/LiteratureDetail.vue:331-346, 467-509` |

---

## F. 安全提醒（上线前必须处理）

| # | 问题 | 位置 |
|---|---|---|
| F1 | **硬编码假登录**：后端不可达时代码自动降级，**密码恒等于用户名**，5 个账号写死在源码里（`admin`/`lisi`/`wangwu`/`zhaoliu`/`chenqi`）。后端一挂，输 `admin/admin` 即获得管理员身份 | `composables/useAuth.js:52-73` |
| F2 | 降级时生成的 token 是明文假串 `'mock-jwt-token-' + Date.now()`，而路由守卫**只判断 token 字符串是否存在** → 手动 `localStorage.setItem('token','x')` 即可进入所有受保护页面 | `useAuth.js:62`、`main.js:121-138` |
| F3 | 前端**没有响应拦截器**，不处理任何 401，token 过期后各页面静默失败 | 全局 |
| F4 | `v-html` 渲染后端返回的 HTML，且 marked 的 `sanitize` 选项已失效（见 C10）→ XSS 风险面 | `DatasetDetail.vue`、`DatasetUpload.vue` |

> ⚠️ F1/F2 是**上线前必须移除**的。目前练手阶段可以保留以便调试，
> 但一旦部署到任何公开可访问的环境，这两条会直接导致越权。

---

## G. 说明

- 本文档中的问题**定位到行号**，但除 A1/A2/A3/A4/A6、B1、B2 外，
  大部分来自一次性逐行通读，**前端同学整改前建议自行确认一遍**。
- 已核实的前端与后端契约冲突，统一以 `docs/01-api-contract.md` 为准。
