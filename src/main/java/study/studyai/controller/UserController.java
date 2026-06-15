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
import study.studyai.model.dto.user.UserAddRequest;
import study.studyai.model.dto.user.UserLoginRequest;
import study.studyai.model.dto.user.UserProfileRequest;
import study.studyai.model.dto.user.UserQueryRequest;
import study.studyai.model.dto.user.UserRegisterRequest;
import study.studyai.model.dto.user.UserUpdateRequest;
import study.studyai.model.entity.User;
import study.studyai.model.vo.LoginUserVO;
import study.studyai.model.vo.UserProfileVO;
import study.studyai.service.UserService;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {;
            throw new IllegalArgumentException("请求参数不能为空");
        }
        String username = userRegisterRequest.getUsername();
        String password = userRegisterRequest.getPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        long result = userService.userRegister(username, password, checkPassword);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);
        if (userLoginRequest == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        String username = userLoginRequest.getUsername();
        String password = userLoginRequest.getPassword();
        LoginUserVO loginUserVO = userService.userLogin(username, password, request);
        return ResultUtils.success(loginUserVO);
    }

    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求参数不能为空");
        }
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @PostMapping("/profile/update")
    public BaseResponse<UserProfileVO> updateMyProfile(@RequestBody UserProfileRequest userProfileRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userProfileRequest == null, ErrorCode.PARAMS_ERROR);
        UserProfileVO userProfileVO = userService.updateMyProfile(userProfileRequest, request);
        return ResultUtils.success(userProfileVO);
    }

    @GetMapping("/profile/get")
    public BaseResponse<UserProfileVO> getMyProfile(HttpServletRequest request) {
        UserProfileVO userProfileVO = userService.getMyProfile(request);
        return ResultUtils.success(userProfileVO);
    }

    @PostMapping("/admin/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);
        Long userId = userService.addUser(userAddRequest);
        return ResultUtils.success(userId);
    }

    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.deleteUser(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean result = userService.updateUser(userUpdateRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<LoginUserVO>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Page<LoginUserVO> userPage = userService.listUserByPage(userQueryRequest);
        return ResultUtils.success(userPage);
    }

    @GetMapping("/admin/profile/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserProfileVO> getUserProfile(Long userId) {
        UserProfileVO userProfileVO = userService.getUserProfile(userId);
        return ResultUtils.success(userProfileVO);
    }

    @PostMapping("/admin/profile/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserProfileVO> updateUserProfile(@RequestBody UserProfileRequest userProfileRequest) {
        ThrowUtils.throwIf(userProfileRequest == null || userProfileRequest.getUserId() == null, ErrorCode.PARAMS_ERROR);
        UserProfileVO userProfileVO = userService.updateUserProfile(userProfileRequest);
        return ResultUtils.success(userProfileVO);
    }

}
