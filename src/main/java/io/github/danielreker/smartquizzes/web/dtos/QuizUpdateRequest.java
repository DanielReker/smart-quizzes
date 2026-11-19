package io.github.danielreker.smartquizzes.web.dtos;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.hibernate.validator.constraints.time.DurationMin;
import org.openapitools.jackson.nullable.JsonNullable;

import java.time.Duration;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class QuizUpdateRequest {

    @NotNull
    @NotBlank
    @Size(max = 255)
    @Builder.Default
    JsonNullable<String> name = JsonNullable.undefined();

    @Nullable
    @Builder.Default
    JsonNullable<String> description = JsonNullable.undefined();

    @Nullable
    @DurationMin(seconds = 1)
    @Builder.Default
    JsonNullable<Duration> timeToComplete = JsonNullable.undefined();

    @NotNull
    @Builder.Default
    JsonNullable<Boolean> allowAnonymous = JsonNullable.undefined();

    @Nullable
    @Min(1)
    @Builder.Default
    JsonNullable<Integer> maxAttempts = JsonNullable.undefined();

}
