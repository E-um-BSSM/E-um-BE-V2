package com.example.eumbev2.controller;

import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.dto.submission.*;
import com.example.eumbev2.entity.submission.SubmissionStatus;
import com.example.eumbev2.service.submission.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes/{classId}/assignments/{assignmentId}/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionResponse create(
            @PathVariable Long classId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionCreateRequest request
    ) {
        return submissionService.create(classId, assignmentId, request);
    }

    @GetMapping
    public PageResponse<SubmissionResponse> list(
            @PathVariable Long classId,
            @PathVariable Long assignmentId,
            @RequestParam(required = false) SubmissionStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return submissionService.list(classId, assignmentId, status, PageRequest.of(page, size));
    }

    @GetMapping("/{submissionId}")
    public SubmissionResponse getOne(@PathVariable Long classId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {
        return submissionService.getOne(classId, assignmentId, submissionId);
    }

    @DeleteMapping("/{submissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable Long classId, @PathVariable Long assignmentId, @PathVariable Long submissionId) {
        submissionService.cancel(classId, assignmentId, submissionId);
    }

    @PatchMapping("/{submissionId}/feedback")
    public SubmissionResponse feedback(
            @PathVariable Long classId,
            @PathVariable Long assignmentId,
            @PathVariable Long submissionId,
            @Valid @RequestBody SubmissionFeedbackRequest request
    ) {
        return submissionService.feedback(classId, assignmentId, submissionId, request);
    }
}
