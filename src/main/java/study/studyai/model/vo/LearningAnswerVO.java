package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LearningAnswerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long questionId;

    private Boolean correct;

    private String correctAnswer;

    private String analysis;

    private String aiFeedback;
}
