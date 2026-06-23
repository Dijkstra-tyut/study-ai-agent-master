package study.studyai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 课程表
 * @TableName course
 */
@TableName(value ="course")
public class Course {

    /**
     * 课程ID
     */
    @TableId(value = "course_id", type = IdType.AUTO)
    private Long course_id;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String course_name;

    /**
     * 负责人
     */
    @TableField("teacher_id")
    private Long teacher_id;

    /**
     * 课程介绍
     */
    @TableField("description")
    private String description;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date create_time;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date update_time;

    public Long getCourse_id() {
        return course_id;
    }

    public void setCourse_id(Long course_id) {
        this.course_id = course_id;
    }

    public String getCourse_name() {
        return course_name;
    }

    public void setCourse_name(String course_name) {
        this.course_name = course_name;
    }

    public Long getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(Long teacher_id) {
        this.teacher_id = teacher_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
