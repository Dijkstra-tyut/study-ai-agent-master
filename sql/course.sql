CREATE TABLE `course` (
                          `course_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '课程ID',
                          `course_name` VARCHAR(100) NOT NULL COMMENT '课程名称',
                          `teacher_id` BIGINT NOT NULL COMMENT '负责人 (关联 teacher_profile/user 表的主键)',
                          `description` TEXT DEFAULT NULL COMMENT '课程介绍',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          PRIMARY KEY (`course_id`),
                          KEY `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程表';