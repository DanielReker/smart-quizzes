package io.github.danielreker.smartquizzes.dao.entities.answers;

import io.github.danielreker.smartquizzes.dao.entities.questions.TextQuestionEntity;
import io.github.danielreker.smartquizzes.model.QuestionType;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Length;

@Getter
@Setter
@NoArgsConstructor
@Entity
@DiscriminatorValue(QuestionType.Names.TEXT)
public class TextAnswerEntity
        extends AnswerEntity<TextQuestionEntity> {

    @Override
    public QuestionType getQuestionType() {
        return QuestionType.TEXT;
    }

    @Column(name = "value", columnDefinition = "text", length = Length.LONG32)
    private String value;

    @Column(name = "feedback", columnDefinition = "text", length = Length.LONG32)
    private String feedback;

}