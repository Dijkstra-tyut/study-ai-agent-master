package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CourseVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long course_id;

    private String course_name;

    private Long teacher_id;

    private String description;

    private Date create_time;

    private Date update_time;
}
