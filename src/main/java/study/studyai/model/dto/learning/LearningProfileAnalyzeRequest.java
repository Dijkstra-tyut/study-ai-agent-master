package study.studyai.model.dto.learning;

import lombok.Data;

import java.io.Serializable;

@Data
public class LearningProfileAnalyzeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long userId;
}
