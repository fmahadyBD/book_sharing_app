package com.fmahadybd.book_network_api_service.auth;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fmahadybd.book_network_api_service.email.EmailService;
import com.fmahadybd.book_network_api_service.email.EmailTemplateName;
import com.fmahadybd.book_network_api_service.role.RoleRepository;
import com.fmahadybd.book_network_api_service.security.JwtService;
import com.fmahadybd.book_network_api_service.user.Token;
import com.fmahadybd.book_network_api_service.user.TokenRepository;
import com.fmahadybd.book_network_api_service.user.User;
import com.fmahadybd.book_network_api_service.user.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * AuthenticationService handles user registration, authentication, and account activation.
 */
@Service // Marks this class as a Spring Service (a business logic component).
@RequiredArgsConstructor // Generates a constructor with required arguments (final fields).
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;

    // Reads the activation URL from the application properties
    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    /**
     * Registers a new user, assigns a role, saves them in the database, and sends an activation email.
     * 
     * @param request The registration request containing user details.
     * @throws MessagingException If an error occurs while sending the activation email.
     */
    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
               
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Encrypts password before saving
                .accountLocked(false)
                .enabled(false) // Account remains disabled until activation
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user); // Sends an activation email to the user
    }

    /**
     * Authenticates a user by verifying credentials and generating a JWT token.
     * 
     * @param request The authentication request containing email and password.
     * @return AuthenticationResponse containing the generated JWT token.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        var claims = new HashMap<String, Object>();
        var user = ((User) auth.getPrincipal());
        claims.put("fullName", user.getFullName()); // Adds user's full name as a claim in the token

        var jwtToken = jwtService.generateToken(claims, user); // Generates JWT token
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    /**
     * Activates a user account using a provided token.
     * 
     * @param token The activation token received via email.
     * @throws MessagingException If an error occurs while resending a validation email.
     */
    @Transactional // Ensures the activation process runs as a single transaction.
    public void activateAccount(String token) throws MessagingException {
        Token savedToken = tokenRepository.findByToken(token)
                // TODO: Define a proper exception for invalid tokens
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        // Checks if the token has expired
        if (LocalDateTime.now().isAfter(savedToken.getExpiresAt())) {
            sendValidationEmail(savedToken.getUser()); // Resends activation email
            throw new RuntimeException(
                    "Activation token has expired. A new token has been sent to the same email address.");
        }

        var user = userRepository.findById(savedToken.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setEnabled(true); // Enables the user account
        userRepository.save(user); // Saves the updated user state

        savedToken.setValidatedAt(LocalDateTime.now()); // Marks token as used
        tokenRepository.save(savedToken);
    }

    /**
     * Generates and stores an activation token for a given user.
     * 
     * @param user The user for whom the activation token is generated.
     * @return The generated activation token.
     */
    private String generateAndSaveActivationToken(User user) {
        // Generates a random 6-digit activation code
        String generatedToken = generateActivationCode(6);

        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(15)) // Token expires in 15 minutes
                .user(user)
                .build();

        tokenRepository.save(token); // Saves the token in the database
        return generatedToken;
    }

    /**
     * Sends an activation email with a generated token.
     * 
     * @param user The user to whom the email is sent.
     * @throws MessagingException If an error occurs while sending the email.
     */
    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveActivationToken(user); // Generates a new token

        emailService.sendEmail(
                user.getEmail(),
                user.getFullName(),
                EmailTemplateName.ACTIVATE_ACCOUNT,
                activationUrl,
                newToken,
                "Account activation"); // Sends email with activation instructions
    }

    /**
     * Generates a random numeric activation code of the specified length.
     * 
     * @param length The length of the activation code.
     * @return The generated activation code.
     */
    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();

        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length());
            codeBuilder.append(characters.charAt(randomIndex));
        }

        return codeBuilder.toString(); // Returns the generated activation code
    }
}
