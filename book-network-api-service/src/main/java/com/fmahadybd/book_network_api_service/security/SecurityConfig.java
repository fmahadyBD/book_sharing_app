package com.fmahadybd.book_network_api_service.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtFilter jwtAuthFilter;  // Custom JWT filter to intercept and validate tokens
    private final AuthenticationProvider authenticationProvider;  // Authentication provider to validate user credentials

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())  // Enables CORS with default settings
                .csrf(AbstractHttpConfigurer::disable)  // Disables CSRF protection (since you're using stateless JWT authentication)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(
                                        "/auth/**",  // Allow unauthenticated access to authentication endpoints
                                        "/v2/api-docs",  // Swagger docs endpoints
                                        "/v3/api-docs",  // Swagger docs endpoints
                                        "/v3/api-docs/**",  // Swagger docs endpoints
                                        "/swagger-resources",  // Swagger resources
                                        "/swagger-resources/**",  // Swagger resources
                                        "/configuration/ui",  // Swagger UI configuration
                                        "/configuration/security",  // Swagger security configuration
                                        "/swagger-ui/**",  // Swagger UI
                                        "/swagger-ui.html",  // Swagger UI HTML
                                        "/webjars/**"  // WebJars for Swagger UI
                                )
                                    .permitAll()  // Allows anyone to access these endpoints without authentication
                                .anyRequest()  // Any other request requires authentication
                                    .authenticated()  // User must be authenticated
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))  // Configures the session to be stateless
                .authenticationProvider(authenticationProvider)  // Uses the custom AuthenticationProvider for user authentication
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);  // Adds the custom JWT filter before UsernamePasswordAuthenticationFilter

        return http.build();  // Returns the security filter chain configuration
    }
}
