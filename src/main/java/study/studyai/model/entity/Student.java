package study.studyai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 学生信息表 (用户画像)
 * @TableName student_profile
 */
@Data
@TableName(value ="student_profile")
public class Student {

    /**
     * 学生ID
     */
    @TableId(value = "student_id", type = IdType.INPUT)
    private Long student_id;

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
     * 创建时间
     */
    private Date create_time;

    /**
     * 更新时间
     */
    private Date update_time;

    public Long getStudent_id() {
        return student_id;
    }

    public void setStudent_id(Long student_id) {
        this.student_id = student_id;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getLearning_target() {
        return learning_target;
    }

    public void setLearning_target(String learning_target) {
        this.learning_target = learning_target;
    }

    public String getInterest_direction() {
        return interest_direction;
    }

    public void setInterest_direction(String interest_direction) {
        this.interest_direction = interest_direction;
    }

    public String getKnowledge_level() {
        return knowledge_level;
    }

    public void setKnowledge_level(String knowledge_level) {
        this.knowledge_level = knowledge_level;
    }

    public Date getCreate_time() {
        return create_time;
    }

    public void setCreate_time(Date create_time) {
        this.create_time = create_time;
    }

    public Date getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(Date update_time) {
        this.update_time = update_time;
    }
}
