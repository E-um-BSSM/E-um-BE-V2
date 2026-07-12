package com.example.eumbev2.service.assignment;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.common.security.SecurityUtils;
import com.example.eumbev2.dto.assignment.*;
import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.entity.submission.Submission;
import com.example.eumbev2.entity.submission.SubmissionStatus;
import com.example.eumbev2.entity.user.User;
import com.example.eumbev2.repository.assignment.AssignmentRepository;
import com.example.eumbev2.repository.submission.SubmissionRepository;
import com.example.eumbev2.service.classroom.ClassroomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AssignmentService {

    private final ClassroomService classroomService;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentService(ClassroomService classroomService, AssignmentRepository assignmentRepository, SubmissionRepository submissionRepository) {
        this.classroomService = classroomService;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
    }

    public AssignmentResponse create(Long classId, AssignmentCreateRequest request) {
        Classroom classroom = classroomService.getOrThrow(classId);
        classroomService.requireMentor(classroom);

        Assignment assignment = Assignment.builder()
                .classroom(classroom)
                .title(request.title())
                .description(request.description())
                .dueDate(request.dueDate())
                .difficulty(request.difficulty())
                .build();
        assignmentRepository.save(assignment);
        return toResponse(assignment);
    }

    @Transactional(readOnly = true)
    public PageResponse<AssignmentResponse> list(Long classId, Pageable pageable) {
        Classroom classroom = classroomService.getOrThrow(classId);
        Page<Assignment> page = assignmentRepository.findByClassroomOrderByCreatedAtDesc(classroom, pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getOne(Long classId, Long assignmentId) {
        return toResponse(getOrThrow(classId, assignmentId));
    }

    public AssignmentResponse update(Long classId, Long assignmentId, AssignmentUpdateRequest request) {
        Assignment assignment = getOrThrow(classId, assignmentId);
        classroomService.requireMentor(assignment.getClassroom());

        if (request.title() != null) assignment.setTitle(request.title());
        if (request.description() != null) assignment.setDescription(request.description());
        if (request.dueDate() != null) assignment.setDueDate(request.dueDate());
        if (request.difficulty() != null) assignment.setDifficulty(request.difficulty());

        return toResponse(assignment);
    }

    public void delete(Long classId, Long assignmentId) {
        Assignment assignment = getOrThrow(classId, assignmentId);
        classroomService.requireMentor(assignment.getClassroom());
        submissionRepository.deleteByAssignment(assignment);
        assignmentRepository.delete(assignment);
    }

    public Assignment getOrThrow(Long classId, Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ApiException(ErrorCode.ASSIGNMENT_NOT_FOUND));
        if (!assignment.getClassroom().getId().equals(classId)) {
            throw new ApiException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }
        return assignment;
    }

    private AssignmentResponse toResponse(Assignment assignment) {
        long submissionCount = submissionRepository.countByAssignment(assignment);
        SubmissionStatus mySubmissionStatus = null;
        Long currentUserId = SecurityUtils.currentUserIdOrNull();
        if (currentUserId != null) {
            User user = SecurityUtils.getCurrentUser();
            mySubmissionStatus = submissionRepository.findByAssignmentAndUser(assignment, user)
                    .map(Submission::getStatus)
                    .orElse(null);
        }
        return AssignmentResponse.of(assignment, submissionCount, mySubmissionStatus);
    }
}
