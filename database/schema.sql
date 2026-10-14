-- ============================================================
-- EAVI 团队数据管理系统 —— 数据库结构（规范化 v2）
-- 说明：
--   1. 与 EAVI_system 后端实体类 / Mapper XML 对齐；
--   2. 补齐主外键、唯一约束、索引、逻辑删除与乐观锁字段；
--   3. 字符集统一 utf8mb4，支持中文与 Emoji；
-- 适用 MySQL：>= 5.7（建议 8.0）
-- 整理日期：2026-09-06
-- ============================================================

CREATE DATABASE IF NOT EXISTS `eavi_system`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_general_ci;

USE `eavi_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 先删子表，再删父表
DROP TABLE IF EXISTS `paper_code`;
DROP TABLE IF EXISTS `paper_dataset`;
DROP TABLE IF EXISTS `paper_task`;
DROP TABLE IF EXISTS `dataset_task`;
DROP TABLE IF EXISTS `folder_paper`;
DROP TABLE IF EXISTS `result`;
DROP TABLE IF EXISTS `repository`;
DROP TABLE IF EXISTS `folder`;
DROP TABLE IF EXISTS `paper`;
DROP TABLE IF EXISTS `dataset`;
DROP TABLE IF EXISTS `task`;
DROP TABLE IF EXISTS `status`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `role`;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- 1. role 角色表（字典表）
-- ------------------------------------------------------------
CREATE TABLE `role` (
  `role_id`  INT          NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role`     VARCHAR(32)  NOT NULL                COMMENT '角色名称，如：管理员',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_name` (`role`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '用户角色字典表';

-- ------------------------------------------------------------
-- 2. status 数据集状态字典表
-- ------------------------------------------------------------
CREATE TABLE `status` (
  `status_id` INT         NOT NULL AUTO_INCREMENT COMMENT '状态ID',
  `status`    VARCHAR(32) NOT NULL                COMMENT '状态编码，如 active/processing/error',
  PRIMARY KEY (`status_id`),
  UNIQUE KEY `uk_status_code` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据集状态字典表';

-- ------------------------------------------------------------
-- 3. user 用户表
-- ------------------------------------------------------------
CREATE TABLE `user` (
  `user_id`    INT          NOT NULL AUTO_INCREMENT COMMENT '用户ID（JWT 中携带的主键）',
  `username`   VARCHAR(64)  NOT NULL                COMMENT '登录用户名',
  `email`      VARCHAR(128) DEFAULT NULL            COMMENT '邮箱',
  `name`       VARCHAR(64)  DEFAULT NULL            COMMENT '真实姓名/显示名',
  `role_id`    INT          NOT NULL                COMMENT '角色ID，关联 role.role_id',
  `avatar`     VARCHAR(512) DEFAULT NULL            COMMENT '头像地址',
  `password`   VARCHAR(64)  NOT NULL DEFAULT 'e10adc3949ba59abbe56e057f20f883e' COMMENT 'MD5 加密后的密码（默认即 123456 的 MD5）',
  `version`    INT          NOT NULL DEFAULT 1      COMMENT '乐观锁版本号',
  `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-正常 1-删除',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_user_username` (`username`),
  KEY `idx_user_role` (`role_id`),
  CONSTRAINT `fk_user_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统用户表';

-- ------------------------------------------------------------
-- 4. task 任务类别表（图像分类/目标检测/机器翻译等）
-- ------------------------------------------------------------
CREATE TABLE `task` (
  `id`   BIGINT      NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `name` VARCHAR(64) NOT NULL                COMMENT '任务名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_name` (`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'AI 任务类别字典表';

-- ------------------------------------------------------------
-- 5. dataset 数据集表
-- 注意：
--   base_address 只保存“eavi-static/data/datasets/ 下的相对目录名”，
--   用于详情页扫描 Markdown 与图片；外链下载地址请放 true_address。
-- ------------------------------------------------------------
CREATE TABLE `dataset` (
  `dataset_id`    INT          NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  `dataset_name`  VARCHAR(255) NOT NULL                COMMENT '数据集名称',
  `description`   TEXT         DEFAULT NULL            COMMENT '数据集简介',
  `status_id`     INT          NOT NULL DEFAULT 1      COMMENT '状态ID，关联 status.status_id',
  `size_formatted` VARCHAR(64) DEFAULT NULL            COMMENT '格式化大小，如 460 MB',
  `file_count`    INT          DEFAULT 0               COMMENT '文件数量',
  `user_id`       INT          NOT NULL                COMMENT '上传者ID，关联 user.user_id',
  `downloadCount` INT          NOT NULL DEFAULT 0      COMMENT '下载次数',
  `created_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time`  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `true_address`  VARCHAR(1024) DEFAULT NULL           COMMENT '真实下载地址/服务器位置说明',
  `base_address`  VARCHAR(255) DEFAULT NULL            COMMENT '存储根目录下的相对目录名，NULL 表示本地文档尚未上架',
  `version`       INT          NOT NULL DEFAULT 1      COMMENT '乐观锁版本号',
  `is_deleted`    TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-正常 1-删除',
  PRIMARY KEY (`dataset_id`),
  UNIQUE KEY `uk_dataset_base_address` (`base_address`),
  KEY `idx_dataset_user` (`user_id`),
  KEY `idx_dataset_status` (`status_id`),
  KEY `idx_dataset_created` (`is_deleted`, `created_time`),
  CONSTRAINT `fk_dataset_status` FOREIGN KEY (`status_id`) REFERENCES `status` (`status_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_dataset_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据集表';

-- ------------------------------------------------------------
-- 6. dataset_task 数据集-任务多对多关联表
-- ------------------------------------------------------------
CREATE TABLE `dataset_task` (
  `dataset_id` INT    NOT NULL COMMENT '数据集ID',
  `task_id`    BIGINT NOT NULL COMMENT '任务ID',
  PRIMARY KEY (`dataset_id`, `task_id`),
  KEY `idx_dataset_task_task` (`task_id`),
  CONSTRAINT `fk_dataset_task_dataset` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`dataset_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_dataset_task_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '数据集-任务关联表';

-- ------------------------------------------------------------
-- 7. paper 文献表（论文）
-- ------------------------------------------------------------
CREATE TABLE `paper` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '文献ID',
  `title`       VARCHAR(512) NOT NULL                COMMENT '标题',
  `authors`     VARCHAR(512) NOT NULL                COMMENT '作者，逗号分隔',
  `venue`       VARCHAR(255) DEFAULT NULL            COMMENT '期刊/会议',
  `abstract`    TEXT         DEFAULT NULL            COMMENT '摘要',
  `upload_time` DATETIME     DEFAULT NULL            COMMENT '录入/发表时间',
  `citations`   INT          NOT NULL DEFAULT 0      COMMENT '引用数',
  `impact`      VARCHAR(64)  DEFAULT NULL            COMMENT '影响因子/级别，如 IF:17.73',
  `status`      VARCHAR(32)  NOT NULL DEFAULT '未阅读' COMMENT '阅读状态：未阅读/正在阅读/已阅读',
  `paper_url`   VARCHAR(512) DEFAULT NULL            COMMENT '论文链接',
  `note_url`    VARCHAR(512) DEFAULT NULL            COMMENT '阅读笔记文件名（eavi-static/data/paper_note/ 下）',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_paper_upload_time` (`upload_time`),
  KEY `idx_paper_citations` (`citations`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文献表';

-- ------------------------------------------------------------
-- 8. paper_task 文献-任务多对多关联表
-- ------------------------------------------------------------
CREATE TABLE `paper_task` (
  `paper_id` BIGINT NOT NULL COMMENT '文献ID',
  `task_id`  BIGINT NOT NULL COMMENT '任务ID',
  PRIMARY KEY (`paper_id`, `task_id`),
  KEY `idx_paper_task_task` (`task_id`),
  CONSTRAINT `fk_paper_task_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_paper_task_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文献-任务关联表';

-- ------------------------------------------------------------
-- 9. paper_code 文献代码仓库表
-- 说明：一篇文献可有多个代码仓库；(paper_id, code_name) 唯一。
-- ------------------------------------------------------------
CREATE TABLE `paper_code` (
  `code_id`    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '代码记录ID',
  `paper_id`   BIGINT       NOT NULL                COMMENT '文献ID',
  `code_name`  VARCHAR(255) NOT NULL                COMMENT '代码/仓库名称',
  `code_url`   VARCHAR(1024) NOT NULL               COMMENT '代码链接或上传后的文件名',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`code_id`),
  UNIQUE KEY `uk_paper_code_name` (`paper_id`, `code_name`),
  CONSTRAINT `fk_paper_code_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文献关联的代码仓库表';

-- ------------------------------------------------------------
-- 10. paper_dataset 文献-数据集关联表
-- 说明：
--   一条记录要么关联库内数据集(dataset_id)，要么填写外部链接(dataset_url)；
--   ref_code 为生成列，用于数据库层防止重复关联。
-- ------------------------------------------------------------
CREATE TABLE `paper_dataset` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '关联记录ID',
  `paper_id`    BIGINT       NOT NULL                COMMENT '文献ID',
  `dataset_id`  INT          DEFAULT NULL            COMMENT '库内数据集ID，可为 NULL',
  `dataset_url` VARCHAR(1024) DEFAULT NULL           COMMENT '外部数据集链接，可为 NULL',
  `display_name` VARCHAR(512) DEFAULT NULL           COMMENT '外部数据集显示名称',
  `ref_code`    VARCHAR(1040) GENERATED ALWAYS AS (
                   CASE
                     WHEN `dataset_id` IS NOT NULL THEN CONCAT('D:', `dataset_id`)
                     ELSE CONCAT('U:', COALESCE(`dataset_url`, ''))
                   END
                 ) STORED COMMENT '关联指纹（生成列，用于唯一约束）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_paper_dataset_ref` (`paper_id`, `ref_code`),
  KEY `idx_paper_dataset_dataset` (`dataset_id`),
  CONSTRAINT `fk_paper_dataset_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_paper_dataset_dataset` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`dataset_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文献-数据集关联表';

-- ------------------------------------------------------------
-- 11. result 论文结果（benchmark）表
-- ------------------------------------------------------------
CREATE TABLE `result` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT COMMENT '结果ID',
  `paper_id`     BIGINT      NOT NULL                COMMENT '文献ID',
  `task_id`      BIGINT      NOT NULL                COMMENT '任务ID',
  `dataset_id`   INT         NOT NULL                COMMENT '数据集ID',
  `model_name`   VARCHAR(255) DEFAULT NULL           COMMENT '模型名称',
  `metric_name`  VARCHAR(128) NOT NULL               COMMENT '指标名，如 Top-1 / mAP',
  `metric_value` VARCHAR(64)  NOT NULL               COMMENT '指标值',
  `is_sota`      TINYINT(1)   NOT NULL DEFAULT 0     COMMENT '是否 SOTA：1-是 0-否',
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_result_paper` (`paper_id`),
  KEY `idx_result_task` (`task_id`),
  KEY `idx_result_dataset` (`dataset_id`),
  CONSTRAINT `fk_result_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_result_task` FOREIGN KEY (`task_id`) REFERENCES `task` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_result_dataset` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`dataset_id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '论文 Benchmark 结果表';

-- ------------------------------------------------------------
-- 12. folder 文献收藏夹表（树形结构）
-- ------------------------------------------------------------
CREATE TABLE `folder` (
  `folder_id`  INT          NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
  `name`       VARCHAR(255) NOT NULL                COMMENT '文件夹名称',
  `parent_id`  INT          DEFAULT NULL            COMMENT '父文件夹ID，NULL 表示根目录',
  `user_id`    INT          DEFAULT NULL            COMMENT '所属用户ID，NULL 表示公共目录',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '逻辑删除：0-正常 1-删除',
  PRIMARY KEY (`folder_id`),
  KEY `idx_folder_parent` (`parent_id`),
  KEY `idx_folder_user` (`user_id`),
  CONSTRAINT `fk_folder_parent` FOREIGN KEY (`parent_id`) REFERENCES `folder` (`folder_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_folder_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
    ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文献文件夹（收藏夹）表';

-- ------------------------------------------------------------
-- 13. folder_paper 文件夹-文献关联表
-- ------------------------------------------------------------
CREATE TABLE `folder_paper` (
  `folder_id` INT    NOT NULL COMMENT '文件夹ID',
  `paper_id`  BIGINT NOT NULL COMMENT '文献ID',
  PRIMARY KEY (`folder_id`, `paper_id`),
  KEY `idx_folder_paper_paper` (`paper_id`),
  CONSTRAINT `fk_folder_paper_folder` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`folder_id`)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_folder_paper_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '文件夹-文献关联表';

-- ------------------------------------------------------------
-- 14. repository 代码仓库表（旧版遗留）
-- 说明：当前后端“文献-代码”功能已改用 paper_code 表，本表暂未使用；
--       为兼容历史实体/Mapper 保留，后续可删除或迁移数据后废弃。
-- ------------------------------------------------------------
CREATE TABLE `repository` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '仓库记录ID',
  `paper_id`    BIGINT       NOT NULL                COMMENT '文献ID',
  `name`        VARCHAR(255) NOT NULL                COMMENT '仓库名称',
  `url`         VARCHAR(512) NOT NULL                COMMENT '仓库链接',
  `description` VARCHAR(512) DEFAULT NULL            COMMENT '描述',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_repository_paper` (`paper_id`),
  CONSTRAINT `fk_repository_paper` FOREIGN KEY (`paper_id`) REFERENCES `paper` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '旧版文献代码仓库表（当前未使用）';
