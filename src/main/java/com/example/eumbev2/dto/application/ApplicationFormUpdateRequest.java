package com.example.eumbev2.dto.application;

import jakarta.validation.Valid;

import java.util.List;

public record ApplicationFormUpdateRequest(@Valid List<ApplicationQuestionInput> questions) {
}
