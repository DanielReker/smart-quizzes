package io.github.danielreker.smartquizzes.services.question;

import io.github.danielreker.smartquizzes.dao.entities.questions.TextQuestionEntity;
import io.github.danielreker.smartquizzes.dao.repositories.answers.TextAnswerRepository;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.mappers.QuestionMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.web.dtos.questions.TextQuestionDto;
import io.github.danielreker.smartquizzes.web.dtos.stats.TextQuestionStatsDto;
import org.springframework.stereotype.Service;

@Service
public class TextQuestionService
        extends QuestionService<TextQuestionEntity, TextQuestionDto> {

    private final TextAnswerRepository textAnswerRepository;

    public TextQuestionService(
            HintMapper hintMapper,
            QuestionMapper questionMapper,
            TextAnswerRepository textAnswerRepository
    ) {
        super(hintMapper, questionMapper);
        this.textAnswerRepository = textAnswerRepository;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.TEXT;
    }

    @Override
    public TextQuestionEntity buildConcreteQuestion(TextQuestionDto questionDto) {
        return questionMapper.toEntity(questionDto);
    }

    @Override
    public TextQuestionStatsDto getQuestionStats(TextQuestionEntity question) {
        return TextQuestionStatsDto
                .builder()
                .questionId(question.getId())
                .answerCount(textAnswerRepository.countByQuestionId(question.getId()))
                .build();
    }

}
