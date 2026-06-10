package study.studyai.model.dto.course;

import lombok.Data;

import java.io.Serializable;

@Data
public class CourseUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long course_id;

    private String course_name;

    private Long teacher_id;

    private String description;
}
