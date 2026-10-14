# 接口契约（唯一权威版本）

> **本文件的优先级高于其他所有文档。**
>
> 判定原则：**以「前端实际发出的请求」为准，不以 `reference/legacy-backend` 的实现为准。**
> 理由：前端已经完成并且不会重写；后端尚未编写。所以前端怎么调，后端就必须怎么实现。
> 学长那份参考后端与前端**并不完全一致**（有 6 处硬冲突，见第 6 节）。遇到不一致时，按本文件写。

---

## 0. 全局约定

| 项目 | 约定 |
|---|---|
| 后端地址 | `http://localhost:8080` |
| 端口 | `8080`（前端 Vite 在 `3000`） |
| 统一响应信封 | `{"code": 200, "message": "...", "data": {...}}` |
| 成功判定 | 前端判 `response.ok && data.code === 200`（HTTP 状态码也要是 200） |
| 认证方式 | `Authorization: Bearer <token>` 请求头 |
| 字符集 | 全链路 UTF-8；数据库 `utf8mb4` |
| 跨域 | **必须开 CORS**（前端从 3000 直连 8080，不走代理） |

### 0.1 两条必须遵守的约定

**① 报错信息必须是中文。**
前端是 `throw new Error(data.message)` 然后直接显示给用户。学长的后端返回的是 `"usernameError"` / `"passwordError"` 这类英文代号，会**原样显示在中文界面上**。请返回中文，例如 `"用户名或密码错误"`。

**② 响应字段名很不统一，必须逐字段照抄。**
同一个"任务名列表"，在数据集列表接口里叫 `task_names`，在数据集详情接口里叫 `taskNames`；"下载次数"在列表里叫 `downloadCount`、在详情里叫 `downloadcount`。
这不是笔误，是前端代码里实际读取的键名。**请严格按本文件的字段名返回，不要"统一命名"**，否则前端拿不到值。这是历史遗留问题，后续由前端同学统一整改，不在后端职责范围内。

---

## 1. 认证模块 `/auth`

### 1.1 `POST /auth/login`
- **前端调用**：`frontend/src/composables/useAuth.js:18`
- **请求体**（`application/json`）：
  ```json
  { "username": "lcy", "password": "123456" }
  ```
- **响应**：
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "<JWT>",
      "refreshToken": "<JWT>",
      "expiresIn": 7200000,
      "user": {
        "id": 1001,
        "username": "lcy",
        "email": null,
        "name": "刘晨宇",
        "role": "管理员",
        "department": "EAVI团队",
        "avatar": null,
        "lastLoginTime": "..."
      }
    }
  }
  ```
- **前端读取**：`data.data.user`、`data.data.token`
- **失败**：
  ```json
  { "code": 401, "message": "用户名或密码错误", "data": null }
  ```
- **要点**：
  - 数据库里密码存的是 **MD5 十六进制小写**，`123456` → `e10adc3949ba59abbe56e057f20f883e`。
  - 只按 `username` 查（**不支持邮箱登录**，尽管 `API接口文档.md` 里写了支持）。
  - `role` 字段要返回**中文角色名**，需 `user.role_id` JOIN `role` 表取 `role.role`。
- **参考后端**：✅ 已有（`UserController:66`），基本可用。

### 1.2 `POST /auth/logout`
- **前端调用**：`frontend/src/composables/useAuth.js:85`
- **请求体**：**没有请求体！** 只有请求头 `Authorization: Bearer <token>`
- **响应**：前端不读内容，调用失败也会本地清除登录态
- **⚠️ 硬冲突**：学长后端写的是 `public Result logout(@RequestBody String token)` —— `@RequestBody` 默认必填，前端不发 body 会直接 **HTTP 400**。
  **请改为** `@RequestBody(required = false)` 或从请求头取 token。
- **参考后端**：⚠️ 存在但与本契约不兼容。

### 1.3 `GET /auth/profile`
- **前端调用**：`frontend/src/composables/useAuth.js:110`
- **请求**：仅请求头 `Authorization: Bearer <token>`
- **响应**：`data` 是**用户对象本身**（不是包一层 `user`）
  ```json
  { "code": 200, "message": "success",
    "data": { "id": 1001, "username": "lcy", "name": "刘晨宇", "role": "管理员", "department": "EAVI团队", "email": null, "avatar": null } }
  ```
- **前端读取**：`data.data` 整体作为用户对象
- **⚠️ 硬冲突**：**学长的后端里根本没有这个接口**（全项目搜索 `profile` 零结果）。
  **必须新增。** 实现方式：校验 `Authorization` 头里的 JWT → 解析出 `userId` → 查库返回用户信息。
- **参考后端**：❌ 缺失，必须新建。

---

## 2. 数据集模块 `/datasets`

### 2.1 `GET /datasets`
- **前端调用**：`frontend/src/views/DatasetList.vue:253`
- **Query 参数**（全部可选）：`page`、`pageSize`、`sortBy`、`sortOrder`、`search`、`taskId`、`status`
  - `taskId` 传的是**任务 ID 数字**（前端用 `String(t.id)` 作筛选键），不是任务名。
- **响应**：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "pageInfo": {
        "datasets": [
          {
            "dataset_id": 1,
            "dataset_name": "PASCAL VOC 2007",
            "description": "...",
            "status": "active",
            "size_formatted": "460 MB",
            "file_count": 9963,
            "task_names": "图像分类,目标检测,实例分割",
            "name": "齐梦男",
            "downloadCount": 156,
            "created_time": "2024-03-11 14:09:04",
            "updated_time": "2024-03-11 14:09:04",
            "true_address": "...",
            "base_address": "VOC2007"
          }
        ],
        "total": 5,
        "totalPages": 1,
        "page": 1,
        "pageSize": 20
      }
    }
  }
  ```
