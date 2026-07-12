package com.example.eumbev2.controller;

import com.example.eumbev2.dto.application.ApplicationFormResponse;
import com.example.eumbev2.dto.application.ApplicationFormUpdateRequest;
import com.example.eumbev2.service.application.ApplicationFormService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes/{classId}/application-form")
public class ApplicationFormController {

    private final ApplicationFormService applicationFormService;

    public ApplicationFormController(ApplicationFormService applicationFormService) {
        this.applicationFormService = applicationFormService;
    }

    @GetMapping
    public ApplicationFormResponse getForm(@PathVariable Long classId) {
        return applicationFormService.getForm(classId);
    }

    @PutMapping
    public ApplicationFormResponse setForm(@PathVariable Long classId, @Valid @RequestBody ApplicationFormUpdateRequest request) {
        return applicationFormService.setForm(classId, request);
    }
}
