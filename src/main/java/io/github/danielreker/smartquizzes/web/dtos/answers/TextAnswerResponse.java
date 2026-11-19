package io.github.danielreker.smartquizzes.web.dtos.answers;

import io.github.danielreker.smartquizzes.model.QuestionType;
import jakarta.annotation.Nullable;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Getter
@SuperBuilder(toBuilder = true)
@Jacksonized
@EqualsAndHashCode(callSuper = true)
@ToString
public class TextAnswerResponse
        extends AnswerResponse<TextAnswerResponse> {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.TEXT;
    }

    @Nullable
    private final String value;

    @Nullable
    private final String feedback;

    @Nullable
    private final String sampleCorrectAnswer;

}
