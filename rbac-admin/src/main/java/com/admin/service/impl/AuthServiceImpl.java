package com.admin.service.impl;

import com.admin.common.constant.CommonConstant;
import com.admin.common.exception.BusinessException;
import com.admin.common.result.ResultCode;
import com.admin.dto.LoginDTO;
import com.admin.entity.SysMenu;
import com.admin.entity.SysUser;
import com.admin.security.JwtTokenProvider;
import com.admin.service.AuthService;
import com.admin.service.MenuService;
import com.admin.service.UserService;
import com.admin.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import org.springframework.util.StringUtils;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final MenuService menuService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final PasswordEncoder passwordEncoder;

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 15;

    /** Token 过期时间（分钟），从 JwtTokenProvider 动态获取 */
    private int getTokenExpireMinutes() {
        return (int) (jwtTokenProvider.getAccessTokenExpirationSeconds() / 60);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO) {
        // 防暴力破解：检查是否被锁定
        String lockKey = CommonConstant.REDIS_LOGIN_LOCK_PREFIX + loginDTO.getUsername();
        Integer attempts = (Integer) redisTemplate.opsForValue().get(lockKey);
        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            Long ttl = redisTemplate.getExpire(lockKey, TimeUnit.MINUTES);
            log.warn("用户 [{}] 登录被锁定，已失败 {} 次，剩余锁定 {} 分钟",
                    loginDTO.getUsername(), attempts, ttl);
            throw new BusinessException(423, "登录失败次数过多，请" + ttl + "分钟后再试");
        }

        // 1. 查询用户
        SysUser user = userService.getByUsername(loginDTO.getUsername());
        if (user == null) {
            recordLoginFailure(loginDTO.getUsername());
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            recordLoginFailure(loginDTO.getUsername());
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }

        // 3. 检查状态
        if (user.getStatus() != CommonConstant.STATUS_NORMAL) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 登录成功，清除失败计数
        redisTemplate.delete(lockKey);

        // 5. 生成 Token
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        // 6. 存入 Redis
        String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(redisKey, accessToken, getTokenExpireMinutes(), TimeUnit.MINUTES);

        // 7. 更新登录信息
        user.setLoginTime(java.time.LocalDateTime.now());
        userService.updateById(user);

        return LoginVO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .build();
    }

    /**
     * 记录登录失败，递增计数器
     */
    private void recordLoginFailure(String username) {
        String lockKey = CommonConstant.REDIS_LOGIN_LOCK_PREFIX + username;
        Integer attempts = (Integer) redisTemplate.opsForValue().get(lockKey);
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        redisTemplate.opsForValue().set(lockKey, attempts, LOCK_DURATION_MINUTES, TimeUnit.MINUTES);
        log.warn("用户 [{}] 登录失败，第 {} 次（共 {} 次后锁定）", username, attempts, MAX_LOGIN_ATTEMPTS);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setNickname(user.getNickname());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        userVO.setAvatar(user.getAvatar());
        userVO.setDeptId(user.getDeptId());

        // 获取权限标识
        List<String> permissions = userService.getUserPermissions(userId);
        // 管理员拥有所有权限
        if (permissions.isEmpty()) {
            permissions.add("*:*:*");
        }
        userVO.setPermissions(permissions);

        // 获取角色
        List<String> roleCodes = userService.getUserRoleCodes(userId);
        List<SysRoleVO> roles = roleCodes.stream().map(code -> {
            SysRoleVO roleVO = new SysRoleVO();
            roleVO.setRoleCode(code);
            return roleVO;
        }).collect(Collectors.toList());
        userVO.setRoles(roles);

        // 获取菜单路由
        List<SysMenu> menus = menuService.getMenusByUserId(userId);
        List<RouterVO> routes = buildRouters(menus);
        userVO.setRoutes(routes);

        return userVO;
    }

    @Override
    public void logout(Long userId) {
        String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + userId;
        redisTemplate.delete(redisKey);
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResultCode.TOKEN_EXPIRED);
        }
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String username = jwtTokenProvider.getUsernameFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, username);

        String redisKey = CommonConstant.REDIS_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(redisKey, newAccessToken, getTokenExpireMinutes(), TimeUnit.MINUTES);

        return LoginVO.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationSeconds())
                .build();
    }

    /**
     * 构建前端路由
     */
    private List<RouterVO> buildRouters(List<SysMenu> menus) {
        List<RouterVO> routers = new ArrayList<>();
        for (SysMenu menu : menus) {
            RouterVO router = new RouterVO();
            router.setHidden(Objects.equals(CommonConstant.NO, menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setMeta(buildMeta(menu));

            if (menu.getChildren() != null && !menu.getChildren().isEmpty()) {
                router.setChildren(buildRouters(menu.getChildren()));
            }
            routers.add(router);
        }
        return routers;
    }

    private String getRouteName(SysMenu menu) {
        String path = menu.getPath();
        // 首字母大写 + 移除斜杠
        return path.substring(0, 1).toUpperCase() + path.substring(1);
    }

    private String getRouterPath(SysMenu menu) {
        String path = menu.getPath();
        // 一级目录加 /
        if (menu.getParentId() == 0 && CommonConstant.MENU_TYPE_DIR == menu.getMenuType()
                && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
    }

    private String getComponent(SysMenu menu) {
        String component = menu.getComponent();
        if (StringUtils.hasText(component)) {
            return component;
        }
        // 目录级别用 Layout
        if (menu.getParentId() == 0 && CommonConstant.MENU_TYPE_DIR == menu.getMenuType()) {
            return "Layout";
        }
        // 多级目录的父目录
        if (menu.getParentId() != 0 && CommonConstant.MENU_TYPE_DIR == menu.getMenuType()) {
            return "ParentView";
        }
        return "";
    }

    private MetaVO buildMeta(SysMenu menu) {
        MetaVO meta = new MetaVO();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setKeepAlive(true);
        return meta;
    }
}
