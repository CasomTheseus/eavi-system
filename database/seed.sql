-- ============================================================
-- EAVI 团队数据管理系统 —— 演示数据（规范化 v2）
-- 适用：在 schema.sql 创建的空库上执行；可重复执行（幂等）。
-- 整理日期：2026-09-06
-- ============================================================

USE `eavi_system`;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 角色
-- ------------------------------------------------------------
INSERT INTO `role` (`role_id`, `role`) VALUES
  (1, '管理员'),
  (2, '数据集管理员'),
  (3, '用户管理员'),
  (4, '设备管理员'),
  (5, 'EAVI团队成员')
ON DUPLICATE KEY UPDATE `role` = VALUES(`role`);

-- ------------------------------------------------------------
-- 数据集状态
-- ------------------------------------------------------------
INSERT INTO `status` (`status_id`, `status`) VALUES
  (1, 'active'),
  (2, 'processing'),
  (3, 'error'),
  (4, 'archived')
ON DUPLICATE KEY UPDATE `status` = VALUES(`status`);

-- ------------------------------------------------------------
-- 用户（初始密码均为 123456，MD5: e10adc3949ba59abbe56e057f20f883e）
-- lcy = 刘晨宇，qmn = 齐梦男
-- ------------------------------------------------------------
INSERT INTO `user`
  (`user_id`, `username`, `email`, `name`, `role_id`, `avatar`, `password`, `version`, `is_deleted`)
VALUES
  (1001, 'lcy', NULL, '刘晨宇', 1, NULL, 'e10adc3949ba59abbe56e057f20f883e', 1, 0),
  (1002, 'qmn', NULL, '齐梦男', 1, NULL, 'e10adc3949ba59abbe56e057f20f883e', 1, 0)
ON DUPLICATE KEY UPDATE
  `email` = VALUES(`email`),
  `name` = VALUES(`name`),
  `role_id` = VALUES(`role_id`),
  `password` = VALUES(`password`),
  `version` = VALUES(`version`),
  `is_deleted` = VALUES(`is_deleted`);

