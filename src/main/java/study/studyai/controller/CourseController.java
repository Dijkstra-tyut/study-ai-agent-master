package study.studyai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
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
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private UserService userService;

    @Resource
    private CourseService courseService;

    @PostMapping("/admin/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addCourse(@RequestBody CourseAddRequest courseAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long courseId = courseService.addCourse(courseAddRequest, loginUser);
        return ResultUtils.success(courseId);
    }

    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCourse(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.deleteCourse(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCourse(@RequestBody CourseUpdateRequest courseUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.updateCourse(courseUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Course>> listCourseByPage(@RequestBody CourseQueryRequest courseQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<Course> coursePage = courseService.listCourseByPage(courseQueryRequest, loginUser);
        return ResultUtils.success(coursePage);
    }

    @PostMapping("/admin/file/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCourseFile(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.deleteCourseFile(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/file/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateCourseFile(@RequestBody CourseFileUpdateRequest courseFileUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.updateCourseFile(courseFileUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/file/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CourseFile>> listCourseFileByPage(@RequestBody CourseFileQueryRequest courseFileQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<CourseFile> courseFilePage = courseService.listCourseFileByPage(courseFileQueryRequest, loginUser);
        return ResultUtils.success(courseFilePage);
    }
}
