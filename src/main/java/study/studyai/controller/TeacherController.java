package study.studyai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.annotation.AuthCheck;
import study.studyai.common.BaseResponse;
import study.studyai.common.DeleteRequest;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.constant.UserConstant;
import study.studyai.exception.ThrowUtils;
import study.studyai.model.dto.course.CourseAddRequest;
import study.studyai.model.dto.course.CourseFileQueryRequest;
import study.studyai.model.dto.course.CourseFileUpdateRequest;
import study.studyai.model.dto.course.CourseQueryRequest;
import study.studyai.model.dto.course.CourseUpdateRequest;
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
@RequestMapping("/teacher")
public class TeacherController {

    @Resource
    private UserService userService;

    @Resource
    private CourseService courseService;

    @Resource
    private FileService fileService;

    @PostMapping("/course/add")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Long> addTeacherCourse(@RequestBody CourseAddRequest courseAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long courseId = courseService.addCourse(courseAddRequest, loginUser);
        return ResultUtils.success(courseId);
    }

    @PostMapping("/course/delete")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> deleteTeacherCourse(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.deleteCourse(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/course/update")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> updateTeacherCourse(@RequestBody CourseUpdateRequest courseUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.updateCourse(courseUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/course/list/page")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Page<Course>> listTeacherCourseByPage(@RequestBody CourseQueryRequest courseQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<Course> coursePage = courseService.listCourseByPage(courseQueryRequest, loginUser);
        return ResultUtils.success(coursePage);
    }

    @PostMapping("/course/file/upload")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<CourseFile> uploadCourseFile(Long courseId, @RequestPart("file") MultipartFile multipartFile, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        CourseFile courseFile = courseService.uploadCourseFile(courseId, multipartFile, loginUser);
        return ResultUtils.success(courseFile);
    }

    @PostMapping("/course/file/delete")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> deleteTeacherCourseFile(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.deleteCourseFile(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/course/file/update")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> updateTeacherCourseFile(@RequestBody CourseFileUpdateRequest courseFileUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.updateCourseFile(courseFileUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/course/file/list/page")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Page<CourseFile>> listTeacherCourseFileByPage(@RequestBody CourseFileQueryRequest courseFileQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<CourseFile> courseFilePage = courseService.listCourseFileByPage(courseFileQueryRequest, loginUser);
        return ResultUtils.success(courseFilePage);
    }

    @GetMapping("/course/file/download")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public void downloadTeacherCourseFile(Long id, HttpServletRequest request, HttpServletResponse response) {
        User loginUser = userService.getLoginUser(request);
        CourseFile courseFile = courseService.getCourseFile(id, loginUser);
        fileService.downloadFileToResponse(courseFile.getFile_key(), response);
    }
}
