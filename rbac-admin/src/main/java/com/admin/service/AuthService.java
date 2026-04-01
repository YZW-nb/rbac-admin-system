package com.admin.service;

import com.admin.dto.LoginDTO;
import com.admin.vo.LoginVO;
import com.admin.vo.UserVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 获取当前用户信息（含权限菜单）
     */
    UserVO getUserInfo(Long userId);

    /**
     * 登出
     */
    void logout(Long userId);

    /**
     * 刷新 Token
     */
    LoginVO refreshToken(String refreshToken);
}
