package study.studyai.model.dto.user;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserProfileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID（管理员维护用户信息时使用）
     */
    private Long userId;

    /**
     * 角色：student/teacher
     */
    private String role;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 专业
     */
    private String major;

    /**
     * 年级
     */
    private String grade;

    /**
     * 学习目标
     */
    private String learning_target;

    /**
     * 兴趣方向
     */
    private String interest_direction;

    /**
     * 知识基础画像
     */
    private String knowledge_level;

    /**
     * 教师姓名
     */
    private String teacher_name;

    /**
     * 研究方向
     */
    private String research_area;

    /**
     * 简介
     */
    private String intro;
}
