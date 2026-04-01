package com.admin.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用树形结构构建工具
 * 用于将扁平列表转换为树形结构
 */
public class TreeBuildUtil {

    /**
     * 构建树形结构
     *
     * @param list       扁平列表
     * @param idGetter   获取节点 ID 的函数
     * @param parentIdGetter 获取父节点 ID 的函数
     * @param childrenSetter  设置子节点列表的函数
     * @param rootParentId    根节点的父 ID 值（通常为 0）
     * @param <T>        节点类型
     * @return 树形结构列表
     */
    public static <T> List<T> build(List<T> list,
                                     Function<T, Long> idGetter,
                                     Function<T, Long> parentIdGetter,
                                     ChildrenSetter<T> childrenSetter,
                                     long rootParentId) {
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }

        // 按 parentId 分组
        List<T> roots = new ArrayList<>();
        java.util.Map<Long, List<T>> childrenMap = list.stream()
                .filter(node -> parentIdGetter.apply(node) != null)
                .collect(Collectors.groupingBy(parentIdGetter));

        for (T node : list) {
            Long parentId = parentIdGetter.apply(node);
            if (parentId == null || parentId == rootParentId) {
                roots.add(node);
            }
            List<T> children = childrenMap.get(idGetter.apply(node));
            if (children != null) {
                childrenSetter.set(node, children);
            }
        }

        return roots;
    }

    @FunctionalInterface
    public interface ChildrenSetter<T> {
        void set(T parent, List<T> children);
    }
}
