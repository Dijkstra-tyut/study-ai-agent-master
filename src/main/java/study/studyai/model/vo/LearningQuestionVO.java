package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class LearningQuestionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long courseId;

    private String conversationId;

    private String question;

    private String questionType;

    private String difficulty;

    private Date createTime;
}
