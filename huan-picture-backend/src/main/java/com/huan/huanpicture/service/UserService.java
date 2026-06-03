package com.huan.huanpicture.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.huan.huanpicture.model.dto.user.UserLoginRequest;
import com.huan.huanpicture.model.dto.user.UserQueryRequest;
import com.huan.huanpicture.model.dto.user.UserRegisterRequest;
import com.huan.huanpicture.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.huan.huanpicture.model.vo.LoginUserVO;
import com.huan.huanpicture.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Huan
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-11-18 10:38:12
 */
public interface UserService extends IService<User> {
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取加密后的密码
     *
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);

    LoginUserVO getUserLoginVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    boolean userLogout(HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 判断用户是否为管理员（admin）
     *
     * @return
     */
    boolean isAdmin(User user);
}
