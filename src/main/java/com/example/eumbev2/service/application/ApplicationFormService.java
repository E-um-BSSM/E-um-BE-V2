package com.example.eumbev2.service.application;

import com.example.eumbev2.dto.application.*;
import com.example.eumbev2.entity.application.ApplicationQuestion;
import com.example.eumbev2.entity.application.QuestionType;
import com.example.eumbev2.entity.classroom.Classroom;
import com.example.eumbev2.repository.application.ApplicationQuestionRepository;
import com.example.eumbev2.service.classroom.ClassroomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ApplicationFormService {

    private final ClassroomService classroomService;
    private final ApplicationQuestionRepository applicationQuestionRepository;

    public ApplicationFormService(ClassroomService classroomService, ApplicationQuestionRepository applicationQuestionRepository) {
        this.classroomService = classroomService;
        this.applicationQuestionRepository = applicationQuestionRepository;
    }

    @Transactional(readOnly = true)
    public ApplicationFormResponse getForm(Long classId) {
        Classroom classroom = classroomService.getOrThrow(classId);
        List<ApplicationQuestionResponse> questions = applicationQuestionRepository
                .findByClassroomOrderByOrderNoAsc(classroom).stream()
                .map(ApplicationQuestionResponse::from)
                .toList();
        return new ApplicationFormResponse(classId, questions);
    }

    public ApplicationFormResponse setForm(Long classId, ApplicationFormUpdateRequest request) {
        Classroom classroom = classroomService.getOrThrow(classId);
        classroomService.requireMentor(classroom);

        applicationQuestionRepository.deleteByClassroom(classroom);
        applicationQuestionRepository.flush();

        List<ApplicationQuestionInput> inputs = request.questions() == null ? List.of() : request.questions();
        List<ApplicationQuestion> questions = new ArrayList<>();
        int order = 1;
        for (ApplicationQuestionInput input : inputs) {
            Integer maxLength = input.maxLength();
            if (maxLength == null) {
                if (input.type() == QuestionType.SHORT_TEXT) maxLength = 200;
                else if (input.type() == QuestionType.LONG_TEXT) maxLength = 2000;
            }
            ApplicationQuestion question = ApplicationQuestion.builder()
                    .classroom(classroom)
                    .type(input.type())
                    .orderNo(order++)
                    .title(input.title())
                    .description(input.description())
                    .required(input.requiredOrDefault())
                    .maxLength(maxLength)
                    .build();
            if (input.type() == QuestionType.SINGLE_CHOICE && input.options() != null) {
                question.setOptions(new ArrayList<>(input.options()));
            }
            questions.add(question);
        }
        applicationQuestionRepository.saveAll(questions);

        List<ApplicationQuestionResponse> responses = questions.stream().map(ApplicationQuestionResponse::from).toList();
        return new ApplicationFormResponse(classId, responses);
    }
}
