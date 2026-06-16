package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class LearningAskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String conversationId;

    private String answer;
}
