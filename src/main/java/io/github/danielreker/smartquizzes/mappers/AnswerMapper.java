package io.github.danielreker.smartquizzes.mappers;

import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.MultiChoiceAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.SingleChoiceAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.TextAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.ChoiceEntity;
import io.github.danielreker.smartquizzes.web.dtos.answers.AnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answers.MultiChoiceAnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answers.SingleChoiceAnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answers.TextAnswerResponse;
import org.mapstruct.*;

import java.util.Optional;
import java.util.UUID;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        subclassExhaustiveStrategy = SubclassExhaustiveStrategy.RUNTIME_EXCEPTION
)
public interface AnswerMapper {

    @Mapping(target = "questionName", ignore = true)
    @Mapping(target = "questionDescription", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "revealedHints", ignore = true)
    @Mapping(target = "nextHintsPenalties", ignore = true)
    void fillResponseBuilder(
            @MappingTarget AnswerResponse.AnswerResponseBuilder<?, ?, ?> builder,
            AnswerEntity<?> entity
    );


    @SubclassMapping(source = SingleChoiceAnswerEntity.class, target = SingleChoiceAnswerResponse.class)
    @SubclassMapping(source = MultiChoiceAnswerEntity.class, target = MultiChoiceAnswerResponse.class)
    @SubclassMapping(source = TextAnswerEntity.class, target = TextAnswerResponse.class)
    AnswerResponse<?> toDto(AnswerEntity<?> entity);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "revealedHints", ignore = true)
    @Mapping(target = "questionName", source = "question.name")
    @Mapping(target = "questionDescription", source = "question.description")
    @Mapping(target = "sampleCorrectAnswer", source = "question.sampleCorrectAnswer")
    @Mapping(target = "nextHintsPenalties", ignore = true)
    TextAnswerResponse toDto(TextAnswerEntity entity);

    @Mapping(target = "selectedChoiceId", source = "selectedChoice.id")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "revealedHints", ignore = true)
    @Mapping(target = "questionName", source = "question.name")
    @Mapping(target = "questionDescription", source = "question.description")
    @Mapping(target = "possibleChoices", source = "question.possibleChoices")
    @Mapping(target = "nextHintsPenalties", ignore = true)
    SingleChoiceAnswerResponse toDto(SingleChoiceAnswerEntity entity);

    @Mapping(target = "selectedChoiceIds", source = "selectedChoices")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "revealedHints", ignore = true)
    @Mapping(target = "questionName", source = "question.name")
    @Mapping(target = "questionDescription", source = "question.description")
    @Mapping(target = "possibleChoices", source = "question.possibleChoices")
    @Mapping(target = "nextHintsPenalties", ignore = true)
    MultiChoiceAnswerResponse toDto(MultiChoiceAnswerEntity entity);

    default UUID choiceEntityToId(ChoiceEntity choiceEntity) {
        return Optional
                .ofNullable(choiceEntity)
                .map(ChoiceEntity::getId)
                .orElse(null);
    }

}
