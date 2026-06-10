package study.studyai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import study.studyai.annotation.AuthCheck;
import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.constant.UserConstant;
import study.studyai.exception.ThrowUtils;
import study.studyai.model.dto.course.CourseFileQueryRequest;
import study.studyai.model.dto.course.CourseQueryRequest;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.CourseFile;
import study.studyai.model.entity.User;
import study.studyai.service.CourseService;
import study.studyai.service.FileService;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Resource
    private UserService userService;

    @Resource
    private CourseService courseService;

    @Resource
    private FileService fileService;

    @PostMapping("/course/list/page")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public BaseResponse<Page<Course>> listStudentCourseByPage(@RequestBody CourseQueryRequest courseQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<Course> coursePage = courseService.listCourseByPage(courseQueryRequest, loginUser);
        return ResultUtils.success(coursePage);
    }

    @PostMapping("/course/file/list/page")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public BaseResponse<Page<CourseFile>> listStudentCourseFileByPage(@RequestBody CourseFileQueryRequest courseFileQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<CourseFile> courseFilePage = courseService.listCourseFileByPage(courseFileQueryRequest, loginUser);
        return ResultUtils.success(courseFilePage);
    }

    @GetMapping("/course/file/download")
    @AuthCheck(mustRole = UserConstant.STUDENT_ROLE)
    public void downloadStudentCourseFile(Long id, HttpServletRequest request, HttpServletResponse response) {
        User loginUser = userService.getLoginUser(request);
        CourseFile courseFile = courseService.getCourseFile(id, loginUser);
        fileService.downloadFileToResponse(courseFile.getFile_key(), response);
    }
}
