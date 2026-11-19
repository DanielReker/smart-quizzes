package io.github.danielreker.smartquizzes.model.scoring;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record EvaluationResult(
        @NotNull BigDecimal normalizedScore,
        @Nullable String feedback
) {
}
