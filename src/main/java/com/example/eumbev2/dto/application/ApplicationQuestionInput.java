package com.example.eumbev2.dto.application;

import com.example.eumbev2.entity.application.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ApplicationQuestionInput(
        @NotNull QuestionType type,
        @NotBlank String title,
        String description,
        Boolean required,
        Integer maxLength,
        List<String> options
) {
    public boolean requiredOrDefault() {
        return required != null && required;
    }
}
