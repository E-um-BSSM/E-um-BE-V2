package com.example.eumbev2.service.classroom;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.common.security.SecurityUtils;
import com.example.eumbev2.dto.classroom.*;
import com.example.eumbev2.entity.assignment.Assignment;
import com.example.eumbev2.entity.classroom.*;
import com.example.eumbev2.entity.user.User;
import com.example.eumbev2.repository.application.ApplicationAnswerRepository;
import com.example.eumbev2.repository.application.ApplicationQuestionRepository;
import com.example.eumbev2.repository.assignment.AssignmentRepository;
import com.example.eumbev2.repository.classroom.ClassroomMemberRepository;
import com.example.eumbev2.repository.classroom.ClassroomRepository;
import com.example.eumbev2.repository.notice.NoticeRepository;
import com.example.eumbev2.repository.submission.SubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final NoticeRepository noticeRepository;
    private final ApplicationQuestionRepository applicationQuestionRepository;
    private final ApplicationAnswerRepository applicationAnswerRepository;

    public ClassroomService(
            ClassroomRepository classroomRepository,
            ClassroomMemberRepository classroomMemberRepository,
            AssignmentRepository assignmentRepository,
            SubmissionRepository submissionRepository,
            NoticeRepository noticeRepository,
            ApplicationQuestionRepository applicationQuestionRepository,
            ApplicationAnswerRepository applicationAnswerRepository
    ) {
        this.classroomRepository = classroomRepository;
        this.classroomMemberRepository = classroomMemberRepository;
        this.assignmentRepository = assignmentRepository;
        this.submissionRepository = submissionRepository;
        this.noticeRepository = noticeRepository;
        this.applicationQuestionRepository = applicationQuestionRepository;
        this.applicationAnswerRepository = applicationAnswerRepository;
    }

    public ClassResponse create(ClassCreateRequest request) {
        User mentor = SecurityUtils.getCurrentUser();

        Classroom classroom = Classroom.builder()
                .title(request.title())
                .description(request.description())
                .difficulty(request.difficulty())
                .accessScope(request.accessScope())
                .status(ClassStatus.RECRUITING)
                .bannerImageUrl(request.bannerImageUrl())
                .mentorIntroduction(request.mentorIntroduction())
                .guide(request.guide())
                .mentor(mentor)
                .build();
        if (request.tags() != null) {
            classroom.setTags(new java.util.ArrayList<>(request.tags()));
        }
        classroomRepository.save(classroom);

        ClassroomMember mentorMembership = ClassroomMember.builder()
                .classroom(classroom)
                .user(mentor)
                .role(Role.MENTOR)
                .status(MemberStatus.ACCEPTED)
                .appliedAt(Instant.now())
                .joinedAt(Instant.now())
                .build();
        classroomMemberRepository.save(mentorMembership);

        return ClassResponse.of(classroom, 0);
    }

    @Transactional(readOnly = true)
    public PageResponse<ClassSummaryResponse> search(String keyword, Integer difficulty, ClassStatus status, ClassSearchSort sort, Pageable pageable) {
        Page<Classroom> page = (sort == ClassSearchSort.POPULAR)
                ? classroomRepository.searchOrderByPopularity(blankToNull(keyword), difficulty, status, pageable)
                : classroomRepository.search(blankToNull(keyword), difficulty, status, withCreatedAtDesc(pageable));
        return PageResponse.from(page, c -> ClassSummaryResponse.of(c, menteeCount(c)));
    }

    @Transactional(readOnly = true)
    public PageResponse<ClassSummaryResponse> myClasses(Role role, ClassStatus status, MemberStatus membership, Pageable pageable) {
        User user = SecurityUtils.getCurrentUser();
        Page<Classroom> page = classroomMemberRepository.findMyClasses(user, role, status, membership, pageable);
        return PageResponse.from(page, c -> ClassSummaryResponse.of(c, menteeCount(c)));
    }

    @Transactional(readOnly = true)
    public ClassDetailResponse getDetail(Long classId) {
        Classroom classroom = getOrThrow(classId);
        Role myRole = SecurityUtils.currentUserIdOrNull() == null
                ? null
                : classroomMemberRepository.findByClassroomAndUser(classroom, SecurityUtils.getCurrentUser())
                    .map(ClassroomMember::getRole)
                    .orElse(null);
        boolean isMentor = myRole == Role.MENTOR;
        String inviteCode = (isMentor && classroom.isInviteCodeValid()) ? classroom.getClassroomCode() : null;
        long assignmentCount = assignmentRepository.findByClassroom(classroom).size();
        return ClassDetailResponse.of(classroom, menteeCount(classroom), inviteCode, assignmentCount, myRole);
    }

    public ClassResponse update(Long classId, ClassUpdateRequest request) {
        Classroom classroom = getOrThrow(classId);
        requireMentor(classroom);

        if (request.title() != null) classroom.setTitle(request.title());
        if (request.description() != null) classroom.setDescription(request.description());
        if (request.difficulty() != null) classroom.setDifficulty(request.difficulty());
        if (request.tags() != null) classroom.setTags(new java.util.ArrayList<>(request.tags()));
        if (request.accessScope() != null) classroom.setAccessScope(request.accessScope());
        if (request.status() != null) classroom.setStatus(request.status());
        if (request.bannerImageUrl() != null) classroom.setBannerImageUrl(request.bannerImageUrl());
        if (request.mentorIntroduction() != null) classroom.setMentorIntroduction(request.mentorIntroduction());
        if (request.guide() != null) classroom.setGuide(request.guide());

        return ClassResponse.of(classroom, menteeCount(classroom));
    }

    public void delete(Long classId) {
        Classroom classroom = getOrThrow(classId);
        requireMentor(classroom);

        for (Assignment assignment : assignmentRepository.findByClassroom(classroom)) {
            submissionRepository.deleteByAssignment(assignment);
        }
        assignmentRepository.deleteAll(assignmentRepository.findByClassroom(classroom));
        noticeRepository.deleteByClassroom(classroom);
        applicationAnswerRepository.deleteByClassroom(classroom);
        applicationQuestionRepository.deleteByClassroom(classroom);
        classroomMemberRepository.deleteByClassroom(classroom);
        classroomRepository.delete(classroom);
    }

    // --- helpers shared with other classroom-related services ---

    public Classroom getOrThrow(Long classId) {
        return classroomRepository.findById(classId)
                .orElseThrow(() -> new ApiException(ErrorCode.CLASS_NOT_FOUND));
    }

    public void requireMentor(Classroom classroom) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!classroom.getMentor().getId().equals(currentUserId)) {
            throw new ApiException(ErrorCode.NOT_CLASS_MENTOR);
        }
    }

    public long menteeCount(Classroom classroom) {
        return classroomMemberRepository.countByClassroomAndStatusAndRole(classroom, MemberStatus.ACCEPTED, Role.MENTEE);
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    private Pageable withCreatedAtDesc(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
