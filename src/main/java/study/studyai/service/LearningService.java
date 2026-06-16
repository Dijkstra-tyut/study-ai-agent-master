package study.studyai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

import java.util.List;

public interface LearningService {

    LearningAskVO askCourseKnowledge(LearningAskRequest learningAskRequest, User loginUser);

    List<LearningQuestionVO> generateQuestionList(LearningQuestionGenerateRequest learningQuestionGenerateRequest, User loginUser);

    LearningAnswerVO answerQuestion(LearningQuestionAnswerRequest learningQuestionAnswerRequest, User loginUser);

    Page<LearningWrongQuestionVO> listWrongQuestionByPage(LearningWrongQuestionQueryRequest learningWrongQuestionQueryRequest, User loginUser);

    LearningProfileVO analyzeLearningProfile(LearningProfileAnalyzeRequest learningProfileAnalyzeRequest, User loginUser);

    LearningProfileVO getLearningProfile(Long userId, User loginUser);
}
