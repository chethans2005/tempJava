package com.quickcommerce.model;

import java.util.UUID;

/**
 * Abstract base class representing a User in the Quick Commerce system.
 *
 * <p>Follows the Open/Closed Principle: open for extension (via subclasses),
 * closed for modification. All common user attributes and authentication
 * behaviour live here so subclasses do not duplicate them.</p>
 *
 * <p>The class uses encapsulation (private fields + public getters/setters)
 * and declares abstract role-specific behaviour that every concrete subclass
 * must implement.</p>
 */
public abstract class User {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private String userId;
    private String name;
    private String email;
    private String password;          // In production: store a hashed value
    private boolean loggedIn;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new User with an auto-generated UUID.
     *
     * @param name     full name of the user
     * @param email    unique e-mail address
     * @param password plain-text password (hash before persisting in production)
     */
    public User(String name, String email, String password) {
        this.userId   = UUID.randomUUID().toString();
        this.name     = name;
        this.email    = email;
        this.password = password;
        this.loggedIn = false;
    }

    // -----------------------------------------------------------------------
    // Authentication methods
    // -----------------------------------------------------------------------

    /**
     * Authenticates the user with the supplied credentials.
     *
     * @param email    the e-mail to match
     * @param password the password to verify
     * @return {@code true} if credentials are correct
     */
    public boolean login(String email, String password) {
        if (this.email.equals(email) && this.password.equals(password)) {
            this.loggedIn = true;
            System.out.println("[AUTH] " + name + " logged in successfully.");
            return true;
        }
        System.out.println("[AUTH] Login failed for: " + email);
        return false;
    }

    /**
     * Logs the user out of the system.
     */
    public void logout() {
        this.loggedIn = false;
        System.out.println("[AUTH] " + name + " logged out.");
    }

    // -----------------------------------------------------------------------
    // Abstract method — role-specific behaviour
    // -----------------------------------------------------------------------

    /**
     * Returns a description of this user's role in the system.
     * Each concrete subclass must provide its own implementation.
     *
     * @return role description string
     */
    public abstract String getRole();

    // -----------------------------------------------------------------------
    // Getters & Setters (Encapsulation)
    // -----------------------------------------------------------------------

    public String getUserId()               { return userId; }

    /**
     * Used by persistence layer to restore an existing user id.
     */
    public void setUserId(String userId) { this.userId = userId; }

    /**
     * Returns the stored password. Intended for persistence only.
     */
    public String getPassword() { return password; }

    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }

    public String getEmail()                 { return email; }
    public void   setEmail(String email)     { this.email = email; }

    // Password deliberately has no getter — never expose it
    public void   setPassword(String password) { this.password = password; }

    public boolean isLoggedIn()              { return loggedIn; }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("User{userId='%s', name='%s', email='%s', role='%s'}",
                userId, name, email, getRole());
    }
}
