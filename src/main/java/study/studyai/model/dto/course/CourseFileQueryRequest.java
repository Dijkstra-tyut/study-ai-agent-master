package study.studyai.model.dto.course;

import lombok.Data;
import study.studyai.common.PageRequest;

import java.io.Serializable;

@Data
public class CourseFileQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long courseId;

    private Long teacherId;

    private String fileName;
}
