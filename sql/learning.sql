CREATE TABLE IF NOT EXISTS `learning_question` (
                                      `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '题目ID',
                                      `user_id` BIGINT NOT NULL COMMENT '学生ID',
                                      `course_id` BIGINT NOT NULL COMMENT '课程ID',
                                      `conversation_id` VARCHAR(100) NOT NULL COMMENT '对话ID',
                                      `question` TEXT NOT NULL COMMENT '题目内容',
                                      `answer` TEXT NOT NULL COMMENT '标准答案',
                                      `analysis` TEXT DEFAULT NULL COMMENT '答案解析',
                                      `question_type` VARCHAR(50) DEFAULT NULL COMMENT '题目类型',
                                      `difficulty` VARCHAR(50) DEFAULT NULL COMMENT '难度',
                                      `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_user_id` (`user_id`),
                                      KEY `idx_course_id` (`course_id`),
                                      KEY `idx_user_course` (`user_id`, `course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习出题记录表';

CREATE TABLE IF NOT EXISTS `learning_answer_record` (
                                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '答题记录ID',
                                           `question_id` BIGINT NOT NULL COMMENT '题目ID',
                                           `user_id` BIGINT NOT NULL COMMENT '学生ID',
                                           `course_id` BIGINT NOT NULL COMMENT '课程ID',
                                           `user_answer` TEXT NOT NULL COMMENT '学生答案',
                                           `correct` TINYINT NOT NULL DEFAULT 0 COMMENT '是否正确',
                                           `ai_feedback` TEXT DEFAULT NULL COMMENT 'AI反馈',
                                           `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                           `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           PRIMARY KEY (`id`),
                                           KEY `idx_question_id` (`question_id`),
                                           KEY `idx_user_id` (`user_id`),
                                           KEY `idx_user_course_correct` (`user_id`, `course_id`, `correct`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生答题记录表';

CREATE TABLE IF NOT EXISTS `learning_profile` (
                                    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '画像ID',
                                    `user_id` BIGINT NOT NULL COMMENT '学生ID',
                                    `knowledge_level` INT DEFAULT NULL COMMENT '知识水平',
                                    `learning_style` VARCHAR(100) DEFAULT NULL COMMENT '学习风格',
                                    `interest` VARCHAR(255) DEFAULT NULL COMMENT '兴趣方向',
                                    `weakness` TEXT DEFAULT NULL COMMENT '薄弱点JSON',
                                    `error_preference` VARCHAR(255) DEFAULT NULL COMMENT '错题偏好',
                                    `learning_speed` VARCHAR(100) DEFAULT NULL COMMENT '学习速度',
                                    `learning_route` MEDIUMTEXT DEFAULT NULL COMMENT '学习路线',
                                    `behavior_analysis` MEDIUMTEXT DEFAULT NULL COMMENT '学习行为分析',
                                    `profile_json` MEDIUMTEXT DEFAULT NULL COMMENT '完整画像JSON',
                                    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学习动态画像表';
