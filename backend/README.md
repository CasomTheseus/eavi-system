# backend/ —— 后端工作区（待实现）

这个目录目前**是空的**，等后端同学来实现。

## 从这里开始

按顺序读：

1. **[../README.md](../README.md)** —— 项目全貌、五分钟跑起来、跨平台铁律
2. **[../docs/01-api-contract.md](../docs/01-api-contract.md)** —— ⭐ 接口契约（唯一权威版本）
3. **[../docs/05-backend-todo.md](../docs/05-backend-todo.md)** —— ⭐ 你的任务清单，按 Phase 推进
4. **[../database/schema.sql](../database/schema.sql)** —— 数据库表结构（14 张表，已建好，不要改）

## 技术栈

Spring Boot 3.0.5 + JDK 17 + MyBatis-Plus 3.5.3.1 + MySQL 8 + Druid + JWT(jjwt)

## 三条硬性约束

1. **端口必须是 8080。** 前端所有请求都硬编码了这个地址。
   （前端 Vite 跑在 3000，跨域直连 8080，需要开 CORS。）

2. **接口字段名严格按契约返回，不要"统一命名"。**
   同一个"任务名列表"，列表接口叫 `task_names`，详情接口叫 `taskNames`；
   "下载次数"在列表叫 `downloadCount`、在详情叫 `downloadcount`。
   这不是笔误，是前端实际读取的键名。

3. **不允许硬编码盘符路径和密码。**
   历史版本同时存在 `f:/Axidian/...`、`e:/UI/...` 三种路径，
   导致换台机器就跑不起来。详见 [../docs/03-cross-platform.md](../docs/03-cross-platform.md)。

## 参考实现

`../reference/legacy-backend/` 里有一份学长给的可用实现，**可以读，不要照抄**。
它有 6 处与前端契约冲突、以及若干已知缺陷，清单见
**[../reference/README.md](../reference/README.md)**。

## 建议的启动顺序

```
Phase 1  骨架 + 认证         → 打通登录（先做这个，后面都要用 userId）
Phase 2  任务字典 + 数据集读取
Phase 3  数据集文档与图片     → 注意 base_address 为 NULL 的情况
Phase 4  数据集上传           → 需要把 HTML 里的 base64 图片落盘
Phase 5  文献模块
Phase 6  文献目录             → 注意权限：只返回当前用户的目录
Phase 7  静态资源
Phase 8  加固与自测
```

## 工程结构建议

```
backend/
├── pom.xml
├── README.md                    ← 本文件
└── src/
    ├── main/
    │   ├── java/com/ateavi/
    │   │   ├── Main.java
    │   │   ├── controller/      5 个控制器
    │   │   ├── service/         + service/impl/
    │   │   ├── mapper/          MyBatis-Plus Mapper 接口
    │   │   ├── pojo/            实体类（对应数据库表）
    │   │   │   ├── vo/          返回给前端的视图对象
    │   │   │   └── DTO/         内部传输对象
    │   │   └── utils/           Result、JwtHelper、MD5Util
    │   └── resources/
    │       ├── application.yaml
    │       ├── application-local.yaml   ← 本机配置，不入库
    │       └── mapper/                  MyBatis XML
    └── test/
```

> ⚠️ 写测试时注意：参考实现有个测试文件调用了**签名已经变化**的方法，
> 导致 `mvn package -DskipTests` **仍然失败**（`-DskipTests` 会编译测试代码）。
> 用 `-Dmaven.test.skip=true` 跳过编译，或者把测试写对。
