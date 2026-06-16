package study.studyai.studyaiagent.learning.model;

import java.util.List;

public class LearningQuestionGenerateResult {

    private List<LearningQuestionResult> questionList;

    public List<LearningQuestionResult> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<LearningQuestionResult> questionList) {
        this.questionList = questionList;
    }
}
