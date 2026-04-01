package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.common.page.PageResult;
import com.admin.common.result.Result;
import com.admin.dto.DictDataDTO;
import com.admin.dto.DictTypeDTO;
import com.admin.entity.SysDictData;
import com.admin.entity.SysDictType;
import com.admin.service.DictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.admin.annotation.BusinessType.*;

/**
 * 字典管理控制器
 */
@Tag(name = "字典管理")
@RestController
@RequestMapping("/api/system/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ========== 字典类型 ==========

    @Operation(summary = "字典类型列表")
    @PreAuthorize("@ss.hasPerm('sys:dict:list')")
    @GetMapping("/type/list")
    public Result<PageResult<SysDictType>> typeList(
            @RequestParam(required = false) String dictName,
            @RequestParam(required = false) String dictType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(dictService.typePageList(dictName, dictType, pageNum, pageSize));
    }

    @Operation(summary = "新增字典类型")
    @Log(title = "字典管理", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:dict:add')")
    @PostMapping("/type")
    public Result<Void> addType(@Valid @RequestBody DictTypeDTO dictTypeDTO) {
        dictService.addType(dictTypeDTO);
        return Result.success();
    }

    @Operation(summary = "修改字典类型")
    @Log(title = "字典管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:dict:edit')")
    @PutMapping("/type")
    public Result<Void> editType(@Valid @RequestBody DictTypeDTO dictTypeDTO) {
        dictService.updateType(dictTypeDTO);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @Log(title = "字典管理", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:dict:remove')")
    @DeleteMapping("/type/{ids}")
    public Result<Void> removeType(@PathVariable List<Long> ids) {
        dictService.deleteTypes(ids);
        return Result.success();
    }

    // ========== 字典数据 ==========

    @Operation(summary = "字典数据列表")
    @PreAuthorize("@ss.hasPerm('sys:dict:list')")
    @GetMapping("/data/list")
    public Result<PageResult<SysDictData>> dataList(
            @RequestParam(required = false) String dictType,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(dictService.dataPageList(dictType, pageNum, pageSize));
    }

    @Operation(summary = "根据类型查询字典数据（前端用）")
    @GetMapping("/data/type/{dictType}")
    public Result<List<SysDictData>> getDictDataByType(@PathVariable String dictType) {
        return Result.success(dictService.getDictDataByType(dictType));
    }

    @Operation(summary = "新增字典数据")
    @Log(title = "字典管理", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:dict:add')")
    @PostMapping("/data")
    public Result<Void> addData(@Valid @RequestBody DictDataDTO dictDataDTO) {
        dictService.addData(dictDataDTO);
        return Result.success();
    }

    @Operation(summary = "修改字典数据")
    @Log(title = "字典管理", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:dict:edit')")
    @PutMapping("/data")
    public Result<Void> editData(@Valid @RequestBody DictDataDTO dictDataDTO) {
        dictService.updateData(dictDataDTO);
        return Result.success();
    }

    @Operation(summary = "删除字典数据")
    @Log(title = "字典管理", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:dict:remove')")
    @DeleteMapping("/data/{ids}")
    public Result<Void> removeData(@PathVariable List<Long> ids) {
        dictService.deleteDataList(ids);
        return Result.success();
    }
}
