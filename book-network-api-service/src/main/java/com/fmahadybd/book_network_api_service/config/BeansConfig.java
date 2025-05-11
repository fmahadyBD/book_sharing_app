package com.fmahadybd.book_network_api_service.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.Collections;

/**
 * Configuration class for defining Spring Beans related to security and
 * auditing.
 */
@Configuration
@RequiredArgsConstructor
public class BeansConfig {

    // Injecting the UserDetailsService to be used in authentication
    private final UserDetailsService userDetailsService;

    /**
     * Defines a bean for AuthenticationProvider using DaoAuthenticationProvider.
     * This provider retrieves user details from the UserDetailsService and verifies
     * passwords.
     *
     * @return Configured AuthenticationProvider
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Setting user details service
        authProvider.setPasswordEncoder(passwordEncoder()); // Setting password encoder
        return authProvider;
    }

    /**
     * Defines a bean for AuthenticationManager, which manages authentication
     * operations.
     *
     * @param config AuthenticationConfiguration instance provided by Spring
     *               Security
     * @return Configured AuthenticationManager
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines a bean for password encoding using BCrypt hashing algorithm.
     * This ensures that passwords are securely stored.
     *
     * @return PasswordEncoder instance using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines a bean for auditing the current authenticated user.
     * This is used for tracking created/modified entities in JPA.
     *
     * @return AuditorAware<Integer> implementation
     */
    @Bean
    public AuditorAware<Integer> auditorAware() {
        return new ApplicationAuditAware();
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Collections.singletonList("http://localhost:4200"));
        config.setAllowedHeaders(Arrays.asList(
                HttpHeaders.ORIGIN,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.AUTHORIZATION));
        config.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "DELETE",
                "PUT",
                "OPTIONS",
                "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);

    }
}
