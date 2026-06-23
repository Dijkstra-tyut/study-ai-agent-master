package study.studyai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 学生答题记录表
 * @TableName learning_answer_record
 */
@TableName(value = "learning_answer_record")
public class LearningAnswerRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("question_id")
    private Long question_id;

    @TableField("user_id")
    private Long user_id;

    @TableField("course_id")
    private Long course_id;

    @TableField("user_answer")
    private String user_answer;

    private Integer correct;

    @TableField("ai_feedback")
    private String ai_feedback;

    @TableField("create_time")
    private Date create_time;

    @TableField("update_time")
    private Date update_time;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getQuestion_id() {
        return question_id;
    }

    public void setQuestion_id(Long question_id) {
        this.question_id = question_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Long getCourse_id() {
        return course_id;
    }

    public void setCourse_id(Long course_id) {
        this.course_id = course_id;
    }

    public String getUser_answer() {
        return user_answer;
    }

    public void setUser_answer(String user_answer) {
        this.user_answer = user_answer;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public String getAi_feedback() {
        return ai_feedback;
    }

    public void setAi_feedback(String ai_feedback) {
        this.ai_feedback = ai_feedback;
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
