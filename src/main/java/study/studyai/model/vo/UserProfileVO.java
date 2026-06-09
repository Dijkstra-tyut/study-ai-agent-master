package study.studyai.model.vo;

import lombok.Data;
import study.studyai.model.entity.Student;
import study.studyai.model.entity.Teacher;

import java.io.Serializable;

@Data
public class UserProfileVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户信息
     */
    private LoginUserVO user;

    /**
     * 学生信息
     */
    private Student student;

    /**
     * 教师信息
     */
    private Teacher teacher;
}
