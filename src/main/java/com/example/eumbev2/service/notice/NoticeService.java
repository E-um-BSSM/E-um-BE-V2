package com.example.eumbev2.service.notice;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.common.security.SecurityUtils;
import com.example.eumbev2.dto.notice.*;
import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.notice.Notice;
import com.example.eumbev2.repository.notice.NoticeRepository;
import com.example.eumbev2.service.classroom.ClassroomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NoticeService {

    private final ClassroomService classroomService;
    private final NoticeRepository noticeRepository;

    public NoticeService(ClassroomService classroomService, NoticeRepository noticeRepository) {
        this.classroomService = classroomService;
        this.noticeRepository = noticeRepository;
    }

    public NoticeResponse create(Long classId, NoticeCreateRequest request) {
        Classroom classroom = classroomService.getOrThrow(classId);
        classroomService.requireMentor(classroom);

        Notice notice = Notice.builder()
                .classroom(classroom)
                .author(SecurityUtils.getCurrentUser())
                .title(request.title())
                .content(request.content())
                .build();
        noticeRepository.save(notice);
        return NoticeResponse.from(notice);
    }

    @Transactional(readOnly = true)
    public PageResponse<NoticeResponse> list(Long classId, Pageable pageable) {
        Classroom classroom = classroomService.getOrThrow(classId);
        Page<Notice> page = noticeRepository.findByClassroomOrderByCreatedAtDesc(classroom, pageable);
        return PageResponse.from(page, NoticeResponse::from);
    }

    @Transactional(readOnly = true)
    public NoticeResponse getOne(Long classId, Long noticeId) {
        return NoticeResponse.from(getOrThrow(classId, noticeId));
    }

    public NoticeResponse update(Long classId, Long noticeId, NoticeUpdateRequest request) {
        Notice notice = getOrThrow(classId, noticeId);
        classroomService.requireMentor(notice.getClassroom());

        if (request.title() != null) notice.setTitle(request.title());
        if (request.content() != null) notice.setContent(request.content());

        return NoticeResponse.from(notice);
    }

    public void delete(Long classId, Long noticeId) {
        Notice notice = getOrThrow(classId, noticeId);
        classroomService.requireMentor(notice.getClassroom());
        noticeRepository.delete(notice);
    }

    private Notice getOrThrow(Long classId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ApiException(ErrorCode.NOTICE_NOT_FOUND));
        if (!notice.getClassroom().getId().equals(classId)) {
            throw new ApiException(ErrorCode.NOTICE_NOT_FOUND);
        }
        return notice;
    }
}
