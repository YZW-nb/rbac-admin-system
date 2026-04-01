package com.admin.service.impl;

import com.admin.common.constant.CommonConstant;
import com.admin.common.exception.BusinessException;
import com.admin.common.result.ResultCode;
import com.admin.dto.MenuDTO;
import com.admin.common.util.TreeBuildUtil;
import com.admin.entity.SysMenu;
import com.admin.mapper.SysMenuMapper;
import com.admin.service.MenuService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 菜单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements MenuService {

    @Override
    public List<SysMenu> getMenuTree(String menuName, Integer status) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(menuName), SysMenu::getMenuName, menuName);
        wrapper.eq(status != null, SysMenu::getStatus, status);
        wrapper.orderByAsc(SysMenu::getParentId, SysMenu::getSort);
        List<SysMenu> allMenus = list(wrapper);
        return buildTree(allMenus);
    }

    @Override
    public List<SysMenu> getMenuSelectTree() {
        List<SysMenu> allMenus = list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getStatus, CommonConstant.STATUS_NORMAL)
                .orderByAsc(SysMenu::getParentId, SysMenu::getSort));
        return buildTree(allMenus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(MenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        menu.setParentId(menuDTO.getParentId());
        menu.setMenuName(menuDTO.getMenuName());
        menu.setMenuType(menuDTO.getMenuType());
        menu.setPath(menuDTO.getPath());
        menu.setComponent(menuDTO.getComponent());
        menu.setPerms(menuDTO.getPerms());
        menu.setIcon(menuDTO.getIcon());
        menu.setSort(menuDTO.getSort());
        menu.setVisible(menuDTO.getVisible() != null ? menuDTO.getVisible() : CommonConstant.YES);
        menu.setStatus(menuDTO.getStatus());
        save(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(MenuDTO menuDTO) {
        SysMenu menu = new SysMenu();
        menu.setId(menuDTO.getId());
        menu.setParentId(menuDTO.getParentId());
        menu.setMenuName(menuDTO.getMenuName());
        menu.setMenuType(menuDTO.getMenuType());
        menu.setPath(menuDTO.getPath());
        menu.setComponent(menuDTO.getComponent());
        menu.setPerms(menuDTO.getPerms());
        menu.setIcon(menuDTO.getIcon());
        menu.setSort(menuDTO.getSort());
        menu.setVisible(menuDTO.getVisible());
        menu.setStatus(menuDTO.getStatus());
        updateById(menu);
    }

    @Override
    @Transactional
    public void deleteMenu(Long id) {
        // 检查是否有子菜单
        long childCount = count(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, id));
        if (childCount > 0) {
            throw new BusinessException(ResultCode.MENU_HAS_CHILDREN);
        }
        removeById(id);
    }

    @Override
    public List<SysMenu> getMenusByUserId(Long userId) {
        List<SysMenu> menus = baseMapper.selectMenusByUserId(userId);
        return buildTree(menus);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildTree(List<SysMenu> menus) {
        return TreeBuildUtil.build(menus, SysMenu::getId, SysMenu::getParentId,
                SysMenu::setChildren, 0L);
    }
}
