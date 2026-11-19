package io.github.danielreker.smartquizzes.services.question;

import io.github.danielreker.smartquizzes.dao.entities.questions.ChoiceEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.SingleChoiceQuestionEntity;
import io.github.danielreker.smartquizzes.dao.repositories.answers.SingleChoiceAnswerRepository;
import io.github.danielreker.smartquizzes.mappers.ChoiceMapper;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.mappers.QuestionMapper;
import io.github.danielreker.smartquizzes.model.QuestionType;
import io.github.danielreker.smartquizzes.web.dtos.questions.SingleChoiceQuestionDto;
import io.github.danielreker.smartquizzes.web.dtos.stats.ChoiceStatsDto;
import io.github.danielreker.smartquizzes.web.dtos.stats.SingleChoiceQuestionStatsDto;
import org.springframework.stereotype.Service;

@Service
public class SingleChoiceQuestionService
        extends QuestionService<SingleChoiceQuestionEntity, SingleChoiceQuestionDto> {

    private final ChoiceMapper choiceMapper;

    private final SingleChoiceAnswerRepository singleChoiceAnswerRepository;

    public SingleChoiceQuestionService(
            HintMapper hintMapper,
            QuestionMapper questionMapper,
            ChoiceMapper choiceMapper,
            SingleChoiceAnswerRepository singleChoiceAnswerRepository
    ) {
        super(hintMapper, questionMapper);
        this.choiceMapper = choiceMapper;
        this.singleChoiceAnswerRepository = singleChoiceAnswerRepository;
    }

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.SINGLE_CHOICE;
    }

    @Override
    public SingleChoiceQuestionEntity buildConcreteQuestion(SingleChoiceQuestionDto questionDto) {
        final SingleChoiceQuestionEntity question = questionMapper.toEntity(questionDto);

        question.setPossibleChoices(questionDto
                .getPossibleChoices()
                .stream()
                .map(choiceMapper::toEntity)
                .peek(choice -> choice.setQuestion(question))
                .toList());

        validateCorrectChoicesNumber(question);

        return question;
    }

    private void validateCorrectChoicesNumber(SingleChoiceQuestionEntity questionEntity) {
        final long correctChoicesNumber = questionEntity
                .getPossibleChoices()
                .stream()
                .filter(ChoiceEntity::getIsCorrect)
                .count();

        if (correctChoicesNumber != 1) {
            throw new IllegalArgumentException(
                    getQuestionType().getName() + " question must have exactly 1 correct choice"
            );
        }
    }

    @Override
    public SingleChoiceQuestionStatsDto getQuestionStats(SingleChoiceQuestionEntity question) {
        return SingleChoiceQuestionStatsDto
                .builder()
                .questionId(question.getId())
                .answerCount(singleChoiceAnswerRepository.countByQuestionId(question.getId()))
                .choiceStats(question
                        .getPossibleChoices()
                        .stream()
                        .map(choice -> ChoiceStatsDto
                                .builder()
                                .id(choice.getId())
                                .count(singleChoiceAnswerRepository.countChoiceSelectionsByChoiceId(choice.getId()))
                                .build())
                        .toList())
                .build();
    }

}
