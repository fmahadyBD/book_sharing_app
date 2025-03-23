package com.fmahadybd.book_network_api_service.email;

import lombok.Getter;

/**
 * Enum representing the names of email templates used in the application.
 */
@Getter // Generates a getter method for the 'name' field
public enum EmailTemplateName {

    /** Enum constant for the account activation email template */
    ACTIVATE_ACCOUNT("activate_account");

    private final String name; // Holds the template name as a string

    /**
     * Constructor to initialize the enum with a specific template name.
     *
     * @param name The template name associated with the enum constant.
     */
    EmailTemplateName(String name) {
        this.name = name;
    }
}