- **前端读取的键**（严格）：`dataset_id`、`dataset_name`、`description`、`size_formatted`、`file_count`、`task_names`、`name`、`created_time`、`updated_time`、`downloadCount`
- **要点**：
  - `task_names` 是**逗号拼接的字符串**，前端会 `.split(',')` 成标签数组。
  - `name` 是**上传者姓名**（来自 `user.name`），不是数据集名，容易看错。
  - `downloadCount` 是**大驼峰**，和同响应里其他 snake_case 键不一致，请照抄。
  - ⚠️ 前端**没有映射 `status` 字段**（这是前端 bug，已记录在 `docs/04-frontend-known-issues.md`），但 `status` 仍应返回，前端修好后即可用。
  - ⚠️ 参考后端的 `DatasetMapper.selectMyPage` 用了 `group by dataset.dataset_id`，而 select 列表里有 `status.status`、`user.name`、`GROUP_CONCAT(t.name)` 等非聚合列。**MySQL 8 默认开启 `ONLY_FULL_GROUP_BY`，这条 SQL 会直接报错。** 请改为按 select 列表完整分组，或用 `ANY_VALUE()`。
- **参考后端**：✅ 已有（`DatasetMapper.xml#selectMyPage`），字段与上表吻合；⚠️ 需修 `GROUP BY`。

### 2.2 `GET /datasets/{id}`
- **前端调用**：`frontend/src/views/DatasetDetail.vue:218`
- **响应**：⚠️ **注意双层嵌套 `data.data`**
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "data": {
        "dataset_id": 1,
        "datasetName": "PASCAL VOC 2007",
        "description": "...",
        "status": "active",
        "sizeFormatted": "460 MB",
        "fileCount": 9963,
        "taskNames": "图像分类,目标检测,实例分割",
        "name": "齐梦男",
        "downloadcount": 156,
        "createdTime": "...",
        "updatedTime": "...",
        "trueAddress": "...",
        "baseAddress": "VOC2007"
      }
    }
  }
  ```
- **前端读取**：`result.data.data`，字段名用**驼峰**：`datasetName`、`sizeFormatted`、`fileCount`、`taskNames`、`downloadcount`（全小写 c）、`createdTime`、`updatedTime`、`trueAddress`、`name`、`status`
- **要点**：
  - 双层 `data` 是**学长的设计**（`DatasetServiceImpl.findDatasetDetail` 把 VO 塞进 `map.put("data", vo)` 再 `Result.ok(map)`），前端也按双层写了，所以**保持双层**。
  - 同一个 `task_names` 在这里叫 `taskNames`，`size_formatted` 在这里叫 `sizeFormatted` —— 和 2.1 完全不同，请照抄。
- **参考后端**：✅ 已有（`DatasetMapper.xml#selectDataset` → `DatasetVo`），但同样有 `GROUP BY` 问题。

