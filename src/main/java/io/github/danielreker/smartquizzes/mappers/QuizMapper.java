package io.github.danielreker.smartquizzes.mappers;

import io.github.danielreker.smartquizzes.dao.entities.QuizEntity;
import io.github.danielreker.smartquizzes.web.QuizUpdateRequest;
import io.github.danielreker.smartquizzes.web.dtos.QuizCreateRequest;
import io.github.danielreker.smartquizzes.web.dtos.QuizResponse;
import io.github.danielreker.smartquizzes.web.dtos.ShortQuizResponse;
import org.mapstruct.*;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        uses = {QuestionMapper.class, JsonNullableMapper.class}
)
public interface QuizMapper {

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "owner", ignore = true)
    QuizEntity createRequestToEntity(QuizCreateRequest request);

    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "submissions", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityByRequest(
            @MappingTarget QuizEntity entity,
            QuizUpdateRequest request
    );


    QuizResponse toResponse(QuizEntity entity);

    ShortQuizResponse toShortResponse(QuizEntity entity);

}
