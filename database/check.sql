-- ============================================================
-- EAVI 数据库完整性自检脚本（schema.sql + seed.sql 导入后执行）
-- 运行：mysql -u root -p eavi_system < check.sql
-- ============================================================

USE `eavi_system`;

-- 1. 各表记录数
SELECT 'role' AS tbl, COUNT(*) AS cnt FROM `role`
UNION ALL SELECT 'user', COUNT(*) FROM `user`
UNION ALL SELECT 'status', COUNT(*) FROM `status`
UNION ALL SELECT 'task', COUNT(*) FROM `task`
UNION ALL SELECT 'dataset', COUNT(*) FROM `dataset`
UNION ALL SELECT 'dataset_task', COUNT(*) FROM `dataset_task`
UNION ALL SELECT 'paper', COUNT(*) FROM `paper`
UNION ALL SELECT 'paper_task', COUNT(*) FROM `paper_task`
UNION ALL SELECT 'paper_code', COUNT(*) FROM `paper_code`
UNION ALL SELECT 'paper_dataset', COUNT(*) FROM `paper_dataset`
UNION ALL SELECT 'result', COUNT(*) FROM `result`
UNION ALL SELECT 'folder', COUNT(*) FROM `folder`
UNION ALL SELECT 'folder_paper', COUNT(*) FROM `folder_paper`;

-- 2. 外键引用完整性：应全部为空
SELECT '孤儿数据集' AS check_name, COUNT(*) AS problem_cnt
FROM `dataset` d LEFT JOIN `user` u ON u.user_id = d.user_id
WHERE u.user_id IS NULL
UNION ALL
SELECT '孤儿文件夹', COUNT(*)
FROM `folder` f LEFT JOIN `user` u ON u.user_id = f.user_id
WHERE f.user_id IS NOT NULL AND u.user_id IS NULL
UNION ALL
SELECT '文件夹父级不存在', COUNT(*)
FROM `folder` c LEFT JOIN `folder` p ON p.folder_id = c.parent_id
WHERE c.parent_id IS NOT NULL AND p.folder_id IS NULL;

-- 3. 唯一约束复核：任务名、用户名不应重复
SELECT '重复任务名' AS check_name, COUNT(*) AS problem_cnt
FROM (SELECT `name` FROM `task` GROUP BY `name` HAVING COUNT(*) > 1) t
UNION ALL
SELECT '重复用户名', COUNT(*)
FROM (SELECT `username` FROM `user` GROUP BY `username` HAVING COUNT(*) > 1) u;

-- 4. 一文献多数据集 / 多代码验证（应 > 0）
SELECT '多数据集文献数' AS check_name, COUNT(*) AS paper_cnt
FROM (SELECT `paper_id` FROM `paper_dataset` GROUP BY `paper_id` HAVING COUNT(*) > 1) pd
UNION ALL
SELECT '多代码文献数', COUNT(*)
FROM (SELECT `paper_id` FROM `paper_code` GROUP BY `paper_id` HAVING COUNT(*) > 1) pc;

-- 5. 文献详情聚合样例（模拟后端查询）
SELECT p.id,
       p.title,
       GROUP_CONCAT(DISTINCT t.name ORDER BY t.id SEPARATOR ',') AS tasks,
       COUNT(DISTINCT d.dataset_id) AS dataset_cnt
FROM `paper` p
LEFT JOIN `paper_task` pt     ON pt.paper_id = p.id
LEFT JOIN `task` t            ON t.id = pt.task_id
LEFT JOIN `paper_dataset` pd  ON pd.paper_id = p.id
LEFT JOIN `dataset` d         ON d.dataset_id = pd.dataset_id
GROUP BY p.id, p.title
ORDER BY p.id;
