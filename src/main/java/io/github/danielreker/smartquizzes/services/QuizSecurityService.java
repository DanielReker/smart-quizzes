package io.github.danielreker.smartquizzes.services;

import io.github.danielreker.smartquizzes.dao.repositories.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@SuppressWarnings("unused")
public class QuizSecurityService {

    private final QuizRepository quizRepository;

    public boolean isUserQuizOwner(UUID userId, UUID quizId) {
        return quizRepository.existsByIdAndOwnerId(quizId, userId);
    }

}