-- ------------------------------------------------------------
-- AI 任务类别
-- ------------------------------------------------------------
INSERT INTO `task` (`id`, `name`) VALUES
  (1, '图像分类'),
  (2, '目标检测'),
  (3, '实例分割'),
  (4, '机器翻译'),
  (5, '文本摘要'),
  (6, '语言建模')
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`);

-- ------------------------------------------------------------
-- 数据集
-- 说明：VOC2007 在本仓库 eavi-static/data/datasets/VOC2007 下有真实
--       Markdown/图片，可完整打开详情页；其余为“未上架文档”的元数据，
--       base_address 置 NULL，true_address 保存外部真实地址。
-- ------------------------------------------------------------
INSERT INTO `dataset`
  (`dataset_id`, `dataset_name`, `description`, `status_id`, `size_formatted`, `file_count`,
   `user_id`, `downloadCount`, `created_time`, `updated_time`, `true_address`, `base_address`,
   `version`, `is_deleted`)
VALUES
  (1, 'PASCAL VOC 2007',
   'PASCAL 视觉对象类挑战赛数据集，包含 20 个类别、9963 张图像，用于目标检测、图像分类与实例分割等任务。',
   1, '460 MB', 9963, 1002, 156,
   '2024-03-11 14:09:04', '2024-03-11 14:09:04',
   '团队服务器（梦男师兄服务器），线下联系获取', 'VOC2007', 1, 0),
  (2, 'ImageNet',
   '大规模图像分类数据集。',
   1, '150GB', 14197122, 1001, 100000,
   '2024-01-01 00:00:00', '2024-01-01 00:00:00',
   'https://image-net.org', NULL, 1, 0),
  (3, 'COCO',
   '目标检测与实例分割数据集。',
   1, '25GB', 330000, 1001, 80000,
   '2024-01-01 00:00:00', '2024-01-01 00:00:00',
   'https://cocodataset.org', NULL, 1, 0),
  (4, 'WMT14',
   '机器翻译英法数据集。',
   1, '5GB', 5000000, 1001, 5000,
   '2024-01-01 00:00:00', '2024-01-01 00:00:00',
   'https://www.statmt.org/wmt14', NULL, 1, 0),
  (5, 'WikiText',
   '语言建模数据集。',
   1, '1.2GB', 103000, 1001, 6000,
   '2024-01-01 00:00:00', '2024-01-01 00:00:00',
   'https://blog.einstein.ai/the-wikitext-long-term-dependency-language-modeling-dataset/', NULL, 1, 0)
ON DUPLICATE KEY UPDATE
  `dataset_name` = VALUES(`dataset_name`),
  `description` = VALUES(`description`),
  `status_id` = VALUES(`status_id`),
  `size_formatted` = VALUES(`size_formatted`),
  `file_count` = VALUES(`file_count`),
  `user_id` = VALUES(`user_id`),
  `downloadCount` = VALUES(`downloadCount`),
  `created_time` = VALUES(`created_time`),
  `updated_time` = VALUES(`updated_time`),
  `true_address` = VALUES(`true_address`),
  `base_address` = VALUES(`base_address`),
  `version` = VALUES(`version`),
  `is_deleted` = VALUES(`is_deleted`);

-- ------------------------------------------------------------
-- 数据集-任务关联
-- ------------------------------------------------------------
INSERT IGNORE INTO `dataset_task` (`dataset_id`, `task_id`) VALUES
  (1, 1), (1, 2), (1, 3),   -- VOC2007：图像分类/目标检测/实例分割
  (2, 1),                   -- ImageNet：图像分类
  (3, 2), (3, 3),           -- COCO：目标检测/实例分割
  (4, 4),                   -- WMT14：机器翻译
  (5, 6);                   -- WikiText：语言建模

-- ------------------------------------------------------------
-- 文献
-- ------------------------------------------------------------
INSERT INTO `paper`
  (`id`, `title`, `authors`, `venue`, `abstract`, `upload_time`,
   `citations`, `impact`, `status`, `paper_url`, `note_url`)
VALUES
  (1, 'Deep Learning for Computer Vision: A Comprehensive Survey',
   '张三, 李四, 王五', 'IEEE TPAMI', '计算机视觉方向综述。', '2024-01-15 00:00:00',
   156, 'IF:17.73', '已阅读', 'https://arxiv.org/abs/1904.05049', NULL),
  (2, 'Attention Mechanisms in Natural Language Processing: Recent Advances',
   '李四, 赵六, 钱七', 'ACL 2024', '注意力机制综述。', '2024-01-10 00:00:00',
   89, '顶级会议', '正在阅读', 'https://arxiv.org/abs/1706.03762', NULL)
ON DUPLICATE KEY UPDATE
  `title` = VALUES(`title`),
  `authors` = VALUES(`authors`),
  `venue` = VALUES(`venue`),
  `abstract` = VALUES(`abstract`),
  `upload_time` = VALUES(`upload_time`),
  `citations` = VALUES(`citations`),
  `impact` = VALUES(`impact`),
  `status` = VALUES(`status`),
  `paper_url` = VALUES(`paper_url`),
  `note_url` = VALUES(`note_url`);

-- ------------------------------------------------------------
-- 文献-任务关联
-- ------------------------------------------------------------
INSERT IGNORE INTO `paper_task` (`paper_id`, `task_id`) VALUES
  (1, 1), (1, 2), (1, 3),   -- 综述涉及 CV 三类任务
  (2, 4), (2, 5), (2, 6);   -- 注意力机制涉及 NLP 三类任务

-- ------------------------------------------------------------
-- 文献-代码仓库关联（当前后端使用 paper_code 表）
-- ------------------------------------------------------------
INSERT IGNORE INTO `paper_code` (`paper_id`, `code_name`, `code_url`) VALUES
  (1, 'PyTorch 官方视觉实现', 'https://github.com/pytorch/vision'),
  (1, '检测算法参考实现', 'https://github.com/ultralytics/yolov5'),
  (2, 'Tensor2Tensor', 'https://github.com/tensorflow/tensor2tensor');

-- ------------------------------------------------------------
-- 文献-数据集关联（内部数据集 + 外部链接示例）
-- ------------------------------------------------------------
INSERT IGNORE INTO `paper_dataset` (`paper_id`, `dataset_id`, `dataset_url`, `display_name`) VALUES
  (1, 1, NULL, NULL),       -- 关联库内数据集：VOC2007
  (1, 3, NULL, NULL),       -- 关联库内数据集：COCO
  (1, NULL, 'https://crcv.ucf.edu/THUMOS14/', 'THUMOS 14（外部数据集）'),
  (2, 4, NULL, NULL),       -- WMT14
  (2, 5, NULL, NULL);       -- WikiText

-- ------------------------------------------------------------
-- 论文 Benchmark 结果
-- ------------------------------------------------------------
INSERT INTO `result`
  (`paper_id`, `task_id`, `dataset_id`, `model_name`, `metric_name`, `metric_value`, `is_sota`)
VALUES
  (1, 1, 2, 'ResNet-50', 'Top-1', '76.0', 0),
  (1, 2, 1, 'Faster R-CNN', 'mAP', '42.0', 0),
  (1, 2, 1, 'YOLOv5', 'mAP', '72.8', 1),
  (2, 4, 4, 'Transformer', 'BLEU', '41.8', 1);

-- ------------------------------------------------------------
-- 文献文件夹（属于用户 1001 = lcy）
-- ------------------------------------------------------------
INSERT INTO `folder`
  (`folder_id`, `name`, `parent_id`, `user_id`, `is_deleted`)
VALUES
  (100, '我的文库', NULL,   1001, 0),
  (101, '精读',     100,    1001, 0),
  (102, '略读',     100,    1001, 0),
  (103, '计算机视觉', 102,   1001, 0),
  (104, '自然语言处理', 102, 1001, 0)
ON DUPLICATE KEY UPDATE
  `name` = VALUES(`name`),
  `parent_id` = VALUES(`parent_id`),
  `user_id` = VALUES(`user_id`),
  `is_deleted` = VALUES(`is_deleted`);

-- ------------------------------------------------------------
-- 文件夹-文献关联
-- ------------------------------------------------------------
INSERT IGNORE INTO `folder_paper` (`folder_id`, `paper_id`) VALUES
  (100, 1), (100, 2),
  (101, 1),             -- 精读：CV 综述
  (102, 2),             -- 略读：NLP 综述
  (103, 1),             -- 计算机视觉目录
  (104, 2);             -- 自然语言处理目录

-- ------------------------------------------------------------
-- 旧版 repository 表：默认不插入数据（已被 paper_code 替代）
-- ------------------------------------------------------------

-- 完成后打印核对信息
SELECT 'seed complete' AS status;
