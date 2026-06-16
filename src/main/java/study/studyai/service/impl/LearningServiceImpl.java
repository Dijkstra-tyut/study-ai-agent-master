package study.studyai.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.mapper.ChatMemoryMessageMapper;
import study.studyai.mapper.CourseMapper;
import study.studyai.mapper.LearningAnswerRecordMapper;
import study.studyai.mapper.LearningProfileMapper;
import study.studyai.mapper.LearningQuestionMapper;
import study.studyai.model.dto.learning.LearningAskRequest;
import study.studyai.model.dto.learning.LearningProfileAnalyzeRequest;
import study.studyai.model.dto.learning.LearningQuestionAnswerRequest;
import study.studyai.model.dto.learning.LearningQuestionGenerateRequest;
import study.studyai.model.dto.learning.LearningWrongQuestionQueryRequest;
import study.studyai.model.entity.ChatMemoryMessage;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.LearningAnswerRecord;
import study.studyai.model.entity.LearningProfile;
import study.studyai.model.entity.LearningQuestion;
import study.studyai.model.entity.User;
import study.studyai.model.enums.UserRoleEnum;
import study.studyai.model.vo.LearningAnswerVO;
import study.studyai.model.vo.LearningAskVO;
import study.studyai.model.vo.LearningProfileVO;
import study.studyai.model.vo.LearningQuestionVO;
import study.studyai.model.vo.LearningWrongQuestionVO;
import study.studyai.service.LearningService;
import study.studyai.studyaiagent.learning.model.LearningAnswerCheckResult;
import study.studyai.studyaiagent.learning.model.LearningProfileResult;
import study.studyai.studyaiagent.learning.model.LearningQuestionResult;
import study.studyai.studyaiagent.learning.service.LearningAiService;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LearningServiceImpl implements LearningService {

    private static final int DEFAULT_QUESTION_COUNT = 3;

    private static final int MAX_QUESTION_COUNT = 10;

    private static final int MAX_HISTORY_LENGTH = 20000;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private LearningQuestionMapper learningQuestionMapper;

    @Resource
    private LearningAnswerRecordMapper learningAnswerRecordMapper;

    @Resource
    private LearningProfileMapper learningProfileMapper;

    @Resource
    private ChatMemoryMessageMapper chatMemoryMessageMapper;

    @Resource
    private LearningAiService learningAiService;

    @Override
    public LearningAskVO askCourseKnowledge(LearningAskRequest learningAskRequest, User loginUser) {
        if (learningAskRequest == null || StrUtil.isBlank(learningAskRequest.getQuestion())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course course = getCourse(learningAskRequest.getCourseId());
        String conversationId = buildConversationId("knowledge", course.getCourse_id(), learningAskRequest.getConversationId());
        String answer = learningAiService.askCourseQuestion(loginUser.getId(), conversationId, course, learningAskRequest.getQuestion());
        LearningAskVO learningAskVO = new LearningAskVO();
        learningAskVO.setConversationId(conversationId);
        learningAskVO.setAnswer(answer);
        return learningAskVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<LearningQuestionVO> generateQuestionList(LearningQuestionGenerateRequest learningQuestionGenerateRequest, User loginUser) {
        if (learningQuestionGenerateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course course = getCourse(learningQuestionGenerateRequest.getCourseId());
        int questionCount = getQuestionCount(learningQuestionGenerateRequest.getQuestionCount());
        String conversationId = buildConversationId("question", course.getCourse_id(), learningQuestionGenerateRequest.getConversationId());
        List<LearningQuestionResult> questionResultList = learningAiService.generateQuestionList(loginUser.getId(), conversationId, course, questionCount);
        List<LearningQuestionVO> resultList = new ArrayList<>();
        for (LearningQuestionResult questionResult : questionResultList) {
            if (questionResult == null || StrUtil.hasBlank(questionResult.getQuestion(), questionResult.getAnswer())) {
                continue;
            }
            LearningQuestion learningQuestion = new LearningQuestion();
            learningQuestion.setUser_id(loginUser.getId());
            learningQuestion.setCourse_id(course.getCourse_id());
            learningQuestion.setConversation_id(conversationId);
            learningQuestion.setQuestion(questionResult.getQuestion());
            learningQuestion.setAnswer(questionResult.getAnswer());
            learningQuestion.setAnalysis(questionResult.getAnalysis());
            learningQuestion.setQuestion_type(questionResult.getQuestionType());
            learningQuestion.setDifficulty(questionResult.getDifficulty());
            learningQuestionMapper.insert(learningQuestion);
            resultList.add(getLearningQuestionVO(learningQuestion));
        }
        if (CollUtil.isEmpty(resultList)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目保存失败");
        }
        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningAnswerVO answerQuestion(LearningQuestionAnswerRequest learningQuestionAnswerRequest, User loginUser) {
        if (learningQuestionAnswerRequest == null || learningQuestionAnswerRequest.getQuestionId() == null
                || StrUtil.isBlank(learningQuestionAnswerRequest.getUserAnswer())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LearningQuestion learningQuestion = learningQuestionMapper.selectById(learningQuestionAnswerRequest.getQuestionId());
        if (learningQuestion == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        checkOwner(learningQuestion.getUser_id(), loginUser);
        String conversationId = StrUtil.blankToDefault(learningQuestionAnswerRequest.getConversationId(), learningQuestion.getConversation_id());
        LearningAnswerCheckResult checkResult = learningAiService.checkAnswer(loginUser.getId(), conversationId, learningQuestion, learningQuestionAnswerRequest.getUserAnswer());
        LearningAnswerRecord record = new LearningAnswerRecord();
        record.setQuestion_id(learningQuestion.getId());
        record.setUser_id(loginUser.getId());
        record.setCourse_id(learningQuestion.getCourse_id());
        record.setUser_answer(learningQuestionAnswerRequest.getUserAnswer());
        record.setCorrect(Boolean.TRUE.equals(checkResult.getCorrect()) ? 1 : 0);
        record.setAi_feedback(checkResult.getFeedback());
        learningAnswerRecordMapper.insert(record);
        return getLearningAnswerVO(record, learningQuestion);
    }

    @Override
    public Page<LearningWrongQuestionVO> listWrongQuestionByPage(LearningWrongQuestionQueryRequest learningWrongQuestionQueryRequest, User loginUser) {
        if (learningWrongQuestionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<LearningAnswerRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", loginUser.getId());
        queryWrapper.eq("correct", 0);
        queryWrapper.eq(learningWrongQuestionQueryRequest.getCourseId() != null, "course_id", learningWrongQuestionQueryRequest.getCourseId());
        queryWrapper.orderByDesc("id");
        Page<LearningAnswerRecord> recordPage = learningAnswerRecordMapper.selectPage(
                new Page<>(learningWrongQuestionQueryRequest.getCurrent(), learningWrongQuestionQueryRequest.getPageSize()), queryWrapper);
        Page<LearningWrongQuestionVO> voPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        List<LearningWrongQuestionVO> voList = recordPage.getRecords().stream()
                .map(this::getLearningWrongQuestionVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LearningProfileVO analyzeLearningProfile(LearningProfileAnalyzeRequest learningProfileAnalyzeRequest, User loginUser) {
        Long targetUserId = getTargetUserId(learningProfileAnalyzeRequest, loginUser);
        checkOwner(targetUserId, loginUser);
        String learningHistory = buildLearningHistory(targetUserId);
        LearningProfileResult profileResult = learningAiService.analyzeLearningProfile(learningHistory);
        LearningProfile learningProfile = saveLearningProfile(targetUserId, profileResult);
        return getLearningProfileVO(learningProfile);
    }

    @Override
    public LearningProfileVO getLearningProfile(Long userId, User loginUser) {
        Long targetUserId = userId == null ? loginUser.getId() : userId;
        checkOwner(targetUserId, loginUser);
        QueryWrapper<LearningProfile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", targetUserId);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 1");
        LearningProfile learningProfile = learningProfileMapper.selectOne(queryWrapper);
        if (learningProfile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "暂无学习画像");
        }
        return getLearningProfileVO(learningProfile);
    }

    private Course getCourse(Long courseId) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return course;
    }

    private int getQuestionCount(Integer questionCount) {
        if (questionCount == null) {
            return DEFAULT_QUESTION_COUNT;
        }
        if (questionCount <= 0 || questionCount > MAX_QUESTION_COUNT) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "题目数量范围为 1 到 10");
        }
        return questionCount;
    }

    private String buildConversationId(String scene, Long courseId, String conversationId) {
        String prefix = scene + "_" + courseId + "_";
        if (StrUtil.isBlank(conversationId)) {
            return prefix + UUID.randomUUID().toString().replace("-", "");
        }
        String safeId = conversationId.replaceAll("[^a-zA-Z0-9_\\-]", "");
        if (StrUtil.isBlank(safeId)) {
            safeId = UUID.randomUUID().toString().replace("-", "");
        }
        if (safeId.startsWith(prefix)) {
            return StrUtil.sub(safeId, 0, Math.min(safeId.length(), 90));
        }
        safeId = StrUtil.sub(safeId, 0, Math.min(safeId.length(), 50));
        return prefix + safeId;
    }

    private void checkOwner(Long userId, User loginUser) {
        if (isAdmin(loginUser)) {
            return;
        }
        if (userId == null || !userId.equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    private Long getTargetUserId(LearningProfileAnalyzeRequest request, User loginUser) {
        if (request == null || request.getUserId() == null) {
            return loginUser.getId();
        }
        return request.getUserId();
    }

    private String buildLearningHistory(Long userId) {
        StringBuilder builder = new StringBuilder();
        List<ChatMemoryMessage> messageList = listRecentChatMessage(userId);
        if (CollUtil.isNotEmpty(messageList)) {
            builder.append("历史对话：\n");
            for (ChatMemoryMessage message : messageList) {
                builder.append(message.getConversation_id())
                        .append(" / ")
                        .append(message.getRole())
                        .append("：")
                        .append(message.getContent())
                        .append("\n");
                if (builder.length() > MAX_HISTORY_LENGTH) {
                    return StrUtil.sub(builder.toString(), 0, MAX_HISTORY_LENGTH);
                }
            }
        }
        List<LearningAnswerRecord> wrongRecordList = listRecentWrongRecord(userId);
        if (CollUtil.isNotEmpty(wrongRecordList)) {
            builder.append("\n错题记录：\n");
            for (LearningAnswerRecord record : wrongRecordList) {
                LearningQuestion question = learningQuestionMapper.selectById(record.getQuestion_id());
                if (question == null) {
                    continue;
                }
                builder.append("题目：").append(question.getQuestion())
                        .append("\n学生答案：").append(record.getUser_answer())
                        .append("\n标准答案：").append(question.getAnswer())
                        .append("\n反馈：").append(record.getAi_feedback())
                        .append("\n");
            }
        }
        return StrUtil.sub(builder.toString(), 0, Math.min(builder.length(), MAX_HISTORY_LENGTH));
    }

    private List<ChatMemoryMessage> listRecentChatMessage(Long userId) {
        QueryWrapper<ChatMemoryMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 200");
        List<ChatMemoryMessage> messageList = chatMemoryMessageMapper.selectList(queryWrapper);
        Collections.reverse(messageList);
        return messageList;
    }

    private List<LearningAnswerRecord> listRecentWrongRecord(Long userId) {
        QueryWrapper<LearningAnswerRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("correct", 0);
        queryWrapper.orderByDesc("id");
        queryWrapper.last("limit 100");
        return learningAnswerRecordMapper.selectList(queryWrapper);
    }

    private LearningProfile saveLearningProfile(Long userId, LearningProfileResult profileResult) {
        LearningProfile learningProfile = new LearningProfile();
        learningProfile.setUser_id(userId);
        learningProfile.setKnowledge_level(profileResult.getKnowledgeLevel());
        learningProfile.setLearning_style(profileResult.getLearningStyle());
        learningProfile.setInterest(profileResult.getInterest());
        learningProfile.setWeakness(JSONUtil.toJsonStr(CollUtil.emptyIfNull(profileResult.getWeakness())));
        learningProfile.setError_preference(profileResult.getErrorPreference());
        learningProfile.setLearning_speed(profileResult.getLearningSpeed());
        learningProfile.setLearning_route(profileResult.getLearningRoute());
        learningProfile.setBehavior_analysis(profileResult.getBehaviorAnalysis());
        learningProfile.setProfile_json(JSONUtil.toJsonStr(profileResult));
        learningProfileMapper.insert(learningProfile);
        return learningProfile;
    }

    private LearningQuestionVO getLearningQuestionVO(LearningQuestion learningQuestion) {
        LearningQuestionVO learningQuestionVO = new LearningQuestionVO();
        learningQuestionVO.setId(learningQuestion.getId());
        learningQuestionVO.setCourseId(learningQuestion.getCourse_id());
        learningQuestionVO.setConversationId(learningQuestion.getConversation_id());
        learningQuestionVO.setQuestion(learningQuestion.getQuestion());
        learningQuestionVO.setQuestionType(learningQuestion.getQuestion_type());
        learningQuestionVO.setDifficulty(learningQuestion.getDifficulty());
        learningQuestionVO.setCreateTime(learningQuestion.getCreate_time());
        return learningQuestionVO;
    }

    private LearningAnswerVO getLearningAnswerVO(LearningAnswerRecord record, LearningQuestion learningQuestion) {
        LearningAnswerVO learningAnswerVO = new LearningAnswerVO();
        learningAnswerVO.setRecordId(record.getId());
        learningAnswerVO.setQuestionId(learningQuestion.getId());
        learningAnswerVO.setCorrect(record.getCorrect() != null && record.getCorrect() == 1);
        learningAnswerVO.setCorrectAnswer(learningQuestion.getAnswer());
        learningAnswerVO.setAnalysis(learningQuestion.getAnalysis());
        learningAnswerVO.setAiFeedback(record.getAi_feedback());
        return learningAnswerVO;
    }

    private LearningWrongQuestionVO getLearningWrongQuestionVO(LearningAnswerRecord record) {
        LearningQuestion question = learningQuestionMapper.selectById(record.getQuestion_id());
        LearningWrongQuestionVO vo = new LearningWrongQuestionVO();
        vo.setRecordId(record.getId());
        vo.setQuestionId(record.getQuestion_id());
        vo.setCourseId(record.getCourse_id());
        vo.setUserAnswer(record.getUser_answer());
        vo.setAiFeedback(record.getAi_feedback());
        vo.setCreateTime(record.getCreate_time());
        if (question != null) {
            vo.setQuestion(question.getQuestion());
            vo.setCorrectAnswer(question.getAnswer());
            vo.setAnalysis(question.getAnalysis());
            vo.setQuestionType(question.getQuestion_type());
            vo.setDifficulty(question.getDifficulty());
        }
        return vo;
    }

    private LearningProfileVO getLearningProfileVO(LearningProfile learningProfile) {
        LearningProfileVO learningProfileVO = new LearningProfileVO();
        learningProfileVO.setId(learningProfile.getId());
        learningProfileVO.setUserId(learningProfile.getUser_id());
        learningProfileVO.setKnowledgeLevel(learningProfile.getKnowledge_level());
        learningProfileVO.setLearningStyle(learningProfile.getLearning_style());
        learningProfileVO.setInterest(learningProfile.getInterest());
        learningProfileVO.setWeakness(parseWeakness(learningProfile.getWeakness()));
        learningProfileVO.setErrorPreference(learningProfile.getError_preference());
        learningProfileVO.setLearningSpeed(learningProfile.getLearning_speed());
        learningProfileVO.setLearningRoute(learningProfile.getLearning_route());
        learningProfileVO.setBehaviorAnalysis(learningProfile.getBehavior_analysis());
        learningProfileVO.setCreateTime(learningProfile.getCreate_time());
        learningProfileVO.setUpdateTime(learningProfile.getUpdate_time());
        return learningProfileVO;
    }

    private List<String> parseWeakness(String weakness) {
        if (StrUtil.isBlank(weakness)) {
            return new ArrayList<>();
        }
        return JSONUtil.toList(JSONUtil.parseArray(weakness), String.class);
    }

    private boolean isAdmin(User user) {
        return UserRoleEnum.ADMIN.getValue().equals(user.getRole());
    }
}
