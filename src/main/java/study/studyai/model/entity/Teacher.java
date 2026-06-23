package study.studyai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 教师信息表
 * @TableName teacher_profile
 */
@Data
@TableName(value ="teacher_profile")
public class Teacher {

    /**
     * 教师ID
     */
    @TableId(value = "teacher_id", type = IdType.INPUT)
    private Long teacher_id;

    /**
     * 姓名
     */
    @TableField("teacher_name")
    private String teacher_name;

    /**
     * 研究方向
     */
    @TableField("research_area")
    private String research_area;

    /**
     * 简介
     */
    private String intro;

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

    public Long getTeacher_id() {
        return teacher_id;
    }

    public void setTeacher_id(Long teacher_id) {
        this.teacher_id = teacher_id;
    }

    public String getTeacher_name() {
        return teacher_name;
    }

    public void setTeacher_name(String teacher_name) {
        this.teacher_name = teacher_name;
    }

    public String getResearch_area() {
        return research_area;
    }

    public void setResearch_area(String research_area) {
        this.research_area = research_area;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
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
