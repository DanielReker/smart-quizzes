package io.github.danielreker.smartquizzes.mappers;

import io.github.danielreker.smartquizzes.dao.entities.submission.SubmissionEntity;
import io.github.danielreker.smartquizzes.web.dtos.SubmissionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface SubmissionMapper {

    @Mapping(target = "answers", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "score", ignore = true)
    @Mapping(target = "quizId", source = "quiz.id")
    SubmissionResponse toResponse(SubmissionEntity entity);

}
