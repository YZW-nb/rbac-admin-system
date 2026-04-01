package com.admin.service.impl;

import com.admin.common.constant.CommonConstant;
import com.admin.common.exception.BusinessException;
import com.admin.common.page.PageParam;
import com.admin.common.page.PageResult;
import com.admin.common.result.ResultCode;
import com.admin.dto.UserDTO;
import com.admin.entity.SysUser;
import com.admin.mapper.SysUserMapper;
import com.admin.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final PasswordEncoder passwordEncoder;

    private static final String PASSWORD_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int RANDOM_PASSWORD_LENGTH = 12;

    @Override
    public PageResult<SysUser> pageList(String username, String phone, Integer status, Long deptId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), SysUser::getUsername, username);
        wrapper.like(StringUtils.hasText(phone), SysUser::getPhone, phone);
        wrapper.eq(status != null, SysUser::getStatus, status);
        wrapper.eq(deptId != null, SysUser::getDeptId, deptId);
        wrapper.orderByDesc(SysUser::getCreateTime);
        PageInfo<SysUser> pageInfo = new PageInfo<>(list(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    public SysUser getUserDetail(Long id) {
        SysUser user = getById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUser(UserDTO userDTO) {
        // 检查用户名唯一
        SysUser exist = getByUsername(userDTO.getUsername());
        if (exist != null) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setDeptId(userDTO.getDeptId());
        user.setStatus(userDTO.getStatus());
        save(user);

        // 保存用户角色关联
        if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            saveUserRoleRelations(user.getId(), userDTO.getRoleIds());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(UserDTO userDTO) {
        SysUser user = getById(userDTO.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        // 修改用户名时检查唯一
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().equals(user.getUsername())) {
            SysUser exist = getByUsername(userDTO.getUsername());
            if (exist != null) {
                throw new BusinessException(ResultCode.USER_EXISTS);
            }
            user.setUsername(userDTO.getUsername());
        }
        user.setNickname(userDTO.getNickname());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setAvatar(userDTO.getAvatar());
        user.setDeptId(userDTO.getDeptId());
        user.setStatus(userDTO.getStatus());
        if (StringUtils.hasText(userDTO.getPassword())) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        updateById(user);

        // 更新用户角色关联
        if (userDTO.getRoleIds() != null) {
            removeUserRoleRelations(user.getId());
            if (!userDTO.getRoleIds().isEmpty()) {
                saveUserRoleRelations(user.getId(), userDTO.getRoleIds());
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUsers(List<Long> ids) {
        // 不能删除管理员
        if (ids.contains(1L)) {
            throw new BusinessException("不能删除超级管理员");
        }
        removeByIds(ids);
        // 删除用户角色关联
        for (Long userId : ids) {
            removeUserRoleRelations(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long userId, Integer status) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setStatus(status);
        updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String resetPassword(Long userId) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        String randomPassword = generateRandomPassword();
        log.info("重置用户 [{}] 的密码", user.getUsername());
        user.setPassword(passwordEncoder.encode(randomPassword));
        updateById(user);
        return randomPassword;
    }

    /**
     * 生成随机密码（排除容易混淆的字符：0/O、1/l/I）
     */
    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(RANDOM_PASSWORD_LENGTH);
        for (int i = 0; i < RANDOM_PASSWORD_LENGTH; i++) {
            sb.append(PASSWORD_CHARS.charAt(random.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public SysUser getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        return baseMapper.selectPermsByUserId(userId);
    }

    @Override
    public List<String> getUserRoleCodes(Long userId) {
        return baseMapper.selectRoleCodesByUserId(userId);
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoleRelations(Long userId, List<Long> roleIds) {
        baseMapper.insertUserRoleRelations(userId, roleIds);
    }

    /**
     * 删除用户角色关联
     */
    private void removeUserRoleRelations(Long userId) {
        baseMapper.deleteUserRoleRelations(userId);
    }
}
