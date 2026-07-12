package com.example.eumbev2.dto.notice;

import jakarta.validation.constraints.NotBlank;

public record NoticeCreateRequest(@NotBlank String title, @NotBlank String content) {
}
