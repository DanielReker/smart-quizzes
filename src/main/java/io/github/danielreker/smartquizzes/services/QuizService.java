package io.github.danielreker.smartquizzes.services;

import io.github.danielreker.smartquizzes.dao.entities.quiz.QuizEntity;
import io.github.danielreker.smartquizzes.dao.entities.quiz.QuizStatus;
import io.github.danielreker.smartquizzes.dao.repositories.QuizRepository;
import io.github.danielreker.smartquizzes.dao.repositories.SubmissionRepository;
import io.github.danielreker.smartquizzes.mappers.QuestionMapper;
import io.github.danielreker.smartquizzes.mappers.QuizMapper;
import io.github.danielreker.smartquizzes.model.exceptions.InvalidQuizStatusException;
import io.github.danielreker.smartquizzes.services.question.AiQuestionGeneratorService;
import io.github.danielreker.smartquizzes.web.dtos.*;
import io.github.danielreker.smartquizzes.web.dtos.questions.QuestionDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Transactional
@RequiredArgsConstructor
@Service
public class QuizService {

    private final QuizRepository quizRepository;

    private final SubmissionRepository submissionRepository;

    private final QuizMapper quizMapper;

    private final QuestionManagerService questionManagerService;

    private final QuestionMapper questionMapper;

    private final AiQuestionGeneratorService aiQuestionGeneratorService;

    private final SubmissionService submissionService;


    public QuizResponse createQuiz(
            QuizCreateRequest request
    ) {
        QuizEntity quizEntity = quizMapper.createRequestToEntity(request);
        quizEntity.setStatus(QuizStatus.DRAFT);

        quizEntity = quizRepository.save(quizEntity);

        return quizMapper.toResponse(quizEntity);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public QuizResponse updateQuiz(
            UUID quizId,
            QuizUpdateRequest request
    ) {
        QuizEntity quizEntity = findQuizEntityById(quizId);
        validateQuizUpdatability(quizEntity);
        quizMapper.updateEntityByRequest(quizEntity, request);

        quizEntity = quizRepository.save(quizEntity);

        return quizMapper.toResponse(quizEntity);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public QuizResponse getQuiz(
            UUID quizId
    ) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        return quizMapper.toResponse(quizEntity);
    }

    public ShortQuizResponse getQuizShort(
            UUID quizId
    ) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        return buildShortResponse(quizEntity);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public QuizResponse upsertQuestions(
            UUID quizId,
            QuizQuestionsUpsertRequest request
    ) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        validateQuizUpdatability(quizEntity);

        quizEntity.getQuestions().clear();
        quizEntity
                .getQuestions()
                .addAll(request
                        .questions()
                        .stream()
                        .map(questionManagerService::buildQuestion)
                        .peek(questionEntity -> questionEntity.setQuiz(quizEntity))
                        .toList());

        return quizMapper.toResponse(quizRepository.save(quizEntity));
    }

    private static void validateQuizUpdatability(
            QuizEntity quizEntity
    ) {
        if (quizEntity.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidQuizStatusException(
                    "Can't update non-draft quiz, " +
                            "its current status: " + quizEntity.getStatus()
            );
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public QuizResponse startQuiz(
            UUID quizId
    ) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        if (quizEntity.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidQuizStatusException(
                    "Can't start quiz, its current status: " + quizEntity.getStatus()
            );
        }

        quizEntity.setStatus(QuizStatus.ACTIVE);

        return quizMapper.toResponse(quizRepository.save(quizEntity));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public QuizResponse finishQuiz(
            UUID quizId
    ) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        if (quizEntity.getStatus() != QuizStatus.ACTIVE) {
            throw new InvalidQuizStatusException(
                    "Can't finish quiz, its current status: " + quizEntity.getStatus()
            );
        }

        quizEntity.setStatus(QuizStatus.FINISHED);

        return quizMapper.toResponse(quizRepository.save(quizEntity));
    }

    public SubmissionResponse createSubmission(
            UUID quizId
    ) {
        return submissionService.createSubmission(quizId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public StatsResponse getStats(UUID quizId) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        final Long submissionsCount = submissionRepository
                .countByQuizId(quizId);

        return StatsResponse
                .builder()
                .submissionsCount(submissionsCount)
                .stats(quizEntity
                        .getQuestions()
                        .stream()
                        .map(questionManagerService::getQuestionStats)
                        .toList())
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @quizSecurityService.isUserQuizOwner(authentication.principal.id, #quizId)")
    public QuizResponse aiGenerateQuestions(
            UUID quizId,
            AiGenerateQuestionsRequest request
    ) {
        final QuizEntity quizEntity = findQuizEntityById(quizId);

        final List<QuestionDto> currentQuestions = quizEntity
                .getQuestions()
                .stream()
                .map(questionMapper::toDto)
                .toList();

        final List<QuestionDto> newQuestions = aiQuestionGeneratorService
                .transformQuestions(currentQuestions, request.getPrompt());

        final QuizQuestionsUpsertRequest quizQuestionsUpsertRequest = QuizQuestionsUpsertRequest
                .builder()
                .questions(newQuestions)
                .build();

        return upsertQuestions(quizId, quizQuestionsUpsertRequest);
    }

    public QuizEntity findQuizEntityById(UUID quizId) {
        return quizRepository
                .findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Quiz with id " + quizId + " not found"
                ));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or authentication.principal.id == #userId")
    public QuizzesResponse getUserQuizzes(UUID userId) {
        return QuizzesResponse
                .builder()
                .quizzes(quizRepository
                        .findByOwnerId(userId)
                        .stream()
                        .sorted(Comparator.comparing(QuizEntity::getCreatedDate).reversed())
                        .map(this::buildShortResponse)
                        .toList())
                .build();
    }

    private ShortQuizResponse buildShortResponse(
            QuizEntity quizEntity
    ) {
        return quizMapper
                .toShortResponse(quizEntity)
                .toBuilder()
                .numberOfQuestions(quizEntity.getQuestions().size())
                .build();
    }
}
