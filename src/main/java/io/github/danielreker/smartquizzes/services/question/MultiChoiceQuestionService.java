package io.github.danielreker.smartquizzes.services.question;

import io.github.danielreker.smartquizzes.dao.entities.questions.MultiChoiceQuestionEntity;
import io.github.danielreker.smartquizzes.dao.repositories.answers.MultiChoiceAnswerRepository;
import io.github.danielreker.smartquizzes.mappers.ChoiceMapper;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.mappers.QuestionMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.web.dtos.questions.MultiChoiceQuestionDto;
import io.github.danielreker.smartquizzes.web.dtos.stats.ChoiceStatsDto;
import io.github.danielreker.smartquizzes.web.dtos.stats.MultiChoiceQuestionStatsDto;
import org.springframework.stereotype.Service;

@Service
public class MultiChoiceQuestionService
        extends QuestionService<MultiChoiceQuestionEntity, MultiChoiceQuestionDto> {

    private final ChoiceMapper choiceMapper;

    private final MultiChoiceAnswerRepository multiChoiceAnswerRepository;

    public MultiChoiceQuestionService(
            HintMapper hintMapper,
            QuestionMapper questionMapper,
            ChoiceMapper choiceMapper,
            MultiChoiceAnswerRepository multiChoiceAnswerRepository
    ) {
        super(hintMapper, questionMapper);
        this.choiceMapper = choiceMapper;
        this.multiChoiceAnswerRepository = multiChoiceAnswerRepository;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.MULTI_CHOICE;
    }

    @Override
    public MultiChoiceQuestionEntity buildConcreteQuestion(MultiChoiceQuestionDto questionDto) {
        final MultiChoiceQuestionEntity question = questionMapper.toEntity(questionDto);

        question.setPossibleChoices(questionDto
                .getPossibleChoices()
                .stream()
                .map(choiceMapper::toEntity)
                .peek(choice -> choice.setQuestion(question))
                .toList());

        return question;
    }

    @Override
    public MultiChoiceQuestionStatsDto getQuestionStats(MultiChoiceQuestionEntity question) {
        return MultiChoiceQuestionStatsDto
                .builder()
                .questionId(question.getId())
                .answerCount(multiChoiceAnswerRepository.countByQuestionId(question.getId()))
                .choiceStats(question
                        .getPossibleChoices()
                        .stream()
                        .map(choice -> ChoiceStatsDto
                                .builder()
                                .id(choice.getId())
                                .count(multiChoiceAnswerRepository.countChoiceSelectionsByChoiceId(choice.getId()))
                                .build())
                        .toList())
                .build();
    }

}
