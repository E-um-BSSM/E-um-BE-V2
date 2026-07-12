package com.example.eumbev2.controller;

import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.dto.notice.*;
import com.example.eumbev2.service.notice.NoticeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes/{classId}/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoticeResponse create(@PathVariable Long classId, @Valid @RequestBody NoticeCreateRequest request) {
        return noticeService.create(classId, request);
    }

    @GetMapping
    public PageResponse<NoticeResponse> list(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return noticeService.list(classId, PageRequest.of(page, size));
    }

    @GetMapping("/{noticeId}")
    public NoticeResponse getOne(@PathVariable Long classId, @PathVariable Long noticeId) {
        return noticeService.getOne(classId, noticeId);
    }

    @PatchMapping("/{noticeId}")
    public NoticeResponse update(@PathVariable Long classId, @PathVariable Long noticeId, @Valid @RequestBody NoticeUpdateRequest request) {
        return noticeService.update(classId, noticeId, request);
    }

    @DeleteMapping("/{noticeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long classId, @PathVariable Long noticeId) {
        noticeService.delete(classId, noticeId);
    }
}
