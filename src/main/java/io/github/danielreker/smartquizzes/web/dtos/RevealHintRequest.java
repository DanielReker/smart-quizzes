package io.github.danielreker.smartquizzes.web.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.UUID;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RevealHintRequest {

    @NotNull
    UUID answerId;

}
