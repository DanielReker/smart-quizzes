package io.github.danielreker.smartquizzes.web.dtos;

import io.github.danielreker.smartquizzes.dao.entities.QuizStatus;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
@Jacksonized
public record ShortQuizResponse(

    @NotNull
    UUID id,

    @NotNull
    Instant createdDate,

    @NotNull
    @NotBlank
    @Size(max = 255)
    String name,

    @Nullable
    String description,

    @NotNull
    QuizStatus status,

    @Nullable
    @DurationMin(seconds = 1)
    Duration timeToComplete,

    @NotNull
    Boolean allowAnonymous,

    @Nullable
    @Min(1)
    Integer maxAttempts

) {
}
