package study.studyai.service;

import study.studyai.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import study.studyai.model.dto.user.UserAddRequest;
import study.studyai.model.dto.user.UserProfileRequest;
import study.studyai.model.dto.user.UserQueryRequest;
import study.studyai.model.dto.user.UserUpdateRequest;
import study.studyai.model.vo.LoginUserVO;
import study.studyai.model.vo.UserProfileVO;

import jakarta.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【user(用户表 (统一管理所有角色))】的数据库操作Service
* @createDate 2026-06-08 17:20:38
*/
public interface UserService extends IService<User> {
    long userRegister(String username, String password, String checkPassword);
    
    LoginUserVO userLogin(String username, String password, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);

    UserProfileVO updateMyProfile(UserProfileRequest userProfileRequest, HttpServletRequest request);

    UserProfileVO getMyProfile(HttpServletRequest request);

    UserProfileVO updateUserProfile(UserProfileRequest userProfileRequest);

    UserProfileVO getUserProfile(Long userId);

    Long addUser(UserAddRequest userAddRequest);

    boolean deleteUser(Long id);

    boolean updateUser(UserUpdateRequest userUpdateRequest);

    Page<LoginUserVO> listUserByPage(UserQueryRequest userQueryRequest);
}
