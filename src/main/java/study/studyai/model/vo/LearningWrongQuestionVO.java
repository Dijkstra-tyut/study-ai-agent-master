package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LearningWrongQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long recordId;

    private Long questionId;

    private Long courseId;

    private String question;

    private String userAnswer;

    private String correctAnswer;

    private String analysis;

    private String aiFeedback;

    private String questionType;

    private String difficulty;

    private Date createTime;
}