### 2.3 `GET /datasets/{id}/markdown-content`
- **前端调用**：`frontend/src/views/DatasetDetail.vue:194`
- **响应**：
  ```json
  {
    "code": 200, "message": "success",
    "data": {
      "datasetId": 1,
      "htmlContent": "<h1>PASCAL VOC 2007</h1>...",
      "title": "PASCAL VOC 2007",
      "imageUrls": ["..."],
      "lastModified": "...",
      "fileSize": 9785.0
    }
  }
  ```
- **前端读取**：`result.data.htmlContent`（**单层** `data`，与 2.2 不同），然后用 `v-html` 渲染
- **要点**：
  - 后端要读 `eavi-static/data/datasets/<base_address>/` 下的 Markdown，转成 HTML 返回，并把图片路径改写成 `/datasets/{id}/images/{文件名}`。
  - **`base_address` 为 NULL 的数据集必须返回业务错误，不能 500。**
    种子数据里 5 个数据集只有 `PASCAL VOC 2007` 有本地文档，其余 4 个 `base_address` 是 NULL。
    参考后端的 `buildFullPath()` 对 NULL 会抛 `IllegalArgumentException("相对路径不能为空")` → 前端收到 500。请改成返回 `{"code": 404, "message": "该数据集尚未上架本地文档"}`。
- **参考后端**：✅ 已有，⚠️ 必须补 NULL 判断。

### 2.4 `GET /datasets/{id}/images/{imagePath}`
- **前端调用**：由 `DatasetDetail.vue` 把 HTML 里非 `http`/`datasets/` 开头的 `<img src>` 改写成 `datasets/{id}/images/{原路径}` 后由浏览器请求
- **响应**：**原始图片二进制**（不是 JSON），带正确 `Content-Type`
- **要点**：必须**防路径穿越**（`../`），参考后端的 `imageFinalPath.startsWith(datasetBasePath)` 检查是对的，保留。
- **参考后端**：✅ 已有。

### 2.5 `POST /datasets/upload`
- **前端调用**：`frontend/src/views/DatasetUpload.vue:2375`
- **Content-Type**：`multipart/form-data`
- **表单字段**：

  | 字段 | 类型 | 说明 |
  |---|---|---|
  | `datasetName` | String | 数据集名称 |
  | `description` | String | 简介 |
  | `taskIds` | String | **JSON 数组的字符串**，如 `"[1,2,3]"` |
  | `htmlContent` | String | 正文 HTML（结构化编辑模式） |
  | `trueAddress` | String | 真实下载地址 |
  | `sizeFormatted` | String | 如 `"460 MB"` |
  | `files` | MultipartFile[] | 图片文件 |
  | `markdownFile` | MultipartFile | **Markdown 上传模式**下替代 `htmlContent` |

- **请求头**：`Authorization: Bearer <token>`（**必须**，用来确定 `user_id`）
- **响应**：`{ "code": 200, "message": "...", "data": {...} }`（前端只判 `code === 200`，然后跳转列表页）
- **⚠️ 重要**：`htmlContent` 里图片的 `src` 可能是 **`data:image/...;base64,...` 形式的 Data URI**。
  前端**放弃了** HTML→Markdown 的转换（那部分代码是死的），所以**后端需要自己把 HTML 转成 Markdown**，并把 base64 图片落盘、替换成文件名。
  学长的 `HtmlToMarkdownServiceImpl` + Jsoup 正是干这个的，可作参考。
- **参考后端**：✅ 已有（`DatasetController:128`）。

