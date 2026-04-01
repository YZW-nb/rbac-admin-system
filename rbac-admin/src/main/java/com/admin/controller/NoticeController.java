package com.admin.controller;

import com.admin.annotation.Log;
import com.admin.common.page.PageResult;
import com.admin.common.result.Result;
import com.admin.dto.NoticeDTO;
import com.admin.entity.SysNotice;
import com.admin.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.admin.annotation.BusinessType.*;

/**
 * 通知公告控制器
 */
@Tag(name = "通知公告")
@RestController
@RequestMapping("/api/system/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "公告列表")
    @PreAuthorize("@ss.hasPerm('sys:notice:list')")
    @GetMapping("/list")
    public Result<PageResult<SysNotice>> list(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer noticeType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(noticeService.pageList(title, noticeType, status, pageNum, pageSize));
    }

    @Operation(summary = "公告详情")
    @PreAuthorize("@ss.hasPerm('sys:notice:query')")
    @GetMapping("/{id}")
    public Result<SysNotice> getInfo(@PathVariable Long id) {
        return Result.success(noticeService.getNoticeDetail(id));
    }

    @Operation(summary = "新增公告")
    @Log(title = "通知公告", businessType = INSERT)
    @PreAuthorize("@ss.hasPerm('sys:notice:add')")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody NoticeDTO noticeDTO) {
        noticeService.addNotice(noticeDTO);
        return Result.success();
    }

    @Operation(summary = "修改公告")
    @Log(title = "通知公告", businessType = UPDATE)
    @PreAuthorize("@ss.hasPerm('sys:notice:edit')")
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody NoticeDTO noticeDTO) {
        noticeService.updateNotice(noticeDTO);
        return Result.success();
    }

    @Operation(summary = "删除公告")
    @Log(title = "通知公告", businessType = DELETE)
    @PreAuthorize("@ss.hasPerm('sys:notice:remove')")
    @DeleteMapping("/{ids}")
    public Result<Void> remove(@PathVariable List<Long> ids) {
        noticeService.deleteNotices(ids);
        return Result.success();
    }
}
