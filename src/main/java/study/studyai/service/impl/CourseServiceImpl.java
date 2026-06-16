package study.studyai.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import study.studyai.common.ErrorCode;
import study.studyai.exception.BusinessException;
import study.studyai.manager.FileManager;
import study.studyai.mapper.ChapterMapper;
import study.studyai.mapper.CourseFileMapper;
import study.studyai.mapper.CourseMapper;
import study.studyai.model.dto.course.CourseAddRequest;
import study.studyai.model.dto.course.CourseFileQueryRequest;
import study.studyai.model.dto.course.CourseFileUpdateRequest;
import study.studyai.model.dto.course.CourseQueryRequest;
import study.studyai.model.dto.course.CourseUpdateRequest;
import study.studyai.model.entity.Chapter;
import study.studyai.model.entity.Course;
import study.studyai.model.entity.CourseFile;
import study.studyai.model.entity.User;
import study.studyai.model.enums.FileUploadEnum;
import study.studyai.model.enums.UserRoleEnum;
import study.studyai.model.vo.FileUploadVO;
import study.studyai.service.CourseService;
import study.studyai.service.FileService;
import study.studyai.studyaiagent.app.ValidateCourseApp;
import study.studyai.studyaiagent.course.service.CourseFileAiService;
import study.studyai.studyaiagent.course.model.CourseMarkdownFile;
import study.studyai.studyaiagent.course.service.CourseMarkdownFileService;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private CourseFileMapper courseFileMapper;

    @Resource
    private ChapterMapper chapterMapper;

    @Resource
    private FileService fileService;

    @Resource
    private FileManager fileManager;

    @Resource
    private ValidateCourseApp validateCourseApp;

    @Resource
    private CourseFileAiService courseFileAiService;

    @Resource
    private CourseMarkdownFileService courseMarkdownFileService;

    @Override
    public Long addCourse(CourseAddRequest courseAddRequest, User loginUser) {
        if (courseAddRequest == null || StrUtil.isBlank(courseAddRequest.getCourse_name())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course course = new Course();
        course.setCourse_name(courseAddRequest.getCourse_name());
        course.setDescription(courseAddRequest.getDescription());
        if (isAdmin(loginUser) && courseAddRequest.getTeacher_id() != null) {
            course.setTeacher_id(courseAddRequest.getTeacher_id());
        } else {
            course.setTeacher_id(loginUser.getId());
        }
        int result = courseMapper.insert(course);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return course.getCourse_id();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Long courseId, User loginUser) {
        Course course = getCourseAndCheckOwner(courseId, loginUser);
        QueryWrapper<CourseFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("course_id", course.getCourse_id());
        for (CourseFile courseFile : courseFileMapper.selectList(queryWrapper)) {
            fileService.deleteFile(courseFile.getFile_key());
            deleteMarkdownFile(courseFile);
        }
        courseFileMapper.delete(queryWrapper);
        return courseMapper.deleteById(courseId) > 0;
    }

    @Override
    public boolean updateCourse(CourseUpdateRequest courseUpdateRequest, User loginUser) {
        if (courseUpdateRequest == null || courseUpdateRequest.getCourse_id() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        getCourseAndCheckOwner(courseUpdateRequest.getCourse_id(), loginUser);
        Course course = new Course();
        course.setCourse_id(courseUpdateRequest.getCourse_id());
        if (StrUtil.isNotBlank(courseUpdateRequest.getCourse_name())) {
            course.setCourse_name(courseUpdateRequest.getCourse_name());
        }
        if (courseUpdateRequest.getDescription() != null) {
            course.setDescription(courseUpdateRequest.getDescription());
        }
        if (isAdmin(loginUser) && courseUpdateRequest.getTeacher_id() != null) {
            course.setTeacher_id(courseUpdateRequest.getTeacher_id());
        }
        return courseMapper.updateById(course) > 0;
    }

    @Override
    public Page<Course> listCourseByPage(CourseQueryRequest courseQueryRequest, User loginUser) {
        if (courseQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<Course> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(courseQueryRequest.getCourse_id() != null, "course_id", courseQueryRequest.getCourse_id());
        queryWrapper.like(StrUtil.isNotBlank(courseQueryRequest.getCourse_name()), "course_name", courseQueryRequest.getCourse_name());
        queryWrapper.eq(courseQueryRequest.getTeacher_id() != null, "teacher_id", courseQueryRequest.getTeacher_id());
        return courseMapper.selectPage(new Page<>(courseQueryRequest.getCurrent(), courseQueryRequest.getPageSize()), queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseFile uploadCourseFile(Long courseId, MultipartFile multipartFile, User loginUser, Boolean needChapterAnalysis) {
        Course course = getCourseAndCheckOwner(courseId, loginUser);
        fileManager.validFile(multipartFile, FileUploadEnum.COURSE);
        String markdown = courseFileAiService.convertToMarkdown(multipartFile);
        courseFileAiService.validateCourseFile(course, markdown);
        CourseMarkdownFile courseMarkdownFile = courseMarkdownFileService.uploadMarkdown(markdown, multipartFile.getOriginalFilename());
        FileUploadVO fileUploadVO = null;
        try {
            fileUploadVO = fileService.uploadFile(multipartFile, FileUploadEnum.COURSE);
            CourseFile courseFile = new CourseFile();
            courseFile.setCourse_id(course.getCourse_id());
            courseFile.setTeacher_id(course.getTeacher_id());
            courseFile.setFile_name(fileUploadVO.getFileName());
            courseFile.setFile_key(fileUploadVO.getFileKey());
            courseFile.setFile_url(fileUploadVO.getFileUrl());
            courseFile.setMarkdown_key(courseMarkdownFile.getFileKey());
            courseFile.setMarkdown_url(courseMarkdownFile.getFileUrl());
            courseFile.setFile_type(fileUploadVO.getFileType());
            courseFile.setFile_size(fileUploadVO.getFileSize());
            int result = courseFileMapper.insert(courseFile);
            if (result <= 0) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            if (Boolean.TRUE.equals(needChapterAnalysis)) {
                List<String> chapterNameList = courseFileAiService.analyzeChapterNameList(course, markdown);
                saveChapterNameList(course.getCourse_id(), chapterNameList);
            }
            return courseFile;
        } catch (Exception e) {
            if (fileUploadVO != null) {
                fileService.deleteFile(fileUploadVO.getFileKey());
            }
            fileService.deleteFile(courseMarkdownFile.getFileKey());
            throw e;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourseFile(Long id, User loginUser) {
        CourseFile courseFile = getCourseFileAndCheckOwner(id, loginUser);
        fileService.deleteFile(courseFile.getFile_key());
        deleteMarkdownFile(courseFile);
        return courseFileMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateCourseFile(CourseFileUpdateRequest courseFileUpdateRequest, User loginUser) {
        if (courseFileUpdateRequest == null || courseFileUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        getCourseFileAndCheckOwner(courseFileUpdateRequest.getId(), loginUser);
        CourseFile courseFile = new CourseFile();
        courseFile.setId(courseFileUpdateRequest.getId());
        if (StrUtil.isNotBlank(courseFileUpdateRequest.getFile_name())) {
            courseFile.setFile_name(courseFileUpdateRequest.getFile_name());
        }
        return courseFileMapper.updateById(courseFile) > 0;
    }

    @Override
    public Page<CourseFile> listCourseFileByPage(CourseFileQueryRequest courseFileQueryRequest, User loginUser) {
        if (courseFileQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<CourseFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(courseFileQueryRequest.getCourseId() != null, "course_id", courseFileQueryRequest.getCourseId());
        queryWrapper.eq(courseFileQueryRequest.getTeacherId() != null, "teacher_id", courseFileQueryRequest.getTeacherId());
        queryWrapper.like(StrUtil.isNotBlank(courseFileQueryRequest.getFileName()), "file_name", courseFileQueryRequest.getFileName());
        return courseFileMapper.selectPage(new Page<>(courseFileQueryRequest.getCurrent(), courseFileQueryRequest.getPageSize()), queryWrapper);
    }

    @Override
    public CourseFile getCourseFile(Long id, User loginUser) {
        return getCourseFile(id);
    }

    private Course getCourseAndCheckOwner(Long courseId, User loginUser) {
        if (courseId == null || courseId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (isTeacher(loginUser) && !course.getTeacher_id().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return course;
    }

    private CourseFile getCourseFileAndCheckOwner(Long id, User loginUser) {
        CourseFile courseFile = getCourseFile(id);
        if (isTeacher(loginUser) && !courseFile.getTeacher_id().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return courseFile;
    }

    private CourseFile getCourseFile(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        CourseFile courseFile = courseFileMapper.selectById(id);
        if (courseFile == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return courseFile;
    }

    private void saveChapterNameList(Long courseId, List<String> chapterNameList) {
        if (chapterNameList == null || chapterNameList.isEmpty()) {
            return;
        }
        for (String chapterName : chapterNameList) {
            if (StrUtil.isBlank(chapterName)) {
                continue;
            }
            Chapter chapter = new Chapter();
            chapter.setCourse_id(courseId);
            chapter.setChapter_name(chapterName);
            chapterMapper.insert(chapter);
        }
    }

    private void deleteMarkdownFile(CourseFile courseFile) {
        if (courseFile == null || StrUtil.isBlank(courseFile.getMarkdown_key())) {
            return;
        }
        fileService.deleteFile(courseFile.getMarkdown_key());
    }

    private boolean isAdmin(User user) {
        return UserRoleEnum.ADMIN.getValue().equals(user.getRole());
    }

    private boolean isTeacher(User user) {
        return UserRoleEnum.TEACHER.getValue().equals(user.getRole());
    }
}
