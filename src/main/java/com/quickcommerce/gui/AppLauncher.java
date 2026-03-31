package com.quickcommerce.gui;

/**
 * GUI entry point for the Quick Commerce application.
 *
 * Run this class to launch the login screen.
 * All demo data is seeded automatically by {@link AppContext}.
 *
 * Demo credentials (pre-filled in the login form):
 *
 *   Customer         → priya@gmail.com         / priya123
 *   Vendor           → ravi@freshmart.com       / vendor123
 *   Delivery Partner → arjun@delivery.com       / arjun123
 *   Administrator    → alice@quickcommerce.com  / admin123
 */
public class AppLauncher {

    public static void main(String[] args) {
        // Delegate to Spring Boot application which will seed data and start the Swing UI
        com.quickcommerce.SpringApp.main(args);
    }
}
