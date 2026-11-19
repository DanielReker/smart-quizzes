package io.github.danielreker.smartquizzes.web.controllers;

import io.github.danielreker.smartquizzes.services.QuizService;
import io.github.danielreker.smartquizzes.web.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/quizzes")
class QuizController {

    private final QuizService quizService;


    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(
            @RequestBody @Valid QuizCreateRequest request
    ) {
        final QuizResponse response = quizService.createQuiz(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PatchMapping("/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(
            @PathVariable UUID quizId,
            @RequestBody @Valid QuizUpdateRequest request
    ) {
        final QuizResponse response = quizService
                .updateQuiz(quizId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuiz(
            @PathVariable UUID quizId
    ) {
        final QuizResponse response = quizService
                .getQuiz(quizId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @GetMapping("/{quizId}/short")
    public ResponseEntity<ShortQuizResponse> getQuizShort(
            @PathVariable UUID quizId
    ) {
        final ShortQuizResponse response = quizService
                .getQuizShort(quizId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PutMapping("/{quizId}/questions")
    public ResponseEntity<QuizResponse> upsertQuestions(
            @PathVariable UUID quizId,
            @RequestBody @Valid QuizQuestionsUpsertRequest request
    ) {
        final QuizResponse response = quizService
                .upsertQuestions(quizId, request);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{quizId}/requests/start")
    public ResponseEntity<QuizResponse> startQuiz(
            @PathVariable UUID quizId
    ) {
        final QuizResponse response = quizService.startQuiz(quizId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{quizId}/requests/finish")
    public ResponseEntity<QuizResponse> finishQuiz(
            @PathVariable UUID quizId
    ) {
        final QuizResponse response = quizService.finishQuiz(quizId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{quizId}/submissions")
    public ResponseEntity<SubmissionResponse> createSubmission(
            @PathVariable UUID quizId
    ) {
        final SubmissionResponse submissionResponse =
                quizService.createSubmission(quizId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(submissionResponse);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @GetMapping("/{quizId}/stats")
    public ResponseEntity<StatsResponse> getStats(
            @PathVariable UUID quizId
    ) {
        final StatsResponse response = quizService.getStats(quizId);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @PostMapping("/{quizId}/questions:ai-generate")
    public ResponseEntity<QuizResponse> aiGenerateQuestions(
            @PathVariable UUID quizId,
            @RequestBody @Valid AiGenerateQuestionsRequest request
    ) {
        final QuizResponse response = quizService
                .aiGenerateQuestions(quizId, request);

        return ResponseEntity
                .ok(response);
    }

    @Operation(security = { @SecurityRequirement(name = "bearer-key") })
    @GetMapping
    public ResponseEntity<QuizzesResponse> getUserQuizzes(
            @RequestParam UUID userId
    ) {
        final QuizzesResponse response = quizService
                .getUserQuizzes(userId);

        return ResponseEntity
                .ok(response);
    }

}
