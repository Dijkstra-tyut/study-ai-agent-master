package study.studyai.studyaiagent.learning.service;

import study.studyai.model.entity.Course;
import study.studyai.model.entity.LearningQuestion;
import study.studyai.studyaiagent.learning.model.LearningAnswerCheckResult;
import study.studyai.studyaiagent.learning.model.LearningProfileResult;
import study.studyai.studyaiagent.learning.model.LearningQuestionResult;

import java.util.List;

public interface LearningAiService {

    String askCourseQuestion(Long userId, String conversationId, Course course, String question);

    List<LearningQuestionResult> generateQuestionList(Long userId, String conversationId, Course course, Integer questionCount);

    LearningAnswerCheckResult checkAnswer(Long userId, String conversationId, LearningQuestion learningQuestion, String userAnswer);

    LearningProfileResult analyzeLearningProfile(String learningHistory);
}
