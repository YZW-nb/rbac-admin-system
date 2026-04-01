package com.admin.service;

import com.admin.common.page.PageResult;
import com.admin.dto.DictDataDTO;
import com.admin.dto.DictTypeDTO;
import com.admin.entity.SysDictData;
import com.admin.entity.SysDictType;

import java.util.List;

/**
 * 字典服务接口
 */
public interface DictService {

    // ========== 字典类型 ==========

    /**
     * 分页查询字典类型列表
     *
     * @param dictName 字典名称（模糊匹配，可为空）
     * @param dictType 字典类型编码（模糊匹配，可为空）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<SysDictType> typePageList(String dictName, String dictType, int pageNum, int pageSize);

    /**
     * 新增字典类型
     *
     * @param dictTypeDTO 字典类型数据
     */
    void addType(DictTypeDTO dictTypeDTO);

    /**
     * 修改字典类型
     *
     * @param dictTypeDTO 字典类型数据
     */
    void updateType(DictTypeDTO dictTypeDTO);

    /**
     * 批量删除字典类型及其关联的字典数据
     *
     * @param ids 字典类型ID列表
     */
    void deleteTypes(List<Long> ids);

    // ========== 字典数据 ==========

    /**
     * 分页查询字典数据列表
     *
     * @param dictType 字典类型编码（精确匹配）
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<SysDictData> dataPageList(String dictType, int pageNum, int pageSize);

    /**
     * 根据字典类型编码查询字典数据列表
     *
     * @param dictType 字典类型编码
     * @return 字典数据列表
     */
    List<SysDictData> getDictDataByType(String dictType);

    /**
     * 新增字典数据
     *
     * @param dictDataDTO 字典数据
     */
    void addData(DictDataDTO dictDataDTO);

    /**
     * 修改字典数据
     *
     * @param dictDataDTO 字典数据
     */
    void updateData(DictDataDTO dictDataDTO);

    /**
     * 批量删除字典数据
     *
     * @param ids 字典数据ID列表
     */
    void deleteDataList(List<Long> ids);
}
