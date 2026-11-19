package io.github.danielreker.smartquizzes.services.answer;

import io.github.danielreker.smartquizzes.dao.entities.BaseEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.MultiChoiceAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.ChoiceEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.MultiChoiceQuestionEntity;
import io.github.danielreker.smartquizzes.mappers.AnswerMapper;
import io.github.danielreker.smartquizzes.mappers.ChoiceMapper;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.model.scoring.EvaluationResult;
import io.github.danielreker.smartquizzes.web.dtos.answers.MultiChoiceAnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.MultiChoiceAnswerSubmitRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MultiChoiceAnswerService
        extends AnswerService<
        MultiChoiceQuestionEntity,
        MultiChoiceAnswerEntity,
        MultiChoiceAnswerResponse,
        MultiChoiceAnswerSubmitRequest
        > {

    private final ChoiceMapper choiceMapper;


    public MultiChoiceAnswerService(
            AnswerMapper answerMapper,
            HintMapper hintMapper,
            ChoiceMapper choiceMapper
    ) {
        super(answerMapper, hintMapper);
        this.choiceMapper = choiceMapper;
    }


    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTI_CHOICE;
    }

    @Override
    public MultiChoiceAnswerEntity createEmptyAnswer(
            MultiChoiceQuestionEntity question
    ) {
        final MultiChoiceAnswerEntity answerEntity = new MultiChoiceAnswerEntity();
        answerEntity.setQuestion(question);
        answerEntity.setSelectedChoices(new ArrayList<>());
        return answerEntity;
    }

    @Override
    protected void submitConcreteAnswer(
            MultiChoiceAnswerEntity answerEntity,
            MultiChoiceAnswerSubmitRequest answerSubmitRequest
    ) {
        final List<ChoiceEntity> choices = answerSubmitRequest
                .getSelectedChoiceIds()
                .stream()
                .map(selectedChoiceId -> answerEntity
                        .getQuestion()
                        .getPossibleChoices()
                        .stream()
                        .filter(item -> Objects
                                .equals(item.getId(), selectedChoiceId))
                        .findFirst()
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Couldn't find choice with id " + selectedChoiceId +
                                        " in answer with id " + answerEntity.getId()
                        )))
                .toList();
        answerEntity.getSelectedChoices().clear();
        answerEntity.getSelectedChoices().addAll(choices);
    }

    @Override
    protected MultiChoiceAnswerResponse buildConcreteAnswerResponse(
            MultiChoiceAnswerEntity answerEntity,
            boolean revealCorrectAnswers
    ) {
        return MultiChoiceAnswerResponse
                .builder()
                .possibleChoices(answerEntity
                        .getQuestion()
                        .getPossibleChoices()
                        .stream()
                        .map(choiceMapper::toDto)
                        .map(choice -> revealCorrectAnswers ?
                                choice :
                                choice.toBuilder().isCorrect(null).build())
                        .toList())
                .selectedChoiceIds(Optional
                        .of(answerEntity)
                        .map(MultiChoiceAnswerEntity::getSelectedChoices)
                        .map(choices -> choices
                                .stream()
                                .map(BaseEntity::getId)
                                .toList())
                        .orElse(null))
                .build();
    }

    @Override
    protected EvaluationResult scoreConcreteAnswer(
            MultiChoiceAnswerEntity answerEntity
    ) {
        final List<ChoiceEntity> possibleChoices = answerEntity
                .getQuestion()
                .getPossibleChoices();

        if (possibleChoices.isEmpty()) {
            return new EvaluationResult(BigDecimal.ZERO, null);
        }

        final Set<UUID> selectedChoiceIds = Optional.ofNullable(answerEntity.getSelectedChoices())
                .orElseGet(List::of)
                .stream()
                .map(ChoiceEntity::getId)
                .collect(Collectors.toSet());

        final long correctDecisionsCount = possibleChoices
                .stream()
                .filter(choice -> {
                    final boolean isSelected = selectedChoiceIds.contains(choice.getId());
                    final boolean isCorrect = Boolean.TRUE.equals(choice.getIsCorrect());
                    return (isSelected && isCorrect) || (!isSelected && !isCorrect);
                })
                .count();

        final BigDecimal normalizedScore = BigDecimal.valueOf(correctDecisionsCount)
                .divide(BigDecimal.valueOf(possibleChoices.size()), 6, RoundingMode.HALF_UP);

        return new EvaluationResult(normalizedScore, null);
    }

}
