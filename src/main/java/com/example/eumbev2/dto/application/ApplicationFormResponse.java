package com.example.eumbev2.dto.application;

import java.util.List;

public record ApplicationFormResponse(Long classId, List<ApplicationQuestionResponse> questions) {
}
