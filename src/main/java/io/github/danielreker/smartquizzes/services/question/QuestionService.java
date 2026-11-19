package io.github.danielreker.smartquizzes.services.question;

import io.github.danielreker.smartquizzes.dao.entities.questions.HintEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.QuestionEntity;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.mappers.QuestionMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.web.dtos.questions.QuestionDto;
import io.github.danielreker.smartquizzes.web.dtos.stats.QuestionStatsDto;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public abstract class QuestionService<
        Q_ENTITY extends QuestionEntity,
        Q_DTO extends QuestionDto
        > {

    private final HintMapper hintMapper;

    protected final QuestionMapper questionMapper;

    public abstract QuestionType getQuestionType();

    public QuestionEntity buildQuestion(Q_DTO questionDto) {
        final QuestionEntity questionEntity = buildConcreteQuestion(questionDto);

        questionEntity.setHints(questionDto
                .getHints()
                .stream()
                .map(hintMapper::toEntity)
                .peek(hintEntity -> hintEntity
                        .setQuestion(questionEntity))
                .toList());

        validateMinimumQuestionScore(questionEntity);
        validateHintsOrder(questionEntity);

        return questionEntity;
    }

    private void validateMinimumQuestionScore(QuestionEntity questionEntity) {
        final BigDecimal hintsTotalScorePenalty = questionEntity
                .getHints()
                .stream()
                .map(HintEntity::getScorePenalty)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        final BigDecimal minPossibleScore = questionEntity.getMaxScore()
                .subtract(hintsTotalScorePenalty);

        if (minPossibleScore.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Minimum possible question score (after revealing all hints) must be >= 0"
            );
        }
    }

    private void validateHintsOrder(QuestionEntity questionEntity) {
        final List<Integer> hintOrders = questionEntity
                .getHints()
                .stream()
                .map(HintEntity::getSequentialOrder)
                .sorted(Comparator.naturalOrder())
                .toList();

        final boolean isOrderValid = IntStream
                .range(0, hintOrders.size())
                .allMatch(i -> Objects.equals(hintOrders.get(i), i));

        if (!isOrderValid) {
            throw new IllegalArgumentException(
                    "Hint orders should start from 0 and have +1 step, like: 0, 1, 2, ..."
            );
        }
    }

    protected abstract QuestionEntity buildConcreteQuestion(Q_DTO questionDto);

    public abstract QuestionStatsDto getQuestionStats(Q_ENTITY question);

}
