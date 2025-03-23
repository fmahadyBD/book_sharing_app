package com.fmahadybd.book_network_api_service.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthenticationController handles authentication-related requests such as user registration, 
 * authentication, and account activation.
 */
@RestController // Marks this class as a REST controller that returns JSON responses.
@RequestMapping("auth") // Base URL for all endpoints in this controller.
@RequiredArgsConstructor // Generates a constructor with required arguments for final fields.
@Tag(name = "Authentication") // Used for grouping API documentation in Swagger.
public class AuthenticationController {

    private final AuthenticationService service; // Injects AuthenticationService using Lombok's @RequiredArgsConstructor.

    /**
     * Handles user registration requests.
     * 
     * @param request The registration request object.
     * @return ResponseEntity with status ACCEPTED (202).
     * @throws MessagingException If an error occurs while sending a confirmation email.
     */
    @PostMapping("/register") // Maps HTTP POST requests to /auth/register.
    @ResponseStatus(HttpStatus.ACCEPTED) // Sets the response status to 202 Accepted.
    public ResponseEntity<?> register(
            @RequestBody @Valid RegistrationRequest request // Extracts request body and validates it.
    ) throws MessagingException {
        service.register(request); // Calls the service to handle registration logic.
        return ResponseEntity.accepted().build(); // Returns 202 Accepted response.
    }

    /**
     * Handles user authentication requests.
     * 
     * @param request The authentication request object.
     * @return ResponseEntity with an AuthenticationResponse object.
     */
    @PostMapping("/authenticate") // Maps HTTP POST requests to /auth/authenticate.
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request // Extracts request body.
    ) {
        return ResponseEntity.ok(service.authenticate(request)); // Returns the authentication response.
    }

    /**
     * Handles account activation via a token.
     * 
     * @param token The activation token received via email.
     * @throws MessagingException If an error occurs while processing activation.
     */
    @GetMapping("/activate-account") // Maps HTTP GET requests to /auth/activate-account.
    public void confirm(
            @RequestParam String token // Extracts token parameter from URL query string.
    ) throws MessagingException {
        service.activateAccount(token); // Calls service to activate account.
    }
}
