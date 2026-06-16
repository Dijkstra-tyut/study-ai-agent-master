package study.studyai.model.dto.learning;

import lombok.Data;
import lombok.EqualsAndHashCode;
import study.studyai.common.PageRequest;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
public class LearningWrongQuestionQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long courseId;
}
