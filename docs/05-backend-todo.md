# 后端任务清单

> 目标：让前端在 `http://localhost:3000` 上完整可用。
> 接口细节以 **[01-api-contract.md](01-api-contract.md)** 为准；本文件只讲**顺序、范围和验收**。

---

## 建议的技术栈（沿用参考实现，避免重新踩坑）

| 组件 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.0.5 | 参考实现用的就是它 |
| JDK | 17 | Spring Boot 3 要求 |
| MyBatis-Plus | 3.5.3.1 | 注意：Spring Boot 3 要用 `mybatis-plus-boot-starter`，参考实现已验证可跑 |
| Druid | 1.2.18 | `druid-spring-boot-3-starter`（**必须是 `-3-` 版本**） |
| MySQL Connector | 8.0.28 | —— |
| JWT | jjwt 0.9.1 + jaxb-api 2.3.0 | jjwt 0.9.x 在 JDK 9+ 需要额外引 jaxb-api |
| HTML 解析 | jsoup 1.16.1 | 上传时把 `htmlContent` 转 Markdown |
| Markdown 渲染 | commonmark 0.21.0（+ tables / heading-anchor 扩展） | 数据集详情用 |
| 图片处理 | thumbnailator 0.4.19 | 可选 |
| Lombok | 1.18.28 | 可选 |

> 上述版本见 `reference/legacy-backend/pom.xml`，可直接复用依赖清单。

---

## Phase 0 · 环境准备

- [ ] 装 JDK 17、Maven、MySQL 8.0
- [ ] 按 README 第 1.2 节导入 `database/schema.sql` + `seed.sql`
- [ ] 确认库里有 14 张表、2 个用户（`lcy` / `qmn`）、5 个数据集、2 篇文献
- [ ] `cd frontend && npm install && npm run dev`，确认 3000 端口能打开登录页
- [ ] 在 `backend/` 下初始化 Spring Boot 工程（可用 <https://start.spring.io>）

**验收**：`mysql -u root -p eavi_system -e "SHOW TABLES;"` 输出 14 行。

---

## Phase 1 · 骨架 + 认证（先打通登录）

> **为什么先做这个**：前端所有受保护页面都靠 `localStorage.token` 判断；
> 而且后面所有"当前用户是谁"的逻辑都要从 JWT 解析 `userId`。先把它做对，后面才不会返工。

- [ ] `Result` 统一响应类 + `ResultCodeEnum`（`code:200` 为成功）
- [ ] `JwtHelper`：签发/解析/校验过期。**签名密钥从配置读，不要硬编码**
- [ ] `MD5Util`：与库中已有的 MD5 值兼容（十六进制小写）
- [ ] CORS 配置（前端 3000 → 后端 8080，跨域直连）
- [ ] **`POST /auth/login`** —— 按契约 1.1
- [ ] **`POST /auth/logout`** —— ⚠️ 请求体必须**非必填**（契约 1.2）
- [ ] **`GET /auth/profile`** —— ⚠️ 参考后端里没有，**必须新建**（契约 1.3）
- [ ] 一个"从 `Authorization` 头解析当前 userId"的工具方法（后面每个接口都要用）

**验收**：
1. 用 `lcy` / `123456` 从前端登录页登录成功，跳转到 `/home`
2. 刷新页面仍保持登录
3. 用错误密码登录，**界面显示中文错误提示**（不是 `passwordError`）
4. 点退出，后端**不返回 400**

---

## Phase 2 · 任务字典 + 数据集读取

- [ ] **`GET /tasks`**（契约 5.1）
- [ ] **`GET /datasets`**（契约 2.1）—— 分页、搜索、排序、按任务过滤
- [ ] **`GET /datasets/{id}`**（契约 2.2）—— ⚠️ 注意**双层 `data.data`**，字段用驼峰

**要点**：
- 列表接口的字段名和详情接口**完全不同**（`task_names` vs `taskNames`，`downloadCount` vs `downloadcount`）。
  **照抄契约，不要统一命名。**
- ⚠️ `GROUP BY` 要写全。参考实现的 `selectMyPage` 在 MySQL 8 的 `ONLY_FULL_GROUP_BY`
  下会直接报错，别照抄。

