package io.github.danielreker.smartquizzes.dao.repositories.answers;

import io.github.danielreker.smartquizzes.dao.entities.answers.AnswerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerRepository
        extends JpaRepository<AnswerEntity<?>, UUID> {

}
