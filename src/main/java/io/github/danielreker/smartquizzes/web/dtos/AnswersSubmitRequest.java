package io.github.danielreker.smartquizzes.web.dtos;

import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.AnswerSubmitRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class AnswersSubmitRequest {

    @NotNull
    List<@NotNull @Valid AnswerSubmitRequest> answers;

}