**验收**：
1. 数据集列表显示 5 条，标签、上传者、下载次数都正确
2. 搜索 "VOC" 能筛出 1 条
3. 按任务筛选（"目标检测"）结果正确

---

## Phase 3 · 数据集文档与图片

- [ ] **`GET /datasets/{id}/markdown-content`**（契约 2.3）
- [ ] **`GET /datasets/{id}/images/{imagePath}`**（契约 2.4）

**要点（这一阶段最关键）**：

1. 读 `eavi-static/data/datasets/<base_address>/` 下的 `.md` 文件，转成 HTML 返回。
2. 把 Markdown 里的图片引用改写成 `/datasets/{id}/images/{文件名}`，让浏览器能取到图。
3. 🔴 **`base_address` 为 NULL 时必须返回业务错误，不能 500。**
   种子数据 5 个数据集里只有 `PASCAL VOC 2007` 有本地文档，其余 4 个 `base_address` 是 NULL。
   参考实现会抛 `IllegalArgumentException` → 前端收到 500。
   **请返回 `{"code": 404, "message": "该数据集尚未上架本地文档"}`。**
4. 🔴 图片接口必须**防路径穿越**（拒绝 `../`）。用
   `imagePath.normalize().startsWith(datasetBaseDir)` 校验。

**验收**：
1. 点进「PASCAL VOC 2007」，正文和两张图都正常渲染
2. 点进「ImageNet」，**界面显示中文提示而不是白屏/报错**
3. 手动请求 `GET /datasets/1/images/../../../../etc/passwd`，被拒绝

---

## Phase 4 · 数据集上传

- [ ] **`POST /datasets/upload`**（契约 2.5）

**要点**：

1. 需要 `Authorization` 头来确定 `user_id`（`dataset.user_id` 是 `NOT NULL` 外键）。
2. 支持两种模式：`htmlContent`（结构化编辑）或 `markdownFile`（Markdown 上传）。
3. 🔴 **前端提交的 `htmlContent` 里，图片 `src` 可能是 `data:image/...;base64,...`**。
   前端已放弃 HTML→Markdown 转换，**所以后端要自己做**：
   - 用 jsoup 解析 HTML
   - 把 base64 图片解码落盘到 `eavi-static/data/datasets/<新目录>/`
   - 把 `src` 替换成文件名
   - 转成 Markdown 写盘
4. 目录名要**唯一**且**不含中文/特殊字符**（`dataset.base_address` 有唯一约束）。
5. `taskIds` 是 **JSON 数组的字符串**，形如 `"[1,2,3]"`，需要手动解析。
6. 同时要写 `dataset_task` 关联表。

**验收**：
1. 从界面上传一个新数据集（含 2 张图片），列表里出现
2. 点进详情，正文和图都在
3. `eavi-static/data/datasets/` 下出现了新目录
4. 再上传同名数据集，被拒绝并给出中文提示（唯一约束不能变成 500）

---

## Phase 5 · 文献模块

- [ ] **`GET /papers`**（契约 3.1）
- [ ] **`GET /papers/{id}`**（契约 3.2）
- [ ] 9 个关联增删端点（契约 3.3）

**要点**：

- `GET /papers/{id}` 的 `repositories` 来自 **`paper_code` 表**，不是 `repository` 表。
- `datasets` 是**库内数据集 + 外部链接的并集**，两种形态靠 `datasetId` 是否为 null 区分。
- `tasks` 返回**字符串数组**（任务名）。
- 文献 `status` 必须返回**中文**（`已阅读`/`未阅读`/`正在阅读`）。
- ⚠️ **`Paper` 实体和 `PaperTask` 等关联实体缺 `@TableId`**，见
  `docs/02-database-design.md` 第 6.4 节。**建议在写之前先给这些实体补上主键注解**，
  否则 `deleteById` / `selectById` 这类方法不可用。
- `DELETE` 请求**带请求体**（`{taskName}` / `{codeName}` / `{datasetId}`），
  用 `@RequestBody Map<String,Object>` 接收。

**验收**：
1. 文献列表显示 2 条
2. 进入详情，任务 / 代码 / 数据集 / 结果四个区块都有数据
3. 加一个任务、删一个任务，刷新后状态正确
4. 加一个外部数据集链接，能显示出来
5. 上传一个 `.md` 笔记，能通过 `/data/paper_note/xxx` 打开

---

