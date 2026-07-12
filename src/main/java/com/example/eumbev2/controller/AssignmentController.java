package com.example.eumbev2.controller;

import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.dto.assignment.*;
import com.example.eumbev2.service.assignment.AssignmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes/{classId}/assignments")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@PathVariable Long classId, @Valid @RequestBody AssignmentCreateRequest request) {
        return assignmentService.create(classId, request);
    }

    @GetMapping
    public PageResponse<AssignmentResponse> list(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return assignmentService.list(classId, PageRequest.of(page, size));
    }

    @GetMapping("/{assignmentId}")
    public AssignmentResponse getOne(@PathVariable Long classId, @PathVariable Long assignmentId) {
        return assignmentService.getOne(classId, assignmentId);
    }

    @PatchMapping("/{assignmentId}")
    public AssignmentResponse update(
            @PathVariable Long classId,
            @PathVariable Long assignmentId,
            @Valid @RequestBody AssignmentUpdateRequest request
    ) {
        return assignmentService.update(classId, assignmentId, request);
    }

    @DeleteMapping("/{assignmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long classId, @PathVariable Long assignmentId) {
        assignmentService.delete(classId, assignmentId);
    }
}
