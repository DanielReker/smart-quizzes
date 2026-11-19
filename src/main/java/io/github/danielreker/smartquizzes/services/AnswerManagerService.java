package io.github.danielreker.smartquizzes.services;

import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerStatus;
import io.github.danielreker.smartquizzes.dao.entities.questions.QuestionEntity;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.model.scoring.EvaluationResult;
import io.github.danielreker.smartquizzes.services.answer.AnswerService;
import io.github.danielreker.smartquizzes.web.dtos.answers.AnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.AnswerSubmitRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AnswerManagerService {

    private final Map<QuestionType, AnswerService<?, ?, ?, ?>> answerServices;


    public AnswerManagerService(List<AnswerService<?, ?, ?, ?>> answerServices) {
        this.answerServices = answerServices
                .stream()
                .collect(Collectors.toMap(
                        AnswerService::getQuestionType,
                        Function.identity(),
                        (answerService, _) -> {
                            throw new IllegalStateException("Found duplicated AnswerService for type: " +
                                    answerService.getQuestionType());
                        }
                ));

        for (QuestionType questionType : QuestionType.values()) {
            if (!this.answerServices.containsKey(questionType)) {
                throw new IllegalArgumentException("Couldn't find AnswerService for type: " + questionType);
            }
        }
    }


    public AnswerEntity<?> createEmptyAnswer(QuestionEntity question) {
        final var answerService =
                getAnswerService(question.getQuestionType());

        return answerService.createEmptyAnswer(question);
    }

    public void submitAnswer(
            AnswerEntity<?> answerEntity,
            AnswerSubmitRequest answerSubmitRequest
    ) {
        final var answerService =
                getAnswerService(answerEntity.getQuestionType());

        answerService.submitAnswer(answerEntity, answerSubmitRequest);
    }

    public AnswerResponse<?> buildAnswerResponse(
            AnswerEntity<?> answerEntity,
            AnswerStatus answerStatus,
            boolean revealCorrectAnswers
    ) {
        final var answerService =
                getAnswerService(answerEntity.getQuestionType());

        return answerService.buildAnswerResponse(
                answerEntity, answerStatus, revealCorrectAnswers
        );
    }

    public EvaluationResult scoreAnswer(
            QuestionEntity question,
            AnswerEntity<?> answerEntity
    ) {
        final var answerService =
                getAnswerService(question.getQuestionType());

        return answerService.scoreAnswer(answerEntity);
    }


    // Safe if service type is overridden correctly
    @SuppressWarnings("unchecked")
    private AnswerService<QuestionEntity, AnswerEntity<?>, ? extends AnswerResponse<?>, AnswerSubmitRequest> getAnswerService(
            QuestionType type
    ) {
        return (AnswerService<QuestionEntity, AnswerEntity<?>, ? extends AnswerResponse<?>, AnswerSubmitRequest>)
                this.answerServices.get(type);
    }

}
