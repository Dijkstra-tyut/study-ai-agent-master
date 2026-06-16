package study.studyai.model.dto.learning;

import lombok.Data;

import java.io.Serializable;

@Data
public class LearningQuestionAnswerRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long questionId;

    private String userAnswer;

    private String conversationId;
}
