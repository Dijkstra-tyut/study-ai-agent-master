package study.studyai.model.dto.learning;

import lombok.Data;

import java.io.Serializable;

@Data
public class LearningAskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long courseId;

    private String question;

    private String conversationId;
}
