package io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtype"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextAnswerSubmitRequest.class, name = QuestionType.Names.TEXT),
        @JsonSubTypes.Type(value = SingleChoiceAnswerSubmitRequest.class, name = QuestionType.Names.SINGLE_CHOICE),
        @JsonSubTypes.Type(value = MultiChoiceAnswerSubmitRequest.class, name = QuestionType.Names.MULTI_CHOICE),
})
@Schema(
        discriminatorMapping = {
                @DiscriminatorMapping(value = QuestionType.Names.TEXT, schema = TextAnswerSubmitRequest.class),
                @DiscriminatorMapping(value = QuestionType.Names.SINGLE_CHOICE, schema = SingleChoiceAnswerSubmitRequest.class),
                @DiscriminatorMapping(value = QuestionType.Names.MULTI_CHOICE, schema = MultiChoiceAnswerSubmitRequest.class),
        }
)
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
public abstract class AnswerSubmitRequest {

    @NotNull
    private final UUID answerId;

    @JsonIgnore
    public abstract QuestionType getQuestionType();

}
