package com.example.eumbev2.service.submission;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.common.security.SecurityUtils;
import com.example.eumbev2.dto.submission.*;
import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.classroom.MemberStatus;
import com.example.eumbev2.entity.classroom.Role;
import com.example.eumbev2.entity.submission.Submission;
import com.example.eumbev2.entity.submission.SubmissionStatus;
import com.example.eumbev2.entity.user.User;
import com.example.eumbev2.repository.classroom.ClassroomMemberRepository;
import com.example.eumbev2.repository.submission.SubmissionRepository;
import com.example.eumbev2.service.assignment.AssignmentService;
import com.example.eumbev2.service.classroom.ClassroomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class SubmissionService {

    private final AssignmentService assignmentService;
    private final ClassroomService classroomService;
    private final SubmissionRepository submissionRepository;
    private final ClassroomMemberRepository classroomMemberRepository;

    public SubmissionService(
            AssignmentService assignmentService,
            ClassroomService classroomService,
            SubmissionRepository submissionRepository,
            ClassroomMemberRepository classroomMemberRepository
    ) {
        this.assignmentService = assignmentService;
        this.classroomService = classroomService;
        this.submissionRepository = submissionRepository;
        this.classroomMemberRepository = classroomMemberRepository;
    }

    public SubmissionResponse create(Long classId, Long assignmentId, SubmissionCreateRequest request) {
        Assignment assignment = assignmentService.getOrThrow(classId, assignmentId);
        User user = SecurityUtils.getCurrentUser();
        requireAcceptedMentee(assignment, user);

        if (submissionRepository.existsByAssignmentAndUser(assignment, user)) {
            throw new ApiException(ErrorCode.ALREADY_SUBMITTED);
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .user(user)
                .content(request.content())
                .fileUrl(request.fileUrl())
                .status(SubmissionStatus.SUBMITTED)
                .build();
        submissionRepository.save(submission);
        return SubmissionResponse.from(submission);
    }

    @Transactional(readOnly = true)
    public PageResponse<SubmissionResponse> list(Long classId, Long assignmentId, SubmissionStatus status, Pageable pageable) {
        Assignment assignment = assignmentService.getOrThrow(classId, assignmentId);
        classroomService.requireMentor(assignment.getClassroom());
        Page<Submission> page = (status != null)
                ? submissionRepository.findByAssignmentAndStatus(assignment, status, pageable)
                : submissionRepository.findByAssignment(assignment, pageable);
        return PageResponse.from(page, SubmissionResponse::from);
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getOne(Long classId, Long assignmentId, Long submissionId) {
        return SubmissionResponse.from(getOrThrow(classId, assignmentId, submissionId));
    }

    public void cancel(Long classId, Long assignmentId, Long submissionId) {
        Submission submission = getOrThrow(classId, assignmentId, submissionId);
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!submission.getUser().getId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.NOT_SUBMISSION_OWNER);
        }
        submissionRepository.delete(submission);
    }

    public SubmissionResponse feedback(Long classId, Long assignmentId, Long submissionId, SubmissionFeedbackRequest request) {
        Submission submission = getOrThrow(classId, assignmentId, submissionId);
        classroomService.requireMentor(submission.getAssignment().getClassroom());

        submission.setScore(request.score());
        submission.setFeedback(request.feedback());
        submission.setStatus(SubmissionStatus.GRADED);
        submission.setGradedAt(Instant.now());

        return SubmissionResponse.from(submission);
    }

    private Submission getOrThrow(Long classId, Long assignmentId, Long submissionId) {
        Assignment assignment = assignmentService.getOrThrow(classId, assignmentId);
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ApiException(ErrorCode.SUBMISSION_NOT_FOUND));
        if (!submission.getAssignment().getId().equals(assignment.getId())) {
            throw new ApiException(ErrorCode.SUBMISSION_NOT_FOUND);
        }
        return submission;
    }

    private void requireAcceptedMentee(Assignment assignment, User user) {
        boolean isAcceptedMentee = classroomMemberRepository
                .findByClassroomAndUser(assignment.getClassroom(), user)
                .filter(m -> m.getStatus() == MemberStatus.ACCEPTED && m.getRole() == Role.MENTEE)
                .isPresent();
        if (!isAcceptedMentee) {
            throw new ApiException(ErrorCode.NOT_CLASS_MENTEE);
        }
    }
}
