CREATE DATABASE IF NOT EXISTS `study` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `study`;

CREATE TABLE IF NOT EXISTS `user` (
                        `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` VARCHAR(50) NOT NULL COMMENT '账号',
                        `password` VARCHAR(255) NOT NULL COMMENT '密码',
                        `role` VARCHAR(20) NOT NULL DEFAULT 'student' COMMENT '角色: student/teacher/admin',
                        `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
                        `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表 (统一管理所有角色)';

CREATE TABLE IF NOT EXISTS `student_profile` (
                                   `student_id` BIGINT NOT NULL COMMENT '学生ID (关联 user 表的 id 作为主键)',
                                   `major` VARCHAR(100) DEFAULT NULL COMMENT '专业',
                                   `grade` VARCHAR(50) DEFAULT NULL COMMENT '年级',
                                   `learning_target` VARCHAR(500) DEFAULT NULL COMMENT '学习目标',
                                   `interest_direction` VARCHAR(255) DEFAULT NULL COMMENT '兴趣方向',
                                   `knowledge_level` TEXT DEFAULT NULL COMMENT '知识基础画像 (建议存储 JSON 格式字符串以便 AI 解析)',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生信息表 (用户画像)';

CREATE TABLE IF NOT EXISTS `teacher_profile` (
                                   `teacher_id` BIGINT NOT NULL COMMENT '教师ID (关联 user 表的 id 作为主键)',
                                   `teacher_name` VARCHAR(50) DEFAULT NULL COMMENT '姓名',
                                   `research_area` VARCHAR(255) DEFAULT NULL COMMENT '研究方向',
                                   `intro` TEXT DEFAULT NULL COMMENT '简介',
                                   `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                   `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                   PRIMARY KEY (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='教师信息表';
