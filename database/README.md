# database/ —— MySQL 建库脚本

> ⚠️ **本目录只能有一份权威脚本。**
>
> 项目历史上有过 **四份互相矛盾的建库脚本**，导致"到底用哪份初始化"长期扯皮。
> 现在只认 `schema.sql` + `seed.sql`。

## 文件清单

| 文件 | 说明 | 能否使用 |
|---|---|---|
| `schema.sql` | ⭐ **唯一权威**建库脚本，14 张表，含主外键/唯一约束/索引/注释 | ✅ 用这个 |
| `seed.sql` | ⭐ 配套演示数据，可重复执行（幂等） | ✅ 用这个 |
| `check.sql` | 完整性自检（孤儿外键、重复数据、聚合样例） | ✅ 可选 |
| `legacy/schema_v1_original.sql` | 历史备份，**只有 9 张表** | ❌ 仅供追溯 |
| `legacy/seed_v1_original.sql` | 历史备份 | ❌ 仅供追溯 |

`legacy/` 里那份是**已知有缺陷的旧版**：缺 `role`、`user`、`status`、`dataset_task`、`paper_code`
五张表，用它初始化会**连登录都跑不通**。保留它只是为了能查历史差异。

## 初始化

```bash
# macOS / Linux
mysql -u root -p < schema.sql
mysql -u root -p < seed.sql
mysql -u root -p eavi_system < check.sql      # 可选
```

```powershell
# Windows PowerShell
Get-Content .\schema.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
Get-Content .\seed.sql   -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4
```

⚠️ `schema.sql` 会 **DROP 并重建** `eavi_system` 库内的所有同名表。
首次执行即可，之后重复跑会清空数据。

## 初始化后的预期结果

- **14 张表**：`role` `status` `user` `task` `dataset` `dataset_task` `paper` `paper_task`
  `paper_code` `paper_dataset` `result` `folder` `folder_paper` `repository`
- **2 个账号**：`lcy` / `123456`（刘晨宇）、`qmn` / `123456`（齐梦男），密码存 MD5
  `e10adc3949ba59abbe56e057f20f883e`
- **6 个任务**、**5 个数据集**、**2 篇文献**、**5 个文件夹**（树形）
- 只有 `PASCAL VOC 2007` 的 `base_address = 'VOC2007'`（有本地文档），
  其余 4 个数据集 `base_address = NULL`（仅元数据）

## 数据结构要点

- `paper_code` 支持**一文献多代码仓库**，唯一键 `(paper_id, code_name)`。
- `paper_dataset` 同时支持**库内数据集**（`dataset_id`）和**外部链接**（`dataset_url`），
  用生成列 `ref_code` + 唯一键在数据库层防止重复关联。
- `dataset.base_address` **可为 NULL**，表示本地文档未上架。
  后端详情接口**必须处理**这种情况（见 `../docs/01-api-contract.md` 第 2.3 节）。
- `user` / `dataset` / `folder` 有逻辑删除字段 `is_deleted`；
  `user` / `dataset` 有乐观锁字段 `version`。

完整设计说明见 **[../docs/02-database-design.md](../docs/02-database-design.md)**。

## 如果要改表结构

1. **直接改 `schema.sql`**。不要新建 `schema_v2.sql` / `schema_new.sql` / `schema_final.sql`
   ——这正是当初产生四份矛盾脚本的原因。
2. 提交信息写清楚改了什么、为什么。
3. 同步更新 `../docs/02-database-design.md`。
4. 知会数据库同学。

## 已知遗留项

- `repository` 表是**遗留表**，当前后端完全未使用（`/papers/{id}/repositories`
  实际走的是 `paper_code` 表）。确认废弃后可在下一版中删除。
- 7 个关联/字典实体类缺 MyBatis-Plus 可见主键，详见
  `../docs/02-database-design.md` 第 6.4 节。
