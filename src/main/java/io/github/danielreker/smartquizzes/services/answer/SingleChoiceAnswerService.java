package io.github.danielreker.smartquizzes.services.answer;

import io.github.danielreker.smartquizzes.dao.entities.BaseEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.SingleChoiceAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.ChoiceEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.SingleChoiceQuestionEntity;
import io.github.danielreker.smartquizzes.mappers.AnswerMapper;
import io.github.danielreker.smartquizzes.mappers.ChoiceMapper;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.model.scoring.EvaluationResult;
import io.github.danielreker.smartquizzes.web.dtos.answers.SingleChoiceAnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.SingleChoiceAnswerSubmitRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
public class SingleChoiceAnswerService
        extends AnswerService<
        SingleChoiceQuestionEntity,
        SingleChoiceAnswerEntity,
        SingleChoiceAnswerResponse,
        SingleChoiceAnswerSubmitRequest
        > {

    private final ChoiceMapper choiceMapper;


    public SingleChoiceAnswerService(
            AnswerMapper answerMapper,
            HintMapper hintMapper,
            ChoiceMapper choiceMapper
    ) {
        super(answerMapper, hintMapper);
        this.choiceMapper = choiceMapper;
    }


    @Override
    public QuestionType getQuestionType() {
        return QuestionType.SINGLE_CHOICE;
    }

    @Override
    public SingleChoiceAnswerEntity createEmptyAnswer(SingleChoiceQuestionEntity question) {
        final SingleChoiceAnswerEntity answerEntity = new SingleChoiceAnswerEntity();
        answerEntity.setQuestion(question);
        return answerEntity;
    }

    @Override
    protected void submitConcreteAnswer(
            SingleChoiceAnswerEntity answerEntity,
            SingleChoiceAnswerSubmitRequest answerSubmitRequest
    ) {
        final ChoiceEntity choice = answerEntity
                .getQuestion()
                .getPossibleChoices()
                .stream()
                .filter(item -> Objects.equals(item.getId(), answerSubmitRequest.getSelectedChoiceId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Could not find choice with id " + answerSubmitRequest.getSelectedChoiceId() +
                                " in answer with id " + answerEntity.getId()
                ));
        answerEntity.setSelectedChoice(choice);
    }

    @Override
    protected SingleChoiceAnswerResponse buildConcreteAnswerResponse(
            SingleChoiceAnswerEntity answerEntity,
            boolean revealCorrectAnswers
    ) {
        return SingleChoiceAnswerResponse
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
                .selectedChoiceId(Optional
                        .of(answerEntity)
                        .map(SingleChoiceAnswerEntity::getSelectedChoice)
                        .map(BaseEntity::getId)
                        .orElse(null))
                .build();
    }

    @Override
    protected EvaluationResult scoreConcreteAnswer(
            SingleChoiceAnswerEntity answerEntity
    ) {
        final ChoiceEntity selected = answerEntity.getSelectedChoice();
        if (selected == null) {
            return new EvaluationResult(BigDecimal.ZERO, null);
        }
        final boolean isCorrect = Boolean.TRUE.equals(selected.getIsCorrect());
        return new EvaluationResult(isCorrect ? BigDecimal.ONE : BigDecimal.ZERO, null);
    }

}
