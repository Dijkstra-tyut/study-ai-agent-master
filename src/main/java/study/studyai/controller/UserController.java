package study.studyai.controller;

import org.springframework.web.bind.annotation.*;

import study.studyai.common.BaseResponse;
import study.studyai.common.ErrorCode;
import study.studyai.common.ResultUtils;
import study.studyai.exception.ThrowUtils;
import study.studyai.model.dto.user.UserLoginRequest;
import study.studyai.model.dto.user.UserRegisterRequest;
import study.studyai.model.entity.User;
import study.studyai.model.vo.LoginUserVO;
import study.studyai.service.UserService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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




}
