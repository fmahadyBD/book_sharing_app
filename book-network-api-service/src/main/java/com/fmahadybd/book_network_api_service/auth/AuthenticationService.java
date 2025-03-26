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


@Service 
@RequiredArgsConstructor 
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final EmailService emailService;
    private final TokenRepository tokenRepository;


    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;


    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
               
                .orElseThrow(() -> new IllegalStateException("ROLE USER was not initiated"));

        var user = User.builder()
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) 
                .accountLocked(false)
                .enabled(false) // Account remains disabled until activation
                .roles(List.of(userRole))
                .build();

        userRepository.save(user);
        sendValidationEmail(user); 
    }

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
