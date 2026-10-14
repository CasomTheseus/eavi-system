# 跨平台开发约定（macOS + Windows）

> **为什么要有这份文档**
> 本项目上一版之所以在三个人之间难以流转，一个核心原因就是路径和配置被写死进了代码。
> 三个人、三台机器、两套操作系统，硬编码的路径必然对不上。这份文档把规则说清楚，
> 目的是让任何一个人在**全新 clone 之后不改一行代码**就能跑起来。

---

## 一、团队环境

| 成员 | 职责 | 操作系统 | 主要工具 |
|---|---|---|---|
| 成员 A | 数据库 | **macOS** | MySQL 8、命令行 |
| 成员 B | 前端 | **macOS** | Node.js 18+、Vite |
| 成员 C | **后端** | **Windows** | JDK 17、Maven、IDEA |

**验收动作**：后端写完后，成员 A 在自己的 macOS 上 `git clone` → 按 README 跑 → 能启动。
这一步是检验有没有硬编码的最好办法。

---

## 二、🔴 硬编码路径（最高优先级）

### 2.1 历史教训

项目原始代码里同时存在这些绝对路径：

```java
// DatasetServiceImpl.java
@Value("${eavi.dataset.storage-path:f:/Axidian/其他工作/UI/eavi-static/data/datasets/}")

// DatasetFilesScanServiceImpl.java
@Value("${eavi.dataset.storage-path:f:/Axidian/小组项目工作/UI/eavi-static/data/datasets/}")

// PaperServiceImpl.java
@Value("${eavi.paper-note.storage-path:f:/Axidian/小组项目工作/UI/eavi-static/data/paper_note/}")
```

问题有三层：

1. **盘符 + 绝对路径**：`f:/` 在另一台机器上根本不存在。
2. **同一个配置项有两套默认值**：`其他工作` 和 `小组项目工作` 两个不同目录，
   同一个 `@Value` 的默认值都不一致。
3. **配置文件与代码默认值双重定义**：`application.yaml` 里也配了一遍，
   两个地方会打架——你改了 yaml，代码里的默认值还是旧的。

### 2.2 正确做法

**配置只写一处，路径用相对路径，允许环境变量覆盖。**

```yaml
# backend/src/main/resources/application.yaml
eavi:
  # 相对启动目录（backend/）解析
  static-root: ${EAVI_STATIC_ROOT:../eavi-static/}
```

```java
@Value("${eavi.static-root}")
private String staticRoot;        // ✅ 不给默认值，默认值只放在 yaml 里

private Path datasetDir(String baseAddress) {
    return Paths.get(staticRoot, "data", "datasets", baseAddress);   // ✅ Paths.get 自动处理分隔符
}
```

```java
// ❌ 不要这样
private static final String ROOT = "e:/UI/eavi-static/";
String path = ROOT + baseAddress;                 // ❌ 手写路径拼接
```

本机路径不一致时，用**环境变量**覆盖，不要改代码：

```powershell
# Windows PowerShell
$env:EAVI_STATIC_ROOT = "D:\workspace\eavi-system\eavi-static\"
```
```bash
# macOS
export EAVI_STATIC_ROOT="$HOME/work/eavi-system/eavi-static/"
```

### 2.3 自查方法

提交前运行（PowerShell）：

```powershell
git grep -nE "[a-zA-Z]:/|[a-zA-Z]:\\\\" -- "*.java" "*.yaml" "*.yml" "*.properties" "*.xml"
```

如果搜出盘符路径，改掉再提交。

---

## 三、🔴 凭据管理

### 3.1 历史教训

原始仓库里提交了明文数据库密码，形式包括：

- `application.yaml` 里的 `password: <明文密码>`
- PowerShell 脚本里 `mysql -uroot -p<明文密码>`
- JWT 签名密钥 `tokenSignKey: <一个可猜的固定字符串>`

**本仓库已全部清理为环境变量占位符。请不要再次引入。**

> ⚠️ 注意：JWT 签名密钥一旦泄漏，任何人都能**伪造任意用户的登录令牌**（包括管理员）。
> 这类密钥必须是随机长字符串，并且**每个部署环境各不相同**。
> 生成方法：`openssl rand -base64 48`（macOS）或
> `[Convert]::ToBase64String((1..48|%{Get-Random -Max 256}))`（PowerShell）。

### 3.2 正确做法

```yaml
spring:
  datasource:
    druid:
      url: jdbc:mysql://localhost:3306/eavi_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
      username: ${MYSQL_USER:root}
      password: ${MYSQL_PASSWORD}          # ✅ 没有默认值，缺失时启动即报错，不会静默用错密码

jwt:
  token:
    tokenSignKey: ${JWT_SIGN_KEY}          # ✅ 同上
```

另有**本机私有配置**方案（`.gitignore` 已排除）：

```yaml
# backend/src/main/resources/application-local.yaml   ← 不会被提交
spring:
  datasource:
    druid:
      password: 你的密码
```

启动：`mvn spring-boot:run -Dspring-boot.run.profiles=local`

