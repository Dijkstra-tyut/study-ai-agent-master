package study.studyai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.util.DigestUtils;
import study.studyai.common.ErrorCode;
import study.studyai.constant.UserConstant;
import study.studyai.exception.BusinessException;
import study.studyai.model.entity.User;
import study.studyai.model.enums.UserRoleEnum;
import study.studyai.model.vo.LoginUserVO;
import study.studyai.service.UserService;
import study.studyai.mapper.UserMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
* @author Lenovo
* @description 针对表【user(用户表 (统一管理所有角色))】的数据库操作Service实现
* @createDate 2026-06-08 17:20:38
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

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
        return loginUserVO;
    }

    /**
     * 获取当前登录用户
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中查询 补充user数据
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
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

    /**
     * 密码加密
     */
    private String getEncryptPassword(String password) {
        final String SALT = "TYUT";
        return DigestUtils.md5DigestAsHex((SALT + password).getBytes());
    }
}




