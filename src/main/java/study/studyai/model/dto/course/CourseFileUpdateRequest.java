package study.studyai.model.dto.course;

import lombok.Data;

import java.io.Serializable;

@Data
public class CourseFileUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String file_name;

    private String review_status;
}
