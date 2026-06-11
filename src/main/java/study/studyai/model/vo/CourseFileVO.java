package study.studyai.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class CourseFileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long course_id;

    private Long teacher_id;

    private String file_name;

    private String file_type;

    private Long file_size;

    private Date create_time;

    private Date update_time;
}