## Phase 6 · 文献目录（含权限）

- [ ] **`GET /folders/tree`**（契约 4.1）
- [ ] **`POST /folders`**（契约 4.2）
- [ ] **`POST /folders/{folderId}/papers`**（契约 4.3）
- [ ] **删除目录**：`DELETE /folders/{id}` / `POST /folders/{id}/delete` / `POST /folders/delete`（契约 4.4）

**要点**：

- 🔴 **前端不传 `userId`，后端必须从 JWT 解析。**
  参考实现是 `if (userId != null) qw.eq(Folder::getUserId, userId)` ——
  `userId` 为 null 时会**返回全部用户的目录，属于越权**。**必须修正。**
- 返回的是**嵌套好的树**（`children` 递归），不是扁平列表。
  前端只渲染 **3 层**，注意别产生更深的层级。
- 删除语义（建议沿用参考实现）：
  - `我的文库` 根目录不允许删除
  - 删除某目录时，其子树内的文献**移动到 `我的文库`**，再删除整棵子树

**验收**：
1. 目录树显示「我的文库 → 精读/略读 → 计算机视觉/自然语言处理」
2. 新建根目录、子目录都成功
3. 删除「计算机视觉」，其中的文献回到「我的文库」
4. 🔴 **用 `qmn` 登录，看不到 `lcy` 的目录**（权限验收，最容易漏）

---

## Phase 7 · 静态资源

- [ ] `/data/paper_note/{filename}`
- [ ] `/data/paper/{filename}`
- [ ] `/data/paper_code/{filename}`

配置方式：把 `eavi-static/` 挂成静态资源目录。

```yaml
spring:
  web:
    resources:
      static-locations:
        - file:${EAVI_STATIC_ROOT:../eavi-static/}
        - classpath:/static/
```

**验收**：上传笔记后 `window.open` 能打开文件。

---

## Phase 8 · 加固与自测

- [ ] 全库跑一遍 README 第 8 节的 **13 条验收标准**
- [ ] 所有报错信息是**中文**
- [ ] `git grep -nE "[a-zA-Z]:[/\\\\]"` 无硬编码盘符路径
- [ ] 没有把密码写进任何被提交的文件
- [ ] 用 `mvn package` 能打包成功
      ⚠️ 注意：参考实现的 `mvn package -DskipTests` **会失败**，
      因为 `-DskipTests` 仍然编译测试代码，而有个测试文件的方法签名早已过时。
      要么修测试，要么用 **`mvn package -Dmaven.test.skip=true`**。
- [ ] 让数据库同学在 macOS 上 clone 后启动一次，确认无硬编码

---

## 常见坑速查

| 坑 | 症状 | 解法 |
|---|---|---|
| `@RequestBody` 必填但前端不发 body | 登出返回 400 | 加 `required = false` |
| `GROUP BY` 不完整 | MySQL 8 报 `ONLY_FULL_GROUP_BY` 错误 | 补全分组列或 `ANY_VALUE()` |
| `base_address` 为 NULL 直接拿去拼路径 | 详情页 500 | 先判 NULL 返回业务错误 |
| 实体没有 `@TableId` 也没有 `id` 字段 | 启动打 WARN；`selectById`/`deleteById` 不可用 | 补 `@TableId` |
| `@RequestBody` 接 `Integer` 但前端传 `Long` | 类型转换异常 | 用 `Map<String,Object>` 接再手动转 |
| `mvn package -DskipTests` 失败 | 测试编译不过 | 用 `-Dmaven.test.skip=true` |
| 前端拿不到值但后端"明明返回了" | 字段名大小写/下划线不一致 | 对照契约逐个核对，**不要统一命名** |
| 中文报错变成乱码 | 响应编码问题 | 全链路 UTF-8，MySQL 连接串带 `characterEncoding=utf8` |
| 路径穿越 | 图片接口可读任意文件 | `normalize()` + `startsWith()` 校验 |

---

## 交付前最后确认

```
[ ] 13 条验收标准全过
[ ] 权限验收（qmn 看不到 lcy 的目录）通过
[ ] 无硬编码路径、无明文密码
[ ] 报错信息全中文
[ ] README 更新：把「后端待实现」改成「后端已完成」，并补上启动说明
[ ] docs/01-api-contract.md 如果有偏离，同步更新并说明原因
```
