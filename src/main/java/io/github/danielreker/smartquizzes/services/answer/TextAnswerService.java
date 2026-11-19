package io.github.danielreker.smartquizzes.services.answer;

import io.github.danielreker.smartquizzes.dao.entities.answers.TextAnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.TextQuestionEntity;
import io.github.danielreker.smartquizzes.mappers.AnswerMapper;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.model.exceptions.AnswerValidationException;
import io.github.danielreker.smartquizzes.model.scoring.EvaluationResult;
import io.github.danielreker.smartquizzes.web.dtos.answers.TextAnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.TextAnswerSubmitRequest;
import org.springframework.ai.chat.client.AdvisorParams;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class TextAnswerService
        extends AnswerService<TextQuestionEntity, TextAnswerEntity, TextAnswerResponse, TextAnswerSubmitRequest> {

    private static final String SYSTEM_PROMPT = """
            Ты - проверяющий преподаватель. Твоя задача - объективно оценить текстовый ответ тестируемого на вопрос и дать оценку и фидбек.
            В ответе обязательно верни:
            - score: балл от 0.0 до 1.0 включительно (1.0 - совершенно верный ответ, 0.0 - совершенно неверный ответ или бессмыслица).
            - feedback: краткий конструктивный комментарий с обоснованием оценки.
            """;

    private final ChatClient chatClient;

    public TextAnswerService(
            AnswerMapper answerMapper,
            HintMapper hintMapper,
            ChatClient.Builder chatClientBuilder
    ) {
        super(answerMapper, hintMapper);
        this.chatClient = chatClientBuilder
                .defaultSystem(SYSTEM_PROMPT)
                .build();
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.TEXT;
    }

    @Override
    public TextAnswerEntity createEmptyAnswer(TextQuestionEntity question) {
        final TextAnswerEntity answerEntity = new TextAnswerEntity();
        answerEntity.setQuestion(question);
        return answerEntity;
    }

    @Override
    protected void submitConcreteAnswer(
            TextAnswerEntity answerEntity,
            TextAnswerSubmitRequest answerSubmitRequest
    ) {
        final TextQuestionEntity questionEntity = answerEntity.getQuestion();

        if (questionEntity.getMaxLength() != null &&
                answerSubmitRequest.getValue().length() > questionEntity.getMaxLength()) {
            throw new AnswerValidationException(
                    "Answer to question with id " + questionEntity.getId() +
                            " must not be longer than " + questionEntity.getMaxLength() + " symbols"
            );
        }

        answerEntity.setValue(answerSubmitRequest.getValue());
    }

    @Override
    protected TextAnswerResponse buildConcreteAnswerResponse(
            TextAnswerEntity answerEntity,
            boolean revealCorrectAnswers
    ) {
        return TextAnswerResponse
                .builder()
                .value(answerEntity.getValue())
                .feedback(answerEntity.getFeedback())
                .sampleCorrectAnswer(revealCorrectAnswers ?
                        answerEntity
                                .getQuestion()
                                .getSampleCorrectAnswer() :
                        null)
                .build();
    }

    @Override
    protected EvaluationResult scoreConcreteAnswer(
            TextAnswerEntity answerEntity
    ) {
        final TextQuestionEntity questionEntity = answerEntity.getQuestion();

        final String answerValue = answerEntity.getValue();

        if (answerValue == null || answerValue.isBlank()) {
            return new EvaluationResult(BigDecimal.ZERO, "Ответ не был предоставлен");
        }

        final StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("Вопрос: ").append(questionEntity.getName()).append("\n");
        Optional.ofNullable(questionEntity.getDescription())
                .ifPresent(desc -> promptBuilder.append("Описание вопроса: ").append(desc).append("\n"));
        Optional.ofNullable(questionEntity.getSampleCorrectAnswer())
                .ifPresent(sample -> promptBuilder.append("Эталонный правильный ответ: ").append(sample).append("\n"));
        promptBuilder.append("Ответ тестируемого: ").append(answerValue);

        final TextAnswerAiEvaluation aiEvaluation = chatClient
                .prompt()
                .user(promptBuilder.toString())
                .advisors(AdvisorParams.ENABLE_NATIVE_STRUCTURED_OUTPUT)
                .call()
                .entity(TextAnswerAiEvaluation.class);

        final BigDecimal score = Optional.ofNullable(aiEvaluation)
                .map(TextAnswerAiEvaluation::score)
                .map(s -> s.max(BigDecimal.ZERO).min(BigDecimal.ONE))
                .orElse(BigDecimal.ZERO);

        final String feedback = Optional.ofNullable(aiEvaluation)
                .map(TextAnswerAiEvaluation::feedback)
                .orElse(null);

        return new EvaluationResult(score, feedback);
    }

    public record TextAnswerAiEvaluation(
            BigDecimal score,
            String feedback
    ) {
    }

}
