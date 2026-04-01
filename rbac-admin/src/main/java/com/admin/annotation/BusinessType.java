package com.admin.annotation;

/**
 * 业务操作类型枚举
 */
public enum BusinessType {
    /** 查询 */
    QUERY,
    /** 新增 */
    INSERT,
    /** 修改 */
    UPDATE,
    /** 删除 */
    DELETE,
    /** 导出 */
    EXPORT,
    /** 导入 */
    IMPORT,
    /** 其他 */
    OTHER
}
