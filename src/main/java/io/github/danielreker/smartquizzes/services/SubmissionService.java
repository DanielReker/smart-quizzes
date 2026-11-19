package io.github.danielreker.smartquizzes.services;

import com.github.kagkarlsson.scheduler.Scheduler;
import io.github.danielreker.smartquizzes.config.SchedulerTasksConfig;
import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerStatus;
import io.github.danielreker.smartquizzes.dao.entities.questions.HintEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.QuestionEntity;
import io.github.danielreker.smartquizzes.dao.entities.quiz.QuizEntity;
import io.github.danielreker.smartquizzes.dao.entities.quiz.QuizStatus;
import io.github.danielreker.smartquizzes.dao.entities.submission.SubmissionEntity;
import io.github.danielreker.smartquizzes.dao.entities.submission.SubmissionStatus;
import io.github.danielreker.smartquizzes.dao.repositories.QuizRepository;
import io.github.danielreker.smartquizzes.dao.repositories.SubmissionRepository;
import io.github.danielreker.smartquizzes.mappers.HintMapper;
import io.github.danielreker.smartquizzes.mappers.SubmissionMapper;
import io.github.danielreker.smartquizzes.model.auth.AuthenticatedUser;
import io.github.danielreker.smartquizzes.model.exceptions.InvalidQuizStatusException;
import io.github.danielreker.smartquizzes.model.exceptions.InvalidSubmissionStatusException;
import io.github.danielreker.smartquizzes.web.dtos.AnswersSubmitRequest;
import io.github.danielreker.smartquizzes.web.dtos.RevealHintRequest;
import io.github.danielreker.smartquizzes.web.dtos.SubmissionResponse;
import io.github.danielreker.smartquizzes.web.dtos.answers.AnswerResponse;
import io.github.danielreker.smartquizzes.web.dtos.answersubmitrequests.AnswerSubmitRequest;
import io.github.danielreker.smartquizzes.web.dtos.questions.HintDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    public static final Duration GRACE_PERIOD = Duration.ofSeconds(5);


    private final SubmissionRepository submissionRepository;

    private final SubmissionMapper submissionMapper;

    private final QuizRepository quizRepository;

    private final AnswerManagerService answerManagerService;

    private final HintMapper hintMapper;

    private final ObjectProvider<Scheduler> schedulerProvider;


    @Transactional
    public SubmissionResponse createSubmission(UUID quizId) {
        final QuizEntity quiz = quizRepository
                .findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz with id " + quizId + " not found"));

        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new InvalidQuizStatusException("Can't create submission for non-active quiz, its current status: " + quiz.getStatus());
        }

        final Optional<AuthenticatedUser> currentUser = Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast);

        if (currentUser.isPresent() && quiz.getMaxAttempts() != null) {
            final Long attemptsCount = submissionRepository.countByOwnerIdAndQuizId(currentUser.get().getId(), quizId);
            if (attemptsCount >= quiz.getMaxAttempts()) {
                throw new IllegalStateException("Max submission attempts exceeded: " + quiz.getMaxAttempts());
            }
        }

        final Instant now = Instant.now();
        final SubmissionEntity submission = new SubmissionEntity();
        submission.setQuiz(quiz);
        submission.setStartedAt(now);

        final List<AnswerEntity<?>> answers = quiz
                .getQuestions()
                .stream()
                .<AnswerEntity<?>>map(answerManagerService::createEmptyAnswer)
                .peek(answer -> answer.setSubmission(submission))
                .toList();

        submission.setAnswers(answers);
        final SubmissionEntity saved = submissionRepository.save(submission);

        if (quiz.getTimeToComplete() != null) {
            final Instant deadlineWithGrace = now
                    .plus(quiz.getTimeToComplete())
                    .plus(GRACE_PERIOD);
            scheduleAutoFinish(saved.getId(), deadlineWithGrace);
        }

        return buildSubmissionResponse(saved);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @submissionSecurityService.canAccessSubmission(authentication.principal.id, #submissionId)")
    @Transactional
    public SubmissionResponse getSubmission(UUID submissionId) {
        final SubmissionEntity submission = findSubmissionById(submissionId);

        return buildSubmissionResponse(submission);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @submissionSecurityService.isSubmissionOwner(authentication.principal.id, #submissionId)")
    @Transactional
    public SubmissionResponse submitAnswers(
            UUID submissionId,
            AnswersSubmitRequest request
    ) {
        final SubmissionEntity submission = findSubmissionById(submissionId);

        if (List.of(SubmissionStatus.SCORING, SubmissionStatus.FINISHED)
                .contains(getStatus(submission))) {
            throw new InvalidSubmissionStatusException(
                    "Cannot submit answer: submission with id " +
                            submission.getId() + " has already ended"
            );
        }

        final Map<UUID, AnswerSubmitRequest> answerIdToSubmitRequest = request
                .getAnswers()
                .stream()
                .collect(Collectors.toMap(
                        AnswerSubmitRequest::getAnswerId,
                        Function.identity()
                ));

        final Instant now = Instant.now();

        submission
                .getAnswers()
                .stream()
                .filter(answer -> answerIdToSubmitRequest
                        .containsKey(answer.getId()))
                .forEach(answer -> {
                    final AnswerSubmitRequest submitRequest = answerIdToSubmitRequest
                            .get(answer.getId());
                    answerManagerService.submitAnswer(answer, submitRequest);
                    answer.setAnsweredAt(now);
                });

        final SubmissionEntity saved = submissionRepository.save(submission);
        return buildSubmissionResponse(saved);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @submissionSecurityService.isSubmissionOwner(authentication.principal.id, #submissionId)")
    @Transactional
    public SubmissionResponse finishSubmission(UUID submissionId) {
        final SubmissionEntity submission = findSubmissionById(submissionId);

        if (List.of(SubmissionStatus.SCORING, SubmissionStatus.FINISHED)
                .contains(getStatus(submission))) {
            throw new InvalidSubmissionStatusException(
                    "Cannot finish already finished submission with id " + submission.getId()
            );
        }

        submission.setManuallyFinishedAt(Instant.now());
        submissionRepository.save(submission);

        scheduleAnswersScoring(submission);

        return buildSubmissionResponse(submission);
    }

    private SubmissionResponse buildSubmissionResponse(
            SubmissionEntity submissionEntity
    ) {
        final SubmissionStatus status = getStatus(submissionEntity);

        final boolean revealCorrectAnswers = List
                .of(SubmissionStatus.FINISHED, SubmissionStatus.SCORING)
                .contains(status);

        return submissionMapper
                .toResponse(submissionEntity)
                .toBuilder()
                .status(status)
                .answers(submissionEntity
                        .getAnswers()
                        .stream()
                        .<AnswerResponse<?>>map(answer -> answerManagerService
                                .buildAnswerResponse(
                                        answer, getAnswerStatus(answer), revealCorrectAnswers
                                ))
                        .toList())
                .score(isAllAnswersScored(submissionEntity) ?
                        submissionEntity
                                .getAnswers()
                                .stream()
                                .map(AnswerEntity::getScore)
                                .reduce(BigDecimal.ZERO, BigDecimal::add) :
                        null)
                .build();
    }

    @Transactional
    public void autoFinishSubmission(UUID submissionId) {
        final SubmissionEntity submission = findSubmissionById(submissionId);
        scheduleAnswersScoring(submission);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN') or @submissionSecurityService.isSubmissionOwner(authentication.principal.id, #submissionId)")
    @Transactional
    public HintDto revealHint(
            UUID submissionId,
            RevealHintRequest request
    ) {
        final SubmissionEntity submission = findSubmissionById(submissionId);

        if (List.of(SubmissionStatus.SCORING, SubmissionStatus.FINISHED)
                .contains(getStatus(submission))) {
            throw new InvalidSubmissionStatusException(
                    "Cannot reveal hint: submission has already ended"
            );
        }

        final AnswerEntity<?> answer = submission
                .getAnswers()
                .stream()
                .filter(item -> Objects
                        .equals(item.getId(), request.getAnswerId()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException(
                        "Answer with id " + request.getAnswerId() +
                                " not found in submission with id " + submissionId
                ));

        final QuestionEntity question = answer.getQuestion();

        final HintEntity nextHint = question
                .getHints()
                .stream()
                .sorted(Comparator.comparing(HintEntity::getSequentialOrder))
                .filter(hint -> !submission
                        .getRevealedHints()
                        .contains(hint))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "All hints for question " + question.getId() +
                                " were already revealed"
                ));

        submission.getRevealedHints().add(nextHint);
        submissionRepository.save(submission);

        return hintMapper.toDto(nextHint);
    }

    public SubmissionStatus getStatus(
            SubmissionEntity submission
    ) {
        if (isAllAnswersScored(submission)) {
            return SubmissionStatus.FINISHED;
        }

        if (submission.getManuallyFinishedAt() != null) {
            return SubmissionStatus.SCORING;
        }

        final Duration timeToComplete = submission
                .getQuiz()
                .getTimeToComplete();

        if (timeToComplete == null) {
            return SubmissionStatus.IN_PROCESS;
        }

        final Instant now = Instant.now();
        final Instant deadline = submission.getStartedAt().plus(timeToComplete);
        final Instant deadlineWithGrace = deadline.plus(GRACE_PERIOD);

        if (!now.isBefore(deadlineWithGrace)) {
            return SubmissionStatus.SCORING;
        }

        if (!now.isBefore(deadline)) {
            return SubmissionStatus.COMPLETING;
        }

        return SubmissionStatus.IN_PROCESS;
    }

    private static boolean isAllAnswersScored(
            SubmissionEntity submission
    ) {
        return submission
                .getAnswers()
                .stream()
                .allMatch(answer ->
                        answer.getScoredAt() != null);
    }

    public AnswerStatus getAnswerStatus(
            AnswerEntity<?> answer
    ) {
        if (answer.getScoredAt() != null) {
            return AnswerStatus.SCORED;
        }

        final SubmissionStatus submissionStatus =
                getStatus(answer.getSubmission());

        final boolean hasSubmissionEnded = List
                .of(SubmissionStatus.SCORING, SubmissionStatus.FINISHED)
                .contains(submissionStatus);

        if (answer.getAnsweredAt() != null) {
            return hasSubmissionEnded ? AnswerStatus.SCORING : AnswerStatus.ANSWERED;
        } else {
            return hasSubmissionEnded ? AnswerStatus.UNANSWERED : AnswerStatus.IN_PROCESS;
        }
    }

    private void scheduleAutoFinish(UUID submissionId, Instant executionTime) {
        schedulerProvider
                .ifAvailable(scheduler ->
                scheduler.schedule(
                        SchedulerTasksConfig
                                .AUTO_FINISH_SUBMISSION
                                .instance(submissionId.toString())
                                .data(submissionId)
                                .build(),
                        executionTime
                )
        );
    }

    private void scheduleAnswersScoring(SubmissionEntity submission) {
        submission
                .getAnswers()
                .stream()
                .filter(answer ->
                        answer.getScoredAt() == null)
                .forEach(this::scheduleAnswersScoring);
    }

    private void scheduleAnswersScoring(AnswerEntity<?> answer) {
        schedulerProvider.ifAvailable(scheduler ->
                scheduler.schedule(
                        SchedulerTasksConfig
                                .SCORE_ANSWER
                                .instance(answer.getId().toString())
                                .data(answer.getId())
                                .build(),
                        Instant.now()
                )
        );
    }

    public SubmissionEntity findSubmissionById(UUID submissionId) {
        return submissionRepository
                .findById(submissionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Submission with id " + submissionId + " not found"
                ));
    }

}
