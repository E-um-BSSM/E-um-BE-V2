package com.example.eumbev2.controller;

import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.dto.member.*;
import com.example.eumbev2.service.member.MemberService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes/{classId}")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public InviteCodeResponse createInvite(@PathVariable Long classId) {
        return memberService.createInvite(classId);
    }

    @GetMapping("/invite")
    public InviteCodeResponse getInvite(@PathVariable Long classId) {
        return memberService.getInvite(classId);
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse join(@PathVariable Long classId, @RequestBody(required = false) JoinRequest request) {
        return memberService.join(classId, request);
    }

    @DeleteMapping("/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelJoin(@PathVariable Long classId) {
        memberService.cancelJoin(classId);
    }

    @GetMapping("/waiting")
    public PageResponse<WaitingMemberResponse> waiting(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return memberService.waitingList(classId, PageRequest.of(page, size));
    }

    @GetMapping("/members")
    public PageResponse<MemberResponse> members(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return memberService.members(classId, PageRequest.of(page, size));
    }

    @PatchMapping("/members/{userId}/accept")
    public MemberResponse accept(@PathVariable Long classId, @PathVariable Long userId) {
        return memberService.accept(classId, userId);
    }

    @DeleteMapping("/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable Long classId, @PathVariable Long userId) {
        memberService.removeOrReject(classId, userId);
    }
}
