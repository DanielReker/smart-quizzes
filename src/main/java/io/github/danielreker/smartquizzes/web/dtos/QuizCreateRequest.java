package io.github.danielreker.smartquizzes.web.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.time.DurationMin;

import java.time.Duration;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class QuizCreateRequest {

    @NotNull
    @NotBlank
    @Size(max = 255)
    String name;

    @Nullable
    String description;

    @Nullable
    @DurationMin(seconds = 1)
    Duration timeToComplete;

    @NotNull
    Boolean allowAnonymous;

    @Nullable
    @Min(1)
    Integer maxAttempts;

}
