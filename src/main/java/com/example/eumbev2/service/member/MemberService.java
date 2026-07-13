package com.example.eumbev2.service.member;

import com.example.eumbev2.common.exception.ApiException;
import com.example.eumbev2.common.exception.ErrorCode;
import com.example.eumbev2.common.response.PageResponse;
import com.example.eumbev2.common.security.SecurityUtils;
import com.example.eumbev2.common.util.CodeGenerator;
import com.example.eumbev2.dto.application.ApplicationAnswerDto;
import com.example.eumbev2.dto.member.*;
import com.example.eumbev2.entity.application.ApplicationAnswer;
import com.example.eumbev2.entity.application.ApplicationQuestion;
import com.example.eumbev2.entity.classroom.*;
import com.example.eumbev2.entity.user.User;
import com.example.eumbev2.repository.application.ApplicationAnswerRepository;
import com.example.eumbev2.repository.application.ApplicationQuestionRepository;
import com.example.eumbev2.repository.classroom.ClassroomMemberRepository;
import com.example.eumbev2.repository.classroom.ClassroomRepository;
import com.example.eumbev2.repository.user.UserRepository;
import com.example.eumbev2.service.classroom.ClassroomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class MemberService {

    private final ClassroomService classroomService;
    private final ClassroomRepository classroomRepository;
    private final ClassroomMemberRepository classroomMemberRepository;
    private final ApplicationQuestionRepository applicationQuestionRepository;
    private final ApplicationAnswerRepository applicationAnswerRepository;
    private final UserRepository userRepository;

    public MemberService(
            ClassroomService classroomService,
            ClassroomRepository classroomRepository,
            ClassroomMemberRepository classroomMemberRepository,
            ApplicationQuestionRepository applicationQuestionRepository,
            ApplicationAnswerRepository applicationAnswerRepository,
            UserRepository userRepository
    ) {
        this.classroomService = classroomService;
        this.classroomRepository = classroomRepository;
        this.classroomMemberRepository = classroomMemberRepository;
        this.applicationQuestionRepository = applicationQuestionRepository;
        this.applicationAnswerRepository = applicationAnswerRepository;
        this.userRepository = userRepository;
    }

    public InviteCodeResponse createInvite(Long classId) {
        Classroom classroom = classroomService.getOrThrow(classId);
        classroomService.requireMentor(classroom);
        classroom.setClassroomCode(CodeGenerator.inviteCode(8));
        classroom.setClassroomCodeExpiresAt(null);
        return InviteCodeResponse.from(classroom);
    }

    @Transactional(readOnly = true)
    public InviteCodeResponse getInvite(Long classId) {
        Classroom classroom = classroomService.getOrThrow(classId);
        classroomService.requireMentor(classroom);
        if (!classroom.isInviteCodeValid()) {
            throw new ApiException(ErrorCode.NO_INVITE_CODE);
        }
        return InviteCodeResponse.from(classroom);
    }

    public MemberResponse join(Long classId, JoinRequest request) {
        Classroom classroom = classroomService.getOrThrow(classId);
        User user = SecurityUtils.getCurrentUser();

        classroomMemberRepository.findByClassroomAndUser(classroom, user).ifPresent(existing -> {
            if (existing.getStatus() != MemberStatus.REJECTED) {
                throw new ApiException(ErrorCode.ALREADY_MEMBER);
            }
            applicationAnswerRepository.deleteByClassroomAndUser(classroom, user);
            classroomMemberRepository.delete(existing);
        });

        if (classroom.getAccessScope() != AccessScope.PUBLIC) {
            String inviteCode = request == null ? null : request.inviteCode();
            if (inviteCode == null || inviteCode.isBlank()) {
                throw new ApiException(ErrorCode.INVITE_CODE_REQUIRED);
            }
            if (!classroom.isInviteCodeValid() || !inviteCode.equals(classroom.getClassroomCode())) {
                throw new ApiException(ErrorCode.INVALID_INVITE_CODE);
            }
        }

        ClassroomMember membership = ClassroomMember.builder()
                .classroom(classroom)
                .user(user)
                .role(Role.MENTEE)
                .status(MemberStatus.WAITING)
                .message(request == null ? null : request.message())
                .appliedAt(Instant.now())
                .build();
        classroomMemberRepository.save(membership);

        if (request != null && request.answers() != null) {
            saveAnswers(classroom, user, request.answers());
        }

        return MemberResponse.from(membership);
    }

    /**
     * 초대 코드만으로 클래스를 찾아 가입 신청한다(멘티). 코드에 해당하는 클래스를 조회한 뒤
     * 기존 join 로직을 재사용한다. classId를 모르는 초대 링크/코드 입력 진입점을 위한 것.
     */
    public MemberResponse joinByCode(JoinByCodeRequest request) {
        String code = request == null ? null : request.inviteCode();
        if (code == null || code.isBlank()) {
            throw new ApiException(ErrorCode.INVITE_CODE_REQUIRED);
        }
        Classroom classroom = classroomRepository.findByClassroomCode(code)
                .filter(Classroom::isInviteCodeValid)
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_INVITE_CODE));
        return join(classroom.getId(), new JoinRequest(code, request.message(), request.answers()));
    }

    public void cancelJoin(Long classId) {
        Classroom classroom = classroomService.getOrThrow(classId);
        User user = SecurityUtils.getCurrentUser();
        ClassroomMember membership = classroomMemberRepository.findByClassroomAndUser(classroom, user)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        if (membership.getStatus() != MemberStatus.WAITING) {
            throw new ApiException(ErrorCode.NOT_WAITING_MEMBER);
        }
        applicationAnswerRepository.deleteByClassroomAndUser(classroom, user);
        classroomMemberRepository.delete(membership);
    }

    @Transactional(readOnly = true)
    public PageResponse<WaitingMemberResponse> waitingList(Long classId, Pageable pageable) {
        Classroom classroom = classroomService.getOrThrow(classId);
        classroomService.requireMentor(classroom);
        Page<ClassroomMember> page = classroomMemberRepository.findByClassroomAndStatus(classroom, MemberStatus.WAITING, pageable);
        return PageResponse.from(page, m -> WaitingMemberResponse.of(m, loadAnswers(classroom, m.getUser())));
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> members(Long classId, Pageable pageable) {
        Classroom classroom = classroomService.getOrThrow(classId);
        Page<ClassroomMember> page = classroomMemberRepository.findByClassroomAndStatus(classroom, MemberStatus.ACCEPTED, pageable);
        return PageResponse.from(page, MemberResponse::from);
    }

    public MemberResponse accept(Long classId, Long userId) {
        ClassroomMember membership = findMembership(classId, userId);
        classroomService.requireMentor(membership.getClassroom());
        if (membership.getStatus() != MemberStatus.WAITING) {
            throw new ApiException(ErrorCode.NOT_WAITING_MEMBER);
        }
        membership.setStatus(MemberStatus.ACCEPTED);
        membership.setJoinedAt(Instant.now());
        return MemberResponse.from(membership);
    }

    public void removeOrReject(Long classId, Long userId) {
        ClassroomMember membership = findMembership(classId, userId);
        Classroom classroom = membership.getClassroom();
        classroomService.requireMentor(classroom);
        if (classroom.getMentor().getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "클래스 멘토는 제외할 수 없습니다.");
        }
        applicationAnswerRepository.deleteByClassroomAndUser(classroom, membership.getUser());
        classroomMemberRepository.delete(membership);
    }

    private ClassroomMember findMembership(Long classId, Long userId) {
        Classroom classroom = classroomService.getOrThrow(classId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
        return classroomMemberRepository.findByClassroomAndUser(classroom, user)
                .orElseThrow(() -> new ApiException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private void saveAnswers(Classroom classroom, User user, List<ApplicationAnswerDto> answers) {
        applicationAnswerRepository.deleteByClassroomAndUser(classroom, user);
        for (ApplicationAnswerDto dto : answers) {
            ApplicationQuestion question = applicationQuestionRepository.findById(dto.questionId())
                    .filter(q -> q.getClassroom().getId().equals(classroom.getId()))
                    .orElseThrow(() -> new ApiException(ErrorCode.APPLICATION_QUESTION_NOT_FOUND));
            applicationAnswerRepository.save(ApplicationAnswer.builder()
                    .classroom(classroom)
                    .user(user)
                    .question(question)
                    .value(dto.value())
                    .build());
        }
    }

    private List<ApplicationAnswerDto> loadAnswers(Classroom classroom, User user) {
        return applicationAnswerRepository.findByClassroomAndUser(classroom, user).stream()
                .map(a -> new ApplicationAnswerDto(a.getQuestion().getId(), a.getValue()))
                .toList();
    }
}
