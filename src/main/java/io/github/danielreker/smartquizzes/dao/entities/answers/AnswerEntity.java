package io.github.danielreker.smartquizzes.dao.entities.answers;

import io.github.danielreker.smartquizzes.dao.entities.BaseEntity;
import io.github.danielreker.smartquizzes.dao.entities.questions.QuestionEntity;
import io.github.danielreker.smartquizzes.dao.entities.submission.SubmissionEntity;
import io.github.danielreker.smartquizzes.model.QuestionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "answer",
        indexes = @Index(name = "idx_answer_submission_id", columnList = "submission_id")
)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "dtype")
public abstract class AnswerEntity<Q_ENTITY extends QuestionEntity>
        extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private SubmissionEntity submission;

    @ManyToOne(targetEntity = QuestionEntity.class, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Q_ENTITY question;

    @Column(name = "score", columnDefinition = "numeric(10,3)", precision = 10, scale = 3)
    private BigDecimal score;

    @Column(name = "answered_at", columnDefinition = "timestamptz")
    private Instant answeredAt;

    @Column(name = "scored_at", columnDefinition = "timestamptz")
    private Instant scoredAt;

    public abstract QuestionType getQuestionType();

}