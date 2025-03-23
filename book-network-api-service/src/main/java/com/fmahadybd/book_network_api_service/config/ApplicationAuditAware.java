package com.fmahadybd.book_network_api_service.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fmahadybd.book_network_api_service.user.User;

import java.util.Optional;

/**
 * Implementation of AuditorAware to provide the currently authenticated user's ID
 * for auditing purposes in JPA entities.
 */
public class ApplicationAuditAware implements AuditorAware<Integer> {

    /**
     * Retrieves the ID of the currently authenticated user.
     *
     * @return Optional containing the user ID if authenticated, otherwise empty.
     */
    @Override
    public Optional<Integer> getCurrentAuditor() {
        // Get the authentication object from the Security Context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Check if the authentication object is null, not authenticated, or an anonymous user
        if (authentication == null || 
            !authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty(); // No authenticated user
        }

        // Retrieve the principal (user details) from the authentication object
        User userPrincipal = (User) authentication.getPrincipal();
        
        // Return the user's ID wrapped in an Optional
        return Optional.ofNullable(userPrincipal.getId());
    }
}
