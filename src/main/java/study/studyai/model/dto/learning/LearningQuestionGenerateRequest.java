package study.studyai.model.dto.learning;

import lombok.Data;

import java.io.Serializable;

@Data
public class LearningQuestionGenerateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long courseId;

    private Integer questionCount;

    private String conversationId;
}
