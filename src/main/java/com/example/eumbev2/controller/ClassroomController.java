package com.example.eumbev2.controller;

import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.dto.classroom.*;
import com.example.eumbev2.dto.member.JoinByCodeRequest;
import com.example.eumbev2.dto.member.MemberResponse;
import com.example.eumbev2.entity.classroom.ClassStatus;
import com.example.eumbev2.entity.classroom.MemberStatus;
import com.example.eumbev2.entity.classroom.Role;
import com.example.eumbev2.service.classroom.ClassroomService;
import com.example.eumbev2.service.member.MemberService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes")
public class ClassroomController {

    private final ClassroomService classroomService;
    private final MemberService memberService;

    public ClassroomController(ClassroomService classroomService, MemberService memberService) {
        this.classroomService = classroomService;
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassResponse create(@Valid @RequestBody ClassCreateRequest request) {
        return classroomService.create(request);
    }

    @GetMapping("/search")
    public PageResponse<ClassSummaryResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) ClassStatus status,
            @RequestParam(required = false, defaultValue = "RECENT") ClassSearchSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return classroomService.search(keyword, difficulty, status, sort, pageable(page, size));
    }

    @GetMapping("/my")
    public PageResponse<ClassSummaryResponse> myClasses(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) ClassStatus status,
            @RequestParam(required = false) MemberStatus membership,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return classroomService.myClasses(role, status, membership, pageable(page, size));
    }

    @PostMapping("/join-by-code")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse joinByCode(@RequestBody JoinByCodeRequest request) {
        return memberService.joinByCode(request);
    }

    @GetMapping("/{classId}")
    public ClassDetailResponse getDetail(@PathVariable Long classId) {
        return classroomService.getDetail(classId);
    }

    @PatchMapping("/{classId}")
    public ClassResponse update(@PathVariable Long classId, @Valid @RequestBody ClassUpdateRequest request) {
        return classroomService.update(classId, request);
    }

    @DeleteMapping("/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long classId) {
        classroomService.delete(classId);
    }

    private Pageable pageable(int page, int size) {
        return PageRequest.of(page, size);
    }
}
