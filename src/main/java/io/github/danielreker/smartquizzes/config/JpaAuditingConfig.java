package io.github.danielreker.smartquizzes.config;

import io.github.danielreker.smartquizzes.dao.entities.auth.UserEntity;
import io.github.danielreker.smartquizzes.model.auth.AuthenticatedUser;
import jakarta.persistence.EntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<UserEntity> auditorProvider(
            EntityManager entityManager
    ) {
        return () -> Optional
                .of(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .filter(AuthenticatedUser.class::isInstance)
                .map(AuthenticatedUser.class::cast)
                .map(principal -> entityManager
                        .getReference(UserEntity.class, principal.getId()));
    }
}
