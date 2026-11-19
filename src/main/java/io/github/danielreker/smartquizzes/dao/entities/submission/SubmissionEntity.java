package io.github.danielreker.smartquizzes.dao.entities.submission;

import io.github.danielreker.smartquizzes.dao.entities.BaseEntity;
import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import io.github.danielreker.smartquizzes.dao.entities.auth.UserEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.HintEntity;
import io.github.danielreker.smartquizzes.dao.entities.quiz.QuizEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(
        name = "submission",
        indexes = @Index(name = "idx_submission_quiz_id", columnList = "quiz_id")
)
@EntityListeners(AuditingEntityListener.class)
public class SubmissionEntity extends BaseEntity {

    @CreatedDate
    @Column(name = "started_at", columnDefinition = "timestamptz", nullable = false)
    private Instant startedAt;

    @Column(name = "manually_finished_at", columnDefinition = "timestamptz")
    private Instant manuallyFinishedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private QuizEntity quiz;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerEntity<?>> answers = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "submission_revealed_hint",
            joinColumns = @JoinColumn(name = "submission_id"),
            inverseJoinColumns = @JoinColumn(name = "hint_id"),
            indexes = @Index(name = "idx_submission_revealed_hint_submission_id", columnList = "submission_id")
    )
    private List<HintEntity> revealedHints = new ArrayList<>();

    @CreatedBy
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserEntity owner;

}