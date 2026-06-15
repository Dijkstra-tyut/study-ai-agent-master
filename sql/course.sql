CREATE TABLE IF NOT EXISTS `course` (
                          `course_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
                          `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
                          `teacher_id` BIGINT NOT NULL COMMENT '负责人 (关联 teacher_profile/user 表的主键)',
                          `description` TEXT DEFAULT NULL COMMENT '课程介绍',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`course_id`),
                          KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';

CREATE TABLE IF NOT EXISTS `course_file` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程文件ID',
                               `course_id` BIGINT NOT NULL COMMENT '课程ID',
                               `teacher_id` BIGINT NOT NULL COMMENT '上传教师ID',
                               `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
                               `file_key` VARCHAR(500) NOT NULL COMMENT 'COS对象key',
                               `file_url` VARCHAR(1000) NOT NULL COMMENT '文件访问地址',
                               `markdown_key` VARCHAR(500) DEFAULT NULL COMMENT 'Markdown COS对象key',
                               `markdown_url` VARCHAR(1000) DEFAULT NULL COMMENT 'Markdown访问地址',
                               `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
                               `file_size` BIGINT DEFAULT NULL COMMENT '文件大小',
                               `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               PRIMARY KEY (`id`),
                               KEY `idx_course_id` (`course_id`),
                               KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程文件表';

CREATE TABLE IF NOT EXISTS `chapter` (
                           `chapter_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '章节ID',
                           `course_id` bigint(20) NOT NULL COMMENT '课程ID',
                           `chapter_name` varchar(255) NOT NULL COMMENT '章节名称',
                           PRIMARY KEY (`chapter_id`),
                           KEY `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='章节表';

CREATE TABLE IF NOT EXISTS `chat_memory_message` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                       `user_id` BIGINT NOT NULL COMMENT '用户ID',
                                       `conversation_id` VARCHAR(100) NOT NULL COMMENT '对话ID',
                                       `role` VARCHAR(50) NOT NULL COMMENT '消息角色',
                                       `content` MEDIUMTEXT NOT NULL COMMENT '消息内容',
                                       `message_order` INT DEFAULT 0 COMMENT '消息顺序',
                                       `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_user_conversation` (`user_id`, `conversation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记忆消息表';
