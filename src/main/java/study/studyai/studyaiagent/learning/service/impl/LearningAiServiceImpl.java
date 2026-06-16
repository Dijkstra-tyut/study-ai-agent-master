package study.studyai.studyaiagent.learning.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.LearningQuestion;
import study.studyai.studyaiagent.course.service.CourseKnowledgeService;
import study.studyai.studyaiagent.learning.model.LearningAnswerCheckResult;
import study.studyai.studyaiagent.learning.model.LearningProfileResult;
import study.studyai.studyaiagent.learning.model.LearningQuestionGenerateResult;
import study.studyai.studyaiagent.learning.model.LearningQuestionResult;
import study.studyai.studyaiagent.learning.service.LearningAiService;
import study.studyai.studyaiagent.memory.StudyConversationId;

import java.util.List;

@Service
public class LearningAiServiceImpl implements LearningAiService {

    private static final String ASK_SYSTEM_PROMPT = "你是学习智能体的课程知识点答疑助手。必须优先依据给定课程资料回答，资料没有提到时要明确说明不能从资料中确认，再给出谨慎建议。";

    private static final String QUESTION_SYSTEM_PROMPT = "你是学习智能体的出题助手。请严格根据课程资料出题，题目要能考察学生对资料核心知识点的理解。";

    private static final String CHECK_SYSTEM_PROMPT = "你是学习智能体的判题助手。请根据标准答案和解析判断学生答案是否正确，并给出简短反馈。";

    private static final String PROFILE_SYSTEM_PROMPT = "你是学习智能体的学习画像分析助手。请根据用户历史问答和错题记录，生成动态画像、学习路线和学习行为分析。";

    private final ChatClient chatClient;

    private final CourseKnowledgeService courseKnowledgeService;

    public LearningAiServiceImpl(ChatModel dashscopeChatModel,
                                 @Qualifier("studyDatabaseChatMemory") ChatMemory chatMemory,
                                 CourseKnowledgeService courseKnowledgeService) {
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        this.courseKnowledgeService = courseKnowledgeService;
    }

    @Override
    public String askCourseQuestion(Long userId, String conversationId, Course course, String question) {
        String context = courseKnowledgeService.searchCourseContext(course, question);
        String chatId = StudyConversationId.build(userId, conversationId);
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n课程资料片段：\n" + context
                + "\n学生问题：\n" + question;
        return chatClient.prompt()
                .system(ASK_SYSTEM_PROMPT)
                .user(userPrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .content();
    }

    @Override
    public List<LearningQuestionResult> generateQuestionList(Long userId, String conversationId, Course course, Integer questionCount) {
        String context = courseKnowledgeService.searchCourseContext(course, "课程重点 出题 考察");
        String chatId = StudyConversationId.build(userId, conversationId);
        String userPrompt = "课程名称：" + course.getCourse_name()
                + "\n出题数量：" + questionCount
                + "\n课程资料片段：\n" + context
                + "\n请返回结构化对象：questionList 为题目列表，每个题目包含 question、answer、analysis、questionType、difficulty。";
        LearningQuestionGenerateResult result = chatClient.prompt()
                .system(QUESTION_SYSTEM_PROMPT)
                .user(userPrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LearningQuestionGenerateResult.class);
        if (result == null || CollUtil.isEmpty(result.getQuestionList())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "生成题目失败");
        }
        return result.getQuestionList();
    }

    @Override
    public LearningAnswerCheckResult checkAnswer(Long userId, String conversationId, LearningQuestion learningQuestion, String userAnswer) {
        String chatId = StudyConversationId.build(userId, conversationId);
        String userPrompt = "题目：\n" + learningQuestion.getQuestion()
                + "\n标准答案：\n" + learningQuestion.getAnswer()
                + "\n解析：\n" + StrUtil.blankToDefault(learningQuestion.getAnalysis(), "无")
                + "\n学生答案：\n" + userAnswer
                + "\n请返回结构化对象：correct 表示是否正确，feedback 表示对学生的反馈。";
        LearningAnswerCheckResult result = chatClient.prompt()
                .system(CHECK_SYSTEM_PROMPT)
                .user(userPrompt)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(LearningAnswerCheckResult.class);
        if (result == null || result.getCorrect() == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题失败");
        }
        return result;
    }

    @Override
    public LearningProfileResult analyzeLearningProfile(String learningHistory) {
        if (StrUtil.isBlank(learningHistory)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "暂无可分析的学习记录");
        }
        String userPrompt = "用户学习记录：\n" + learningHistory
                + "\n请返回结构化对象，字段包含 knowledgeLevel、learningStyle、interest、weakness、errorPreference、learningSpeed、learningRoute、behaviorAnalysis。"
                + "\nknowledgeLevel 为 0 到 100 的整数，weakness 为字符串列表。";
        LearningProfileResult result = chatClient.prompt()
                .system(PROFILE_SYSTEM_PROMPT)
                .user(userPrompt)
                .call()
                .entity(LearningProfileResult.class);
        if (result == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "学习画像分析失败");
        }
        return result;
    }
}
