package io.github.danielreker.smartquizzes.services.answer;

import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerStatus;
import io.github.danielreker.smartquizzes.dao.entities.questions.HintEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.QuestionEntity;
import io.github.danielreker.smartquizzes.mappers.AnswerMapper;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.model.scoring.EvaluationResult;
import io.github.danielreker.smartquizzes.web.dtos.answers.AnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.AnswerSubmitRequest;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class AnswerService<
        Q_ENTITY extends QuestionEntity,
        A_ENTITY extends AnswerEntity<?>,
        A_RESPONSE extends AnswerResponse<A_RESPONSE>,
        A_SR extends AnswerSubmitRequest
        > {

    private final AnswerMapper answerMapper;

    private final HintMapper hintMapper;


    public abstract QuestionType getQuestionType();

    public abstract A_ENTITY createEmptyAnswer(Q_ENTITY question);

    public void submitAnswer(
            A_ENTITY answerEntity,
            A_SR answerSubmitRequest
    ) {
        submitConcreteAnswer(answerEntity, answerSubmitRequest);
    }

    protected abstract void submitConcreteAnswer(
            A_ENTITY answerEntity,
            A_SR answerSubmitRequest
    );


    public A_RESPONSE buildAnswerResponse(
            A_ENTITY answerEntity,
            AnswerStatus answerStatus,
            boolean revealCorrectAnswers
    ) {
        final var builder = buildConcreteAnswerResponse(
                answerEntity, revealCorrectAnswers
        ).toBuilder();

        answerMapper.fillResponseBuilder(builder, answerEntity);

        final List<HintEntity> allHints = answerEntity
                .getQuestion()
                .getHints();

        final List<HintEntity> allRevealedHints = answerEntity
                .getSubmission()
                .getRevealedHints();

        return builder
                .questionName(answerEntity.getQuestion().getName())
                .questionDescription(answerEntity.getQuestion().getDescription())
                .status(answerStatus)
                .revealedHints(allHints
                        .stream()
                        .filter(allRevealedHints::contains)
                        .map(hintMapper::toDto)
                        .toList())
                .nextHintsPenalties(allHints
                        .stream()
                        .filter(hint ->
                                !allRevealedHints.contains(hint))
                        .map(HintEntity::getScorePenalty)
                        .toList())
                .build();
    }

    protected abstract A_RESPONSE buildConcreteAnswerResponse(
            A_ENTITY answerEntity,
            boolean revealCorrectAnswers
    );

    public EvaluationResult scoreAnswer(
            A_ENTITY answerEntity
    ) {
        return scoreConcreteAnswer(answerEntity);
    }

    protected abstract EvaluationResult scoreConcreteAnswer(
            A_ENTITY answerEntity
    );

}
