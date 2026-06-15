package study.studyai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import study.studyai.common.ErrorCode;
import study.studyai.constant.UserConstant;
import study.studyai.exception.BusinessException;
import study.studyai.mapper.StudentMapper;
import study.studyai.mapper.TeacherMapper;
import study.studyai.model.dto.user.UserAddRequest;
import study.studyai.model.dto.user.UserProfileRequest;
import study.studyai.model.dto.user.UserQueryRequest;
import study.studyai.model.dto.user.UserUpdateRequest;
import study.studyai.model.entity.Student;
import study.studyai.model.entity.Teacher;
import study.studyai.model.entity.User;
import study.studyai.model.enums.UserRoleEnum;
import study.studyai.model.vo.LoginUserVO;
import study.studyai.model.vo.UserProfileVO;
import study.studyai.service.UserService;
import study.studyai.mapper.UserMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author Lenovo
* @description 针对表【user(用户表 (统一管理所有角色))】的数据库操作Service实现
* @createDate 2026-06-08 17:20:38
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public long userRegister(String username, String password, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(username, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        
        // 2. 检查是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        
        // 3. 加密
        String encryptPassword = getEncryptPassword(password);
        
        // 4. 插入数据
        User user = new User();
        user.setUsername(username);
        user.setPassword(encryptPassword);
        user.setRole(UserRoleEnum.USER.getValue());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public LoginUserVO userLogin(String username, String password, HttpServletRequest request) {
        // 1. 校验
        if (StrUtil.hasBlank(username,password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        
        // 2. 加密
        String encryptPassword = getEncryptPassword(password);
        
        // 3. 查询用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        
        // 4. 用户不存在或密码错误
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 4. 保存用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user.getId());

        return this.getLoginUserVO(user);
    }


    /**
     * 获取脱敏的登录用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtil.copyProperties(user, loginUserVO);
        loginUserVO.setCreateTime(user.getCreate_time());
        loginUserVO.setUpdateTime(user.getUpdate_time());
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询 补充user数据
        Long userId = (Long) userObj;
        User currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        // 判断是否已经登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateMyProfile(UserProfileRequest userProfileRequest, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        userProfileRequest.setUserId(loginUser.getId());
        return this.updateUserProfile(userProfileRequest);
    }

    @Override
    public UserProfileVO getMyProfile(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        return this.getUserProfile(loginUser.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserProfileVO updateUserProfile(UserProfileRequest userProfileRequest) {
        if (userProfileRequest == null || userProfileRequest.getUserId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String role = userProfileRequest.getRole();
        if (!UserRoleEnum.STUDENT.getValue().equals(role) && !UserRoleEnum.TEACHER.getValue().equals(role)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色只能选择学生或教师");
        }
        User user = this.getById(userProfileRequest.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        user.setRole(role);
        if (StrUtil.isNotBlank(userProfileRequest.getAvatar())) {
            user.setAvatar(userProfileRequest.getAvatar());
        }
        boolean updateResult = this.updateById(user);
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        if (UserRoleEnum.STUDENT.getValue().equals(role)) {
            saveStudentProfile(user.getId(), userProfileRequest);
            teacherMapper.deleteById(user.getId());
        }
        if (UserRoleEnum.TEACHER.getValue().equals(role)) {
            saveTeacherProfile(user.getId(), userProfileRequest);
            studentMapper.deleteById(user.getId());
        }
        return this.getUserProfile(user.getId());
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        UserProfileVO userProfileVO = new UserProfileVO();
        userProfileVO.setUser(this.getLoginUserVO(user));
        if (UserRoleEnum.STUDENT.getValue().equals(user.getRole())) {
            userProfileVO.setStudent(studentMapper.selectById(userId));
        }
        if (UserRoleEnum.TEACHER.getValue().equals(user.getRole())) {
            userProfileVO.setTeacher(teacherMapper.selectById(userId));
        }
        return userProfileVO;
    }

    @Override
    public Long addUser(UserAddRequest userAddRequest) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String username = userAddRequest.getUsername();
        String password = userAddRequest.getPassword();
        if (StrUtil.hasBlank(username, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (username.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        checkUsername(username, null);
        String role = userAddRequest.getRole();
        if (StrUtil.isBlank(role)) {
            role = UserRoleEnum.USER.getValue();
        }
        checkRole(role);
        User user = new User();
        user.setUsername(username);
        user.setPassword(getEncryptPassword(password));
        user.setRole(role);
        user.setAvatar(userAddRequest.getAvatar());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        studentMapper.deleteById(id);
        teacherMapper.deleteById(id);
        return this.removeById(id);
    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User oldUser = this.getById(userUpdateRequest.getId());
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User user = new User();
        user.setId(userUpdateRequest.getId());
        if (StrUtil.isNotBlank(userUpdateRequest.getUsername())) {
            checkUsername(userUpdateRequest.getUsername(), userUpdateRequest.getId());
            user.setUsername(userUpdateRequest.getUsername());
        }
        if (StrUtil.isNotBlank(userUpdateRequest.getPassword())) {
            if (userUpdateRequest.getPassword().length() < 8) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
            }
            user.setPassword(getEncryptPassword(userUpdateRequest.getPassword()));
        }
        if (StrUtil.isNotBlank(userUpdateRequest.getRole())) {
            checkRole(userUpdateRequest.getRole());
            user.setRole(userUpdateRequest.getRole());
        }
        if (StrUtil.isNotBlank(userUpdateRequest.getAvatar())) {
            user.setAvatar(userUpdateRequest.getAvatar());
        }
        return this.updateById(user);
    }

    @Override
    public Page<LoginUserVO> listUserByPage(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(userQueryRequest.getId() != null, "id", userQueryRequest.getId());
        queryWrapper.like(StrUtil.isNotBlank(userQueryRequest.getUsername()), "username", userQueryRequest.getUsername());
        queryWrapper.eq(StrUtil.isNotBlank(userQueryRequest.getRole()), "role", userQueryRequest.getRole());
        Page<User> userPage = this.page(new Page<>(current, pageSize), queryWrapper);
        Page<LoginUserVO> loginUserVOPage = new Page<>(current, pageSize, userPage.getTotal());
        List<LoginUserVO> loginUserVOList = userPage.getRecords().stream()
                .map(this::getLoginUserVO)
                .collect(Collectors.toList());
        loginUserVOPage.setRecords(loginUserVOList);
        return loginUserVOPage;
    }

    /**
     * 密码加密
     */
    private String getEncryptPassword(String password) {
        final String SALT = "TYUT";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }

    private void saveStudentProfile(Long userId, UserProfileRequest userProfileRequest) {
        Student student = new Student();
        student.setStudent_id(userId);
        student.setMajor(userProfileRequest.getMajor());
        student.setGrade(userProfileRequest.getGrade());
        student.setLearning_target(userProfileRequest.getLearning_target());
        student.setInterest_direction(userProfileRequest.getInterest_direction());
        student.setKnowledge_level(userProfileRequest.getKnowledge_level());
        if (studentMapper.selectById(userId) == null) {
            studentMapper.insert(student);
        } else {
            studentMapper.updateById(student);
        }
    }

    private void saveTeacherProfile(Long userId, UserProfileRequest userProfileRequest) {
        Teacher teacher = new Teacher();
        teacher.setTeacher_id(userId);
        teacher.setTeacher_name(userProfileRequest.getTeacher_name());
        teacher.setResearch_area(userProfileRequest.getResearch_area());
        teacher.setIntro(userProfileRequest.getIntro());
        if (teacherMapper.selectById(userId) == null) {
            teacherMapper.insert(teacher);
        } else {
            teacherMapper.updateById(teacher);
        }
    }

    private void checkUsername(String username, Long id) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.ne(id != null, "id", id);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
    }

    private void checkRole(String role) {
        if (UserRoleEnum.getEnumByValue(role) == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "角色不存在");
        }
    }
}


