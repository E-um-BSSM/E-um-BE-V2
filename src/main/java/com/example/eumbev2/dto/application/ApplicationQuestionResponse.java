package com.example.eumbev2.dto.application;

import com.example.eumbev2.entity.application.ApplicationQuestion;
import com.example.eumbev2.entity.application.QuestionType;

import java.util.List;

public record ApplicationQuestionResponse(
        Long id,
        QuestionType type,
        Integer order,
        String title,
        String description,
        boolean required,
        Integer maxLength,
        List<String> options
) {
    public static ApplicationQuestionResponse from(ApplicationQuestion q) {
        return new ApplicationQuestionResponse(
                q.getId(), q.getType(), q.getOrderNo(), q.getTitle(), q.getDescription(),
                q.isRequired(), q.getMaxLength(), q.getOptions()
        );
    }
}
