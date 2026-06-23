package study.studyai.studyaiagent.learning.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Service;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.LearningQuestion;
import study.studyai.studyaiagent.core.AiChatService;
import study.studyai.studyaiagent.course.service.CourseKnowledgeService;
import study.studyai.studyaiagent.learning.model.LearningAnswerCheckResult;
import study.studyai.studyaiagent.learning.model.LearningProfileResult;
import study.studyai.studyaiagent.learning.model.LearningQuestionGenerateResult;
import study.studyai.studyaiagent.learning.model.LearningQuestionResult;
import study.studyai.studyaiagent.learning.service.LearningAiService;

import java.util.List;

@Service
public class LearningAiServiceImpl implements LearningAiService {

    private static final String ASK_SYSTEM_PROMPT = "You are a course knowledge assistant. Answer primarily from the provided course material. "
            + "If the material does not contain the answer, say that it cannot be confirmed from the material, then give a cautious suggestion.";

    private static final String QUESTION_SYSTEM_PROMPT = "You are a quiz generation assistant. Generate questions strictly from the provided course material. "
            + "The questions should test understanding of the core concepts in the material.";

    private static final String CHECK_SYSTEM_PROMPT = "You are an answer grading assistant. Judge whether the student answer is correct based on the standard answer and analysis, and give brief feedback.";

    private static final String PROFILE_SYSTEM_PROMPT = "You are a learning profile analysis assistant. Based on historical questions, answers, and wrong-answer records, generate a dynamic learning profile and study route.";

    private final AiChatService aiChatService;

    private final CourseKnowledgeService courseKnowledgeService;

    public LearningAiServiceImpl(AiChatService aiChatService, CourseKnowledgeService courseKnowledgeService) {
        this.aiChatService = aiChatService;
        this.courseKnowledgeService = courseKnowledgeService;
    }

    @Override
    public String askCourseQuestion(Long userId, String conversationId, Course course, String question) {
        String context = courseKnowledgeService.searchCourseContext(course, question);
        String userPrompt = "Course name: " + course.getCourse_name()
                + "\nConversation id: " + StrUtil.blankToDefault(conversationId, "none")
                + "\nCourse material excerpts:\n" + context
                + "\nStudent question:\n" + question;
        return aiChatService.call(ASK_SYSTEM_PROMPT, userPrompt);
    }

    @Override
    public List<LearningQuestionResult> generateQuestionList(Long userId, String conversationId, Course course, Integer questionCount) {
        String context = courseKnowledgeService.searchCourseContext(course, "course key points quiz assessment");
        String userPrompt = "Course name: " + course.getCourse_name()
                + "\nConversation id: " + StrUtil.blankToDefault(conversationId, "none")
                + "\nQuestion count: " + questionCount
                + "\nCourse material excerpts:\n" + context
                + "\nReturn a JSON object with field questionList. Each item must contain question, answer, analysis, questionType, difficulty.";
        LearningQuestionGenerateResult result = aiChatService.callEntity(QUESTION_SYSTEM_PROMPT, userPrompt, LearningQuestionGenerateResult.class);
        if (result == null || CollUtil.isEmpty(result.getQuestionList())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Question generation failed");
        }
        return result.getQuestionList();
    }

    @Override
    public LearningAnswerCheckResult checkAnswer(Long userId, String conversationId, LearningQuestion learningQuestion, String userAnswer) {
        String userPrompt = "Conversation id: " + StrUtil.blankToDefault(conversationId, "none")
                + "\nQuestion:\n" + learningQuestion.getQuestion()
                + "\nStandard answer:\n" + learningQuestion.getAnswer()
                + "\nAnalysis:\n" + StrUtil.blankToDefault(learningQuestion.getAnalysis(), "none")
                + "\nStudent answer:\n" + userAnswer
                + "\nReturn a JSON object with fields correct(boolean), feedback(string).";
        LearningAnswerCheckResult result = aiChatService.callEntity(CHECK_SYSTEM_PROMPT, userPrompt, LearningAnswerCheckResult.class);
        if (result == null || result.getCorrect() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Answer grading failed");
        }
        return result;
    }

    @Override
    public LearningProfileResult analyzeLearningProfile(String learningHistory) {
        if (StrUtil.isBlank(learningHistory)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "No learning records available for analysis");
        }
        String userPrompt = "User learning records:\n" + learningHistory
                + "\nReturn a JSON object with fields knowledgeLevel(integer 0-100), learningStyle, interest, weakness(array of strings), "
                + "errorPreference, learningSpeed, learningRoute, behaviorAnalysis.";
        LearningProfileResult result = aiChatService.callEntity(PROFILE_SYSTEM_PROMPT, userPrompt, LearningProfileResult.class);
        if (result == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "Learning profile analysis failed");
        }
        return result;
    }
}