### 3.3 自查方法

```powershell
git diff --cached | Select-String -Pattern "password|secret|tokenSignKey|apiKey"
```

---

## 四、换行符（CRLF / LF）

### 4.1 问题

macOS 用 `LF`（`\n`），Windows 默认用 `CRLF`（`\r\n`）。
不统一的话，任何一次跨平台提交都会显示"整个文件都被改了"，`git diff` 完全不可读。

### 4.2 已配置的策略

见仓库根的 `.gitattributes`：

- **版本库里一律存 LF**（`* text=auto eol=lf`）
- **只有 `*.ps1` / `*.bat` / `*.cmd` 强制 CRLF**——因为 Windows PowerShell
  对纯 LF 的 `.ps1` 脚本会解析异常
- 图片、PDF、jar、zip 标记为 `binary`，不做任何转换

### 4.3 你必须做的事

**不需要做任何事。** 只有一个例外：如果你的编辑器弹窗问"要不要转换换行符"，**选不要**。

如果已经不小心提交了 CRLF 文件，用这个命令修正：

```powershell
git add --renormalize .
git commit -m "统一换行符为 LF"
```

---

## 五、文件与路径

| 事项 | 要求 | 原因 |
|---|---|---|
| 路径拼接 | `Paths.get(a, b)` 或 `new File(parent, child)` | 自动适配 `/` 与 `\` |
| 文件分隔符比较 | 用 `Path` 对象比较，不要拿字符串比 `"/"` | Windows 是 `\` |
| **上传文件命名** | **不要用中文名** | 旧代码里有 `1768635308827_合成疼痛表情数据：AI驱动临床评估新范式.pdf`；中文名 + 冒号在 URL 编码、跨系统传输、Windows 保留字符上都有坑。统一 `时间戳_清理后的文件名` |
| 文件名大小写 | 不要靠大小写区分文件 | macOS / Windows 文件系统默认不区分，Linux 服务器区分 |
| Windows 保留字符 | 文件名不能含 `\ / : * ? " < > |` | 尤其在把标题当文件名用时 |
| 源码编码 | 全项目 **UTF-8** | IDEA：`File → Settings → Editor → File Encodings`，三处都设 UTF-8 |
| 文本文件 | 保存时**不带 BOM** | BOM 会污染配置文件首行 |

---

## 六、依赖与构建产物

### 6.1 绝不提交 `node_modules/`

`.gitignore` 已排除。原因：`node_modules` 里有**平台专用二进制**。

例如 `esbuild` 会按平台安装不同的包：

```
@esbuild/darwin-arm64    ← macOS（Apple Silicon）
@esbuild/darwin-x64      ← macOS（Intel）
@esbuild/win32-x64       ← Windows
```

在 macOS 上装好的 `node_modules` 拷到 Windows，运行时找不到 `win32-x64` 的二进制会直接报错。

**每人自己 `npm install`。** 好消息是 `frontend/package-lock.json` 是 lockfileVersion 3，
已包含全部平台的可选依赖（本项目用的是 rollup 3.29.5，它是纯 JS 实现、没有平台专用原生包），
所以 `npm ci` 在 Windows 上可以直接用。

> 补充：`package-lock.json` 里的下载地址指向 `registry.npmmirror.com`（国内镜像）。
> 如果你在国外或有代理，可以 `npm config set registry https://registry.npmjs.org/` 后重新 `npm install`。

### 6.2 其他不应提交的内容

`dist/`、`target/`、`*.jar`、`*.log`、`.idea/`、`.vscode/` —— `.gitignore` 均已配置。

---

## 七、其他平台差异

| 事项 | Windows | macOS / Linux | 统一做法 |
|---|---|---|---|
| 时区 | 系统时区可能不同 | 同 | JDBC URL 带 `serverTimezone=Asia/Shanghai` |
| 换行 | CRLF | LF | 见第四节 |
| 环境变量 | `$env:NAME="v"` | `export NAME=v` | 配置文件与脚本分开写 |
| 路径分隔符 | `\` | `/` | 用 `Paths.get()` |
| 命令行 | PowerShell / cmd | bash / zsh | `scripts/` 下提供两套脚本 |
| MySQL 客户端命令 | `mysql.exe -u root -p` | `mysql -u root -p` | 见 README 第 1.2 节 |
| 文件权限 | 无 Unix 权限位 | 有 | 不要把脚本权限写进 git（`chmod` 后 `git update-index --chmod=+x` 可选） |

---

## 八、提交前检查清单

```powershell
# 1. 有没有盘符绝对路径
git grep -nE "[a-zA-Z]:[/\\\\]" -- "*.java" "*.yaml" "*.yml" "*.properties"

# 2. 有没有凭据
git diff --cached | Select-String -Pattern "password|secret|tokenSignKey|apikey"

# 3. 有没有误提交大目录
git status --short | Select-String "node_modules|dist/|target/"

# 4. 换行符是否正常
git ls-files --eol | Select-String -NotMatch "lf"
```

四条都干净，再提交。