### 2.6 `GET /datasets/{id}/download-links` — **可以不实现**
- 前端虽然定义了调用函数，但**调用点被注释掉了**（`DatasetDetail.vue:263,265`），并且 URL 指向一个不存在的外部域名 `https://api.eavi.xidian.edu.cn/v1/...`。
- **结论：后端不必实现。** 若将来要做，需先和前端同学确认新的 URL。

---

## 3. 文献模块 `/papers`

### 3.1 `GET /papers`
- **前端调用**：`frontend/src/views/LiteratureManagement.vue:424`
- **Query**：`page`、`pageSize`、`search`、`sortBy`（只会是 `citations` 或 `uploadTime`）、`sortOrder`
- **响应**：
  ```json
  {
    "code": 200, "message": "success",
    "data": {
      "papers": [
        { "id": 1, "title": "...", "authors": "张三, 李四", "venue": "IEEE TPAMI",
          "paperAbstract": "...", "uploadTime": "2024-01-15 00:00:00",
          "citations": 156, "impact": "IF:17.73", "status": "已阅读",
          "paperUrl": "https://arxiv.org/abs/1904.05049", "codeUrl": null }
      ],
      "pageInfo": { "page": 1, "pageSize": 20, "totalPages": 1, "total": 2 }
    }
  }
  ```
- **前端读取**：`res.data.data.papers`，每条读 `id`、`title`、`authors`、`venue`、`paperAbstract`、`uploadTime`、`citations`、`impact`、`status`、`paperUrl`、`codeUrl`
- **要点**：
  - `status` 应为**中文**：`已阅读` / `未阅读` / `正在阅读` / `重要文献`。前端做中文→英文码的映射；不认识的值一律当"未阅读"。
    数据库 `paper.status` 默认 `'未阅读'`，种子数据用 `'已阅读'`/`'正在阅读'`。✅ 一致。
  - ⚠️ `codeUrl` 前端会读，但**数据库 `paper` 表已经没有这一列了**（已改为多对多的 `paper_code` 表）。参考后端的 `Paper` 实体也没有这个字段，所以它永远是 `null`。**这是前端的死字段，后端返回 `null` 即可，不要为它在 `paper` 表加回列。**
  - `paperAbstract` 是实体里的 Java 字段名（对应数据库列 `abstract`，用 `@TableField("abstract")` 映射），Jackson 序列化出来就是 `paperAbstract`。请保持一致。
  - 搜索范围：标题 / 作者 / 期刊 / 摘要，任一项 LIKE 命中即可。
- **参考后端**：✅ 已有（`PaperServiceImpl.listPapers`）。

### 3.2 `GET /papers/{id}`
- **前端调用**：`frontend/src/views/LiteratureDetail.vue:399`
- **响应**：
  ```json
  {
    "code": 200, "message": "success",
    "data": {
      "paper": { "id":1, "title":"...", "authors":"...", "venue":"...", "paperAbstract":"...",
                 "uploadTime":"...", "citations":156, "impact":"...", "status":"已阅读",
                 "paperUrl":"...", "noteUrl":"..." },
      "repositories": [ { "name": "PyTorch 官方视觉实现", "url": "https://github.com/pytorch/vision" } ],
      "datasets": [ { "datasetId": 1, "datasetName": "PASCAL VOC 2007", "description": "...",
                      "datasetUrl": null, "tags": "图像分类,目标检测" } ],
      "results": [ { "id":1, "task":"图像分类", "dataset":"ImageNet", "metric":"Top-1",
                     "value":"76.0", "isSota":0 } ],
      "tasks": [ "图像分类", "目标检测" ]
    }
  }
  ```
- **要点**：
  - `repositories` 来自 **`paper_code` 表**（不是 `repository` 表！），键名是 `name` / `url`。
  - `datasets` 是**两个来源的 UNION**：库内数据集（有 `datasetId`）+ 外部链接（`datasetId` 为 null，`datasetName` 取 `display_name` 或 `dataset_url`，`datasetUrl` 有值）。
  - `tasks` 是**字符串数组**（任务名），不是对象数组。
  - `results.isSota` 是 `0/1` 整数。
- **参考后端**：✅ 已有（`PaperServiceImpl.getPaperDetail` + `PaperMapper.xml`）。

