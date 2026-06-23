package study.studyai.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

/**
 * 学习动态画像表
 * @TableName learning_profile
 */
@TableName(value = "learning_profile")
public class LearningProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long user_id;

    @TableField("knowledge_level")
    private Integer knowledge_level;

    @TableField("learning_style")
    private String learning_style;

    private String interest;

    private String weakness;

    @TableField("error_preference")
    private String error_preference;

    @TableField("learning_speed")
    private String learning_speed;

    @TableField("learning_route")
    private String learning_route;

    @TableField("behavior_analysis")
    private String behavior_analysis;

    @TableField("profile_json")
    private String profile_json;

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

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public Integer getKnowledge_level() {
        return knowledge_level;
    }

    public void setKnowledge_level(Integer knowledge_level) {
        this.knowledge_level = knowledge_level;
    }

    public String getLearning_style() {
        return learning_style;
    }

    public void setLearning_style(String learning_style) {
        this.learning_style = learning_style;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public String getWeakness() {
        return weakness;
    }

    public void setWeakness(String weakness) {
        this.weakness = weakness;
    }

    public String getError_preference() {
        return error_preference;
    }

    public void setError_preference(String error_preference) {
        this.error_preference = error_preference;
    }

    public String getLearning_speed() {
        return learning_speed;
    }

    public void setLearning_speed(String learning_speed) {
        this.learning_speed = learning_speed;
    }

    public String getLearning_route() {
        return learning_route;
    }

    public void setLearning_route(String learning_route) {
        this.learning_route = learning_route;
    }

    public String getBehavior_analysis() {
        return behavior_analysis;
    }

    public void setBehavior_analysis(String behavior_analysis) {
        this.behavior_analysis = behavior_analysis;
    }

    public String getProfile_json() {
        return profile_json;
    }

    public void setProfile_json(String profile_json) {
        this.profile_json = profile_json;
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
