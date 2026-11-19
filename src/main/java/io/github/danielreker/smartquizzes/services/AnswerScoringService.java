package io.github.danielreker.smartquizzes.services;

import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.TextAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.HintEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.QuestionEntity;
import io.github.danielreker.smartquizzes.dao.entities.submission.SubmissionEntity;
import io.github.danielreker.smartquizzes.dao.repositories.answers.AnswerRepository;
import io.github.danielreker.smartquizzes.model.scoring.EvaluationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnswerScoringService {

    private final AnswerRepository answerRepository;

    private final AnswerManagerService answerManagerService;


    @Transactional
    public void scoreAnswer(UUID answerId) {
        final AnswerEntity<?> answer = answerRepository
                .findById(answerId)
                .orElse(null);

        if (answer == null || answer.getScoredAt() != null) {
            return;
        }

        final QuestionEntity question = answer.getQuestion();
        final SubmissionEntity submission = answer.getSubmission();

        final BigDecimal hintPenalties = submission
                .getRevealedHints()
                .stream()
                .filter(hint -> Objects.equals(hint.getQuestion().getId(), question.getId()))
                .map(HintEntity::getScorePenalty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal effectiveMaxScore = question
                .getMaxScore()
                .subtract(hintPenalties)
                .max(BigDecimal.ZERO);

        final EvaluationResult evaluationResult =
                answerManagerService.scoreAnswer(question, answer);

        final BigDecimal normalizedScore = evaluationResult
                .normalizedScore()
                .max(BigDecimal.ZERO)
                .min(BigDecimal.ONE);

        final BigDecimal finalScore = normalizedScore
                .multiply(effectiveMaxScore)
                .setScale(3, RoundingMode.HALF_UP);

        answer.setScore(finalScore);
        answer.setScoredAt(Instant.now());

        if (answer instanceof TextAnswerEntity textAnswer) {
            textAnswer.setFeedback(evaluationResult.feedback());
        }

        answerRepository.save(answer);
    }

}
