package com.fmahadybd.book_network_api_service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

/**
 * Service class responsible for sending emails asynchronously using Spring Mail and Thymeleaf templates.
 */
@Service
@Slf4j // Enables logging capabilities
@RequiredArgsConstructor // Generates a constructor with required fields (final variables)
public class EmailService {
    
    private final JavaMailSender mailSender; // Handles sending emails
    private final SpringTemplateEngine templateEngine; // Processes email templates

    /**
     * Sends an email asynchronously using a Thymeleaf email template.
     *
     * @param to             Recipient email address
     * @param username       Username of the recipient
     * @param emailTemplate  Enum representing the email template name
     * @param confirmationUrl URL for email confirmation
     * @param activationCode Activation code to be included in the email
     * @param subject        Email subject
     * @throws MessagingException if an error occurs while creating or sending the email
     */
    @Async // Marks this method for asynchronous execution
    public void sendEmail(
            String to,
            String username,
            EmailTemplateName emailTemplate,
            String confirmationUrl,
            String activationCode,
            String subject) throws MessagingException {
        
        // Determine the template name based on the provided email template enum
        String templateName = (emailTemplate == null) ? "confirm-email" : emailTemplate.getName();

        // Create a new email message
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED, // Allows multiple parts (text, attachments)
                UTF_8.name()); // Ensures UTF-8 character encoding

        // Populate email template properties
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        // Create the Thymeleaf context and set template variables
        Context context = new Context();
        context.setVariables(properties);

        // Configure email properties
        helper.setFrom("contact@fahimdev.com"); // Sender's email address
        helper.setTo(to); // Recipient email address
        helper.setSubject(subject); // Email subject

        // Process the Thymeleaf template and set the email content
        String template = templateEngine.process(templateName, context);
        helper.setText(template, true); // Enables HTML content in the email

        // Send the email
        mailSender.send(mimeMessage);
    }
}
