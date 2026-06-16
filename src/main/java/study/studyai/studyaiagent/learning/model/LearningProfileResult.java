package study.studyai.studyaiagent.learning.model;

import java.util.List;

public class LearningProfileResult {

    private Integer knowledgeLevel;

    private String learningStyle;

    private String interest;

    private List<String> weakness;

    private String errorPreference;

    private String learningSpeed;

    private String learningRoute;

    private String behaviorAnalysis;

    public Integer getKnowledgeLevel() {
        return knowledgeLevel;
    }

    public void setKnowledgeLevel(Integer knowledgeLevel) {
        this.knowledgeLevel = knowledgeLevel;
    }

    public String getLearningStyle() {
        return learningStyle;
    }

    public void setLearningStyle(String learningStyle) {
        this.learningStyle = learningStyle;
    }

    public String getInterest() {
        return interest;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public List<String> getWeakness() {
        return weakness;
    }

    public void setWeakness(List<String> weakness) {
        this.weakness = weakness;
    }

    public String getErrorPreference() {
        return errorPreference;
    }

    public void setErrorPreference(String errorPreference) {
        this.errorPreference = errorPreference;
    }

    public String getLearningSpeed() {
        return learningSpeed;
    }

    public void setLearningSpeed(String learningSpeed) {
        this.learningSpeed = learningSpeed;
    }

    public String getLearningRoute() {
        return learningRoute;
    }

    public void setLearningRoute(String learningRoute) {
        this.learningRoute = learningRoute;
    }

    public String getBehaviorAnalysis() {
        return behaviorAnalysis;
    }

    public void setBehaviorAnalysis(String behaviorAnalysis) {
        this.behaviorAnalysis = behaviorAnalysis;
    }
}
