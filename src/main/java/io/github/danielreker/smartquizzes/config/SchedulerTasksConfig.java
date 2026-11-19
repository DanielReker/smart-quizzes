package io.github.danielreker.smartquizzes.config;

import com.github.kagkarlsson.scheduler.task.Task;
import com.github.kagkarlsson.scheduler.task.TaskDescriptor;
import com.github.kagkarlsson.scheduler.task.helper.Tasks;
import io.github.danielreker.smartquizzes.services.AnswerScoringService;
import io.github.danielreker.smartquizzes.services.SubmissionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class SchedulerTasksConfig {

    public static final TaskDescriptor<UUID> SCORE_ANSWER =
            TaskDescriptor.of("score-answer", UUID.class);

    public static final TaskDescriptor<UUID> AUTO_FINISH_SUBMISSION =
            TaskDescriptor.of("auto-finish-submission", UUID.class);

    @Bean
    public Task<UUID> scoreAnswerTask(AnswerScoringService answerScoringService) {
        return Tasks
                .oneTime(SCORE_ANSWER)
                .execute((instance, _) ->
                        answerScoringService.scoreAnswer(instance.getData()));
    }

    @Bean
    public Task<UUID> autoFinishSubmissionTask(SubmissionService submissionService) {
        return Tasks
                .oneTime(AUTO_FINISH_SUBMISSION)
                .execute((instance, _) ->
                        submissionService.autoFinishSubmission(instance.getData()));
    }

}
