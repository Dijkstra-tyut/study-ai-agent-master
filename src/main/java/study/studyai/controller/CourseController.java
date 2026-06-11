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
import study.studyai.model.vo.CourseFileVO;
import study.studyai.model.vo.CourseVO;
import study.studyai.service.CourseService;
import study.studyai.service.FileService;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Resource
    private UserService userService;

    @Resource
    private CourseService courseService;

    @Resource
    private FileService fileService;

    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Long> addCourse(@RequestBody CourseAddRequest courseAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseAddRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long courseId = courseService.addCourse(courseAddRequest, loginUser);
        return ResultUtils.success(courseId);
    }

    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> deleteCourse(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.deleteCourse(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> updateCourse(@RequestBody CourseUpdateRequest courseUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.updateCourse(courseUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/list/page")
    @AuthCheck
    public BaseResponse<Page<CourseVO>> listCourseByPage(@RequestBody CourseQueryRequest courseQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<Course> coursePage = courseService.listCourseByPage(courseQueryRequest, loginUser);
        return ResultUtils.success(getCourseVOPage(coursePage));
    }

    @PostMapping("/file/upload")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE) public BaseResponse<CourseFileVO> uploadCourseFile(Long courseId, @RequestPart("file") MultipartFile multipartFile, HttpServletRequest request, Boolean needChapterAnalysis) {
        User loginUser = userService.getLoginUser(request);
        CourseFile courseFile = courseService.uploadCourseFile(courseId, multipartFile, loginUser, needChapterAnalysis);
        return ResultUtils.success(getCourseFileVO(courseFile));
    }

    @PostMapping("/file/delete")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> deleteCourseFile(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.deleteCourseFile(deleteRequest.getId(), loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/file/update")
    @AuthCheck(mustRole = UserConstant.TEACHER_ROLE)
    public BaseResponse<Boolean> updateCourseFile(@RequestBody CourseFileUpdateRequest courseFileUpdateRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean result = courseService.updateCourseFile(courseFileUpdateRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/file/list/page")
    @AuthCheck
    public BaseResponse<Page<CourseFileVO>> listCourseFileByPage(@RequestBody CourseFileQueryRequest courseFileQueryRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(courseFileQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<CourseFile> courseFilePage = courseService.listCourseFileByPage(courseFileQueryRequest, loginUser);
        return ResultUtils.success(getCourseFileVOPage(courseFilePage));
    }

    @GetMapping("/file/download")
    @AuthCheck
    public void downloadCourseFile(Long id, HttpServletRequest request, HttpServletResponse response) {
        User loginUser = userService.getLoginUser(request);
        CourseFile courseFile = courseService.getCourseFile(id, loginUser);
        fileService.downloadFileToResponse(courseFile.getFile_key(), response);
    }

    private Page<CourseVO> getCourseVOPage(Page<Course> coursePage) {
        Page<CourseVO> courseVOPage = new Page<>(coursePage.getCurrent(), coursePage.getSize(), coursePage.getTotal());
        List<CourseVO> courseVOList = coursePage.getRecords().stream()
                .map(this::getCourseVO)
                .collect(Collectors.toList());
        courseVOPage.setRecords(courseVOList);
        return courseVOPage;
    }

    private CourseVO getCourseVO(Course course) {
        if (course == null) {
            return null;
        }
        CourseVO courseVO = new CourseVO();
        courseVO.setCourse_id(course.getCourse_id());
        courseVO.setCourse_name(course.getCourse_name());
        courseVO.setTeacher_id(course.getTeacher_id());
        courseVO.setDescription(course.getDescription());
        courseVO.setCreate_time(course.getCreate_time());
        courseVO.setUpdate_time(course.getUpdate_time());
        return courseVO;
    }

    private Page<CourseFileVO> getCourseFileVOPage(Page<CourseFile> courseFilePage) {
        Page<CourseFileVO> courseFileVOPage = new Page<>(courseFilePage.getCurrent(), courseFilePage.getSize(), courseFilePage.getTotal());
        List<CourseFileVO> courseFileVOList = courseFilePage.getRecords().stream()
                .map(this::getCourseFileVO)
                .collect(Collectors.toList());
        courseFileVOPage.setRecords(courseFileVOList);
        return courseFileVOPage;
    }

    private CourseFileVO getCourseFileVO(CourseFile courseFile) {
        if (courseFile == null) {
            return null;
        }
        CourseFileVO courseFileVO = new CourseFileVO();
        courseFileVO.setId(courseFile.getId());
        courseFileVO.setCourse_id(courseFile.getCourse_id());
        courseFileVO.setTeacher_id(courseFile.getTeacher_id());
        courseFileVO.setFile_name(courseFile.getFile_name());
        courseFileVO.setFile_type(courseFile.getFile_type());
        courseFileVO.setFile_size(courseFile.getFile_size());
        courseFileVO.setCreate_time(courseFile.getCreate_time());
        courseFileVO.setUpdate_time(courseFile.getUpdate_time());
        return courseFileVO;
    }
}
