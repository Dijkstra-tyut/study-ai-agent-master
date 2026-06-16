package study.studyai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.studyai.annotation.AuthCheck;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.constant.UserConstant;
import study.studyai.exception.ThrowUtils;
import study.studyai.model.dto.learning.LearningAskRequest;
import study.studyai.model.dto.learning.LearningProfileAnalyzeRequest;
import study.studyai.model.dto.learning.LearningQuestionAnswerRequest;
import study.studyai.model.dto.learning.LearningQuestionGenerateRequest;
import study.studyai.model.dto.learning.LearningWrongQuestionQueryRequest;
import study.studyai.model.entity.User;
import study.studyai.model.vo.LearningAnswerVO;
import study.studyai.model.vo.LearningAskVO;
import study.studyai.model.vo.LearningProfileVO;
import study.studyai.model.vo.LearningQuestionVO;
import study.studyai.model.vo.LearningWrongQuestionVO;
import study.studyai.service.LearningService;
import study.studyai.service.UserService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/learning")
public class LearningController {

    @Resource
    private UserService userService;

    @Resource
    private LearningService learningService;

    @PostMapping("/knowledge/ask")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public BaseResponse<LearningAskVO> askCourseKnowledge(@RequestBody LearningAskRequest learningAskRequest,
                                                          HttpServletRequest request) {
        ThrowUtils.throwIf(learningAskRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        LearningAskVO learningAskVO = learningService.askCourseKnowledge(learningAskRequest, loginUser);
        return ResultUtils.success(learningAskVO);
    }

    @PostMapping("/question/generate")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public BaseResponse<List<LearningQuestionVO>> generateQuestionList(@RequestBody LearningQuestionGenerateRequest learningQuestionGenerateRequest,
                                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(learningQuestionGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        List<LearningQuestionVO> questionVOList = learningService.generateQuestionList(learningQuestionGenerateRequest, loginUser);
        return ResultUtils.success(questionVOList);
    }

    @PostMapping("/question/answer")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public BaseResponse<LearningAnswerVO> answerQuestion(@RequestBody LearningQuestionAnswerRequest learningQuestionAnswerRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(learningQuestionAnswerRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        LearningAnswerVO learningAnswerVO = learningService.answerQuestion(learningQuestionAnswerRequest, loginUser);
        return ResultUtils.success(learningAnswerVO);
    }

    @PostMapping("/question/wrong/list/page")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public BaseResponse<Page<LearningWrongQuestionVO>> listWrongQuestionByPage(@RequestBody LearningWrongQuestionQueryRequest learningWrongQuestionQueryRequest,
                                                                               HttpServletRequest request) {
        ThrowUtils.throwIf(learningWrongQuestionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<LearningWrongQuestionVO> wrongQuestionVOPage = learningService.listWrongQuestionByPage(learningWrongQuestionQueryRequest, loginUser);
        return ResultUtils.success(wrongQuestionVOPage);
    }

    @PostMapping("/profile/analyze")
    @AuthCheck
    public BaseResponse<LearningProfileVO> analyzeLearningProfile(@RequestBody LearningProfileAnalyzeRequest learningProfileAnalyzeRequest,
                                                                  HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        LearningProfileVO learningProfileVO = learningService.analyzeLearningProfile(learningProfileAnalyzeRequest, loginUser);
        return ResultUtils.success(learningProfileVO);
    }

    @GetMapping("/profile/get")
    @AuthCheck
    public BaseResponse<LearningProfileVO> getLearningProfile(Long userId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        LearningProfileVO learningProfileVO = learningService.getLearningProfile(userId, loginUser);
        return ResultUtils.success(learningProfileVO);
    }
}
