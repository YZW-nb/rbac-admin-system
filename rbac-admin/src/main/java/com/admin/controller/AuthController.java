package com.admin.controller;

import com.admin.common.result.Result;
import com.admin.dto.LoginDTO;
import com.admin.service.AuthService;
import com.admin.vo.LoginVO;
import com.admin.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = authService.login(loginDTO);
        return Result.success(loginVO);
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/userinfo")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = getUserId(request);
        UserVO userVO = authService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = getUserId(request);
        authService.logout(userId);
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refresh(@RequestHeader("Refresh-Token") String refreshToken) {
        LoginVO loginVO = authService.refreshToken(refreshToken);
        return Result.success(loginVO);
    }

    private Long getUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof com.admin.entity.SysUser) {
            return ((com.admin.entity.SysUser) auth.getPrincipal()).getId();
        }
        return null;
    }
}
