package study.studyai.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.model.dto.course.CourseAddRequest;
import study.studyai.model.dto.course.CourseFileQueryRequest;
import study.studyai.model.dto.course.CourseFileUpdateRequest;
import study.studyai.model.dto.course.CourseQueryRequest;
import study.studyai.model.dto.course.CourseUpdateRequest;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.CourseFile;
import study.studyai.model.entity.User;

public interface CourseService {

    Long addCourse(CourseAddRequest courseAddRequest, User loginUser);

    boolean deleteCourse(Long courseId, User loginUser);

    boolean updateCourse(CourseUpdateRequest courseUpdateRequest, User loginUser);

    Page<Course> listCourseByPage(CourseQueryRequest courseQueryRequest, User loginUser);

    CourseFile uploadCourseFile(Long courseId, MultipartFile multipartFile, User loginUser);

    boolean deleteCourseFile(Long id, User loginUser);

    boolean updateCourseFile(CourseFileUpdateRequest courseFileUpdateRequest, User loginUser);

    Page<CourseFile> listCourseFileByPage(CourseFileQueryRequest courseFileQueryRequest, User loginUser);

    CourseFile getCourseFile(Long id, User loginUser);
}
