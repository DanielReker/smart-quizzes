package io.github.danielreker.smartquizzes.services;

import io.github.danielreker.smartquizzes.dao.repositories.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
@SuppressWarnings("unused")
public class SubmissionSecurityService {

    private final SubmissionRepository submissionRepository;


    public boolean isSubmissionOwner(UUID userId, UUID submissionId) {
        return submissionRepository.existsByIdAndOwnerId(submissionId, userId);
    }

    public boolean canAccessSubmission(UUID userId, UUID submissionId) {
        return isSubmissionOwner(submissionId, userId)
                || submissionRepository.existsByIdAndQuizOwnerId(submissionId, userId);
    }

}
