package com.admin.service.impl;

import com.admin.common.exception.BusinessException;
import com.admin.common.page.PageParam;
import com.admin.common.page.PageResult;
import com.admin.dto.DictDataDTO;
import com.admin.dto.DictTypeDTO;
import com.admin.entity.SysDictData;
import com.admin.entity.SysDictType;
import com.admin.mapper.SysDictDataMapper;
import com.admin.mapper.SysDictTypeMapper;
import com.admin.service.DictService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 字典服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    private final SysDictTypeMapper dictTypeMapper;
    private final SysDictDataMapper dictDataMapper;

    @Override
    public PageResult<SysDictType> typePageList(String dictName, String dictType, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysDictType> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(dictName), SysDictType::getDictName, dictName);
        wrapper.like(StringUtils.hasText(dictType), SysDictType::getDictType, dictType);
        wrapper.orderByDesc(SysDictType::getCreateTime);
        PageInfo<SysDictType> pageInfo = new PageInfo<>(dictTypeMapper.selectList(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addType(DictTypeDTO dictTypeDTO) {
        // 检查字典类型编码唯一
        long count = dictTypeMapper.selectCount(new LambdaQueryWrapper<SysDictType>()
                .eq(SysDictType::getDictType, dictTypeDTO.getDictType()));
        if (count > 0) {
            throw new BusinessException("字典类型编码已存在");
        }
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(dictTypeDTO, dictType);
        dictTypeMapper.insert(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateType(DictTypeDTO dictTypeDTO) {
        SysDictType dictType = new SysDictType();
        BeanUtils.copyProperties(dictTypeDTO, dictType);
        dictTypeMapper.updateById(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTypes(List<Long> ids) {
        // 批量查询字典类型，避免 N+1 查询
        List<SysDictType> dictTypes = dictTypeMapper.selectBatchIds(ids);
        List<String> dictTypeCodes = dictTypes.stream()
                .map(SysDictType::getDictType)
                .collect(Collectors.toList());
        // 批量删除关联的字典数据
        if (!dictTypeCodes.isEmpty()) {
            dictDataMapper.delete(new LambdaQueryWrapper<SysDictData>()
                    .in(SysDictData::getDictType, dictTypeCodes));
        }
        dictTypeMapper.deleteBatchIds(ids);
    }

    @Override
    public PageResult<SysDictData> dataPageList(String dictType, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, PageParam.limitPageSize(pageSize));
        LambdaQueryWrapper<SysDictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(dictType), SysDictData::getDictType, dictType);
        wrapper.orderByAsc(SysDictData::getSort);
        PageInfo<SysDictData> pageInfo = new PageInfo<>(dictDataMapper.selectList(wrapper));
        return PageResult.of(pageInfo.getList(), pageInfo.getTotal());
    }

    @Override
    public List<SysDictData> getDictDataByType(String dictType) {
        return dictDataMapper.selectDictDataByType(dictType);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addData(DictDataDTO dictDataDTO) {
        SysDictData dictData = new SysDictData();
        BeanUtils.copyProperties(dictDataDTO, dictData);
        dictDataMapper.insert(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateData(DictDataDTO dictDataDTO) {
        SysDictData dictData = new SysDictData();
        BeanUtils.copyProperties(dictDataDTO, dictData);
        dictDataMapper.updateById(dictData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataList(List<Long> ids) {
        dictDataMapper.deleteBatchIds(ids);
    }
}
