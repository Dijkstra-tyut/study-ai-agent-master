package study.studyai.model.dto.course;

import lombok.Data;

import java.io.Serializable;

@Data
public class CourseAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String course_name;

    private Long teacher_id;

    private String description;
}
