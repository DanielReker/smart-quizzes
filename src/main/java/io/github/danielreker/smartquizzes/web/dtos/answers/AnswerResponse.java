package io.github.danielreker.smartquizzes.web.dtos.answers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerStatus;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.web.dtos.questions.HintDto;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "dtype"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextAnswerResponse.class, name = QuestionType.Names.TEXT),
        @JsonSubTypes.Type(value = SingleChoiceAnswerResponse.class, name = QuestionType.Names.SINGLE_CHOICE),
        @JsonSubTypes.Type(value = MultiChoiceAnswerResponse.class, name = QuestionType.Names.MULTI_CHOICE),
})
@Schema(
        discriminatorMapping = {
                @DiscriminatorMapping(value = QuestionType.Names.TEXT, schema = TextAnswerResponse.class),
                @DiscriminatorMapping(value = QuestionType.Names.SINGLE_CHOICE, schema = SingleChoiceAnswerResponse.class),
                @DiscriminatorMapping(value = QuestionType.Names.MULTI_CHOICE, schema = MultiChoiceAnswerResponse.class),
        }
)
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = false)
public abstract class AnswerResponse
        <SELF extends AnswerResponse<SELF>> {

    @NotNull
    private final UUID id;

    @NotNull
    private final String questionName;

    @Nullable
    private final String questionDescription;

    @Nullable
    private final BigDecimal score;

    @Nullable
    private final Instant answeredAt;

    @Nullable
    private final Instant scoredAt;

    @Nullable
    private final AnswerStatus status;

    @NotNull
    List<@NotNull @Valid HintDto> revealedHints;

    @NotNull
    private final List<@NotNull BigDecimal> nextHintsPenalties;

    @JsonIgnore
    public abstract QuestionType getQuestionType();

    public abstract AnswerResponseBuilder<SELF, ? extends SELF, ?> toBuilder();

}