### 3.3 文献关联的增删（9 个端点）

| 端点 | 请求体 | 调用位置 |
|---|---|---|
| `POST /papers/{id}/tasks` | `{"taskName": "图像分类"}` | `LiteratureDetail.vue:884` |
| `DELETE /papers/{id}/tasks` | `{"taskName": "图像分类"}` | `LiteratureDetail.vue:847` |
| `POST /papers/{id}/datasets` | `{"datasetId": 1}` 或 `{"datasetUrl":"https://...","displayName":"..."}` | `:912` / `:929` |
| `DELETE /papers/{id}/datasets` | `{"datasetId": 1}` 或 `{"datasetUrl":"https://..."}` | `:860` |
| `POST /papers/{id}/codes` | `{"codeName":"...","codeUrl":"..."}` | `:826` |
| `DELETE /papers/{id}/codes` | `{"codeName":"..."}` | `:872` |
| `POST /papers/{id}/note` | multipart，字段名 `file` | `:537` |
| `DELETE /papers/{id}/note` | 无 | `:594`, `:809` |
| `POST /papers/{id}/codes/upload` | multipart，字段名 `files`（多个）+ `names`（多个，代码名） | `:566` |

- **注意**：`DELETE` 请求**带请求体**（`axios.delete(url, { data: {...} })`）。Spring Boot 3 默认支持，但若用 Nginx 之类的反代要留意。
- `POST /papers/{id}/note` 的响应：`{"code":200,"data":{"noteUrl":"文件名.pdf"}}`
- `POST /papers/{id}/codes/upload` 的响应：`{"code":200,"data":[{"codeName":"...","codeUrl":"...","size":1234}]}`
- **参考后端**：✅ 全部已有（`PaperController`）。

---

## 4. 文献目录模块 `/folders`

### 4.1 `GET /folders/tree`
- **前端调用**：`frontend/src/views/LiteratureManagement.vue:396` —— **不传任何参数！**
- **响应**：
  ```json
  {
    "code": 200, "message": "success",
    "data": {
      "folders": [
        { "id": 100, "name": "我的文库", "parentId": null, "paperIds": [1,2],
          "children": [
            { "id": 101, "name": "精读", "parentId": 100, "paperIds": [1], "children": [] },
            { "id": 102, "name": "略读", "parentId": 100, "paperIds": [2],
              "children": [ { "id": 103, "name": "计算机视觉", "parentId": 102, "paperIds": [1], "children": [] } ] }
          ] }
      ]
    }
  }
  ```
- **要点**：
  - 返回的是**已经嵌套好的树**（`children` 递归），不是扁平列表。前端手写三层递归渲染，**最多显示 3 层**。
  - 前端只认 `id`、`name`、`children`、`paperIds` 这四个键（`parentId` 会返回但渲染不用）。
  - 🔴 **安全要求：前端不传 `userId`，所以后端必须从 JWT 解析出当前用户，只返回该用户的目录。**
    参考后端写的是 `if (userId != null) qw.eq(Folder::getUserId, userId)` —— `userId` 为 null 时**会返回全部用户的目录**，属于越权。**请务必修正。**
- **参考后端**：⚠️ 已有但存在越权问题，必须改。

### 4.2 `POST /folders`
- **前端调用**：`LiteratureManagement.vue:509`（根目录）与 `:525`（子目录）
- **请求体**：`{"name":"新目录"}`（根）或 `{"name":"子目录","parentId":100}`（子）
  - 前端**不传 `userId`** → 后端从 JWT 取
- **响应**：`{"code":200,"data":{"folderId":105,"name":"新目录"}}`
- **参考后端**：✅ 已有，同样需从 JWT 补 `userId`。

### 4.3 `POST /folders/{folderId}/papers`
- **前端调用**：`LiteratureManagement.vue:621`, `:692`
- **请求体**：`{"paperId": 1}`
- **参考后端**：✅ 已有。

### 4.4 删除目录（3 个端点，全部保留）
前端会**依次盲试**这三个，任意一个成功即可（`LiteratureManagement.vue:672-674`）：

