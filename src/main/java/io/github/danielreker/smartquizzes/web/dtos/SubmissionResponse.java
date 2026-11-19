package io.github.danielreker.smartquizzes.web.dtos;

import io.github.danielreker.smartquizzes.dao.entities.submission.SubmissionStatus;
import io.github.danielreker.smartquizzes.web.dtos.answers.AnswerResponse;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class SubmissionResponse {

    @NotNull
    UUID id;

    @NotNull
    UUID quizId;

    @NotNull
    Instant startedAt;

    @Nullable
    Instant manuallyFinishedAt;

    @NotNull
    SubmissionStatus status;

    @Nullable
    BigDecimal score;

    @NotNull
    List<@NotNull @Valid AnswerResponse<?>> answers;

}
