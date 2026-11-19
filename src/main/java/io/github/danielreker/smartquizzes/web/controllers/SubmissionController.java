package io.github.danielreker.smartquizzes.web.controllers;

import io.github.danielreker.smartquizzes.services.SubmissionService;
import io.github.danielreker.smartquizzes.web.dtos.AnswersSubmitRequest;
import io.github.danielreker.smartquizzes.web.dtos.RevealHintRequest;
import io.github.danielreker.smartquizzes.web.dtos.SubmissionResponse;
import io.github.danielreker.smartquizzes.web.dtos.questions.HintDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/submissions")
public class SubmissionController {

    private final SubmissionService submissionService;


    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @GetMapping("/{submissionId}")
    public ResponseEntity<SubmissionResponse> getSubmission(
            @PathVariable UUID submissionId
    ) {
        final SubmissionResponse response = submissionService
                .getSubmission(submissionId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{submissionId}/answers")
    public ResponseEntity<SubmissionResponse> submitAnswers(
            @PathVariable UUID submissionId,
            @RequestBody @Valid AnswersSubmitRequest request
    ) {
        final SubmissionResponse response = submissionService
                .submitAnswers(submissionId, request);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{submissionId}/finish")
    public ResponseEntity<SubmissionResponse> finishSubmission(
            @PathVariable UUID submissionId
    ) {
        final SubmissionResponse response = submissionService
                .finishSubmission(submissionId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{submissionId}/revealed-hints")
    public ResponseEntity<HintDto> revealHint(
            @PathVariable UUID submissionId,
            @RequestBody @Valid RevealHintRequest request
    ) {
        final HintDto response = submissionService
                .revealHint(submissionId, request);

        return ResponseEntity
                .ok(response);
    }

}