| # | 端点 | 请求体 |
|---|---|---|
| 1 | `DELETE /folders/{folderId}` | 无 |
| 2 | `POST /folders/{folderId}/delete` | 无 |
| 3 | `POST /folders/delete` | `{"folderId": 103}` |

- 三个都失败时前端会弹 `"后端删除接口不可用或未对齐，已回退。"`
- **学长的后端三个都实现了**，建议保留，但**至少要保证第 1 个能用**。
- 语义（参考实现，建议沿用）：
  - 名为 `我的文库` 的根目录**不允许删除**，返回 `{"deleted": false, "message": "cannot-delete-root"}`
  - 删除一个目录时，其**子树内所有文献会被移动到 `我的文库`**，然后删除整棵子树，返回 `{"deleted": true, "movedCount": 3}`
- **参考后端**：✅ 三个都有（`FolderController`）。

---

## 5. 任务字典与静态资源

### 5.1 `GET /tasks`
- **前端调用**：`DatasetList.vue:426`、`DatasetUpload.vue:1165`、`LiteratureDetail.vue:439`
- **响应**：`{"code":200,"message":"success","data":[{"id":1,"name":"图像分类"},{"id":2,"name":"目标检测"}]}`
- **参考后端**：✅ 已有（`TaskController`）。

### 5.2 `GET /tasks/by-dataset/{datasetId}`
- 前端**未使用**，参考后端有此接口，可保留可删。

### 5.3 静态资源（非 JSON，直接返回文件）
前端用 `window.open()` 打开（`LiteratureDetail.vue:582, 962-966`）：

| 路径 | 对应磁盘位置 |
|---|---|
| `/data/paper_note/{filename}` | `eavi-static/data/paper_note/` 下的文件 |
| `/data/paper/{filename}` | `eavi-static/data/paper/` |
| `/data/paper_code/{filename}` | `eavi-static/data/paper_code/` |

- 参考后端做法：在 `application.yaml` 里配 `spring.web.resources.static-locations` 指向 `eavi-static/` 目录。

---

## 6. ⚠️ 与参考后端的 6 处硬冲突（必读）

| # | 契约要求 | 学长后端现状 | 你必须做的 |
|---|---|---|---|
| 1 | `GET /auth/profile` | **不存在** | **新增**，从 JWT 解析 userId 后返回用户对象 |
| 2 | `POST /auth/logout` 无请求体 | `@RequestBody` 必填 → 400 | 改成 `required = false` 或从请求头取 |
| 3 | 报错信息中文 | 返回 `"usernameError"` 等英文代号 | 改为中文提示 |
| 4 | `base_address` 为 NULL 时返回业务错误 | 抛异常 → HTTP 500 | 加 NULL 判断，返回 `code:404` + 中文说明 |
| 5 | `GET /folders/tree` 只返回当前用户目录 | `userId` 为 null 时返回**全部用户**目录 | 从 JWT 取 userId，强制过滤 |
| 6 | `selectMyPage` / `selectDataset` 需在 MySQL 8 下可执行 | `GROUP BY` 不完整，`ONLY_FULL_GROUP_BY` 下报错 | 补全 `GROUP BY` 或用 `ANY_VALUE()` |

---

## 7. 不存在的接口（不要为它们建表）

以下东西在前端代码里能看到，但**不是活代码**，不要实现：

- `GET /api/v1/datasets/stats` —— 只出现在 `DatasetList.vue:305-340` 的**注释块**里。
- `GET /datasets/{id}/download-links` —— 调用点被注释，且指向不存在的外部域名。
- **项目模块**（`/projects`、`/projects/create`、`/projects/:id`）—— 3 个页面全部使用浏览器 localStorage，**零网络请求**。
- **资源模块**（`/resources`、`/resources/:id`、`/resource-application`）—— 3 个页面全部是写死的假数据，申请表单**不发任何请求**。
- **首页** `/home` —— 投稿时间线等全部硬编码。

> 这 7 个页面共 6 个模块目前完全没有后端契约。如果后续要接后端，**需要先和前端同学一起定接口**，不能照搬前端现有的假数据结构。
