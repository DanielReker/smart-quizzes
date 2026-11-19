package io.github.danielreker.smartquizzes.web.dtos.answers;

import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.web.dtos.questions.ChoiceDto;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.UUID;

@Getter
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@ToString
public class SingleChoiceAnswerResponse
        extends AnswerResponse<SingleChoiceAnswerResponse> {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.SINGLE_CHOICE;
    }

    @NotNull
    private final List<@NotNull @Valid ChoiceDto> possibleChoices;

    @Nullable
    private final UUID selectedChoiceId;

}
