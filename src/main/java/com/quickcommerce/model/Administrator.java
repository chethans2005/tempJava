package com.quickcommerce.model;

import java.util.List;

/**
 * Represents a platform Administrator with elevated privileges to
 * manage users, generate reports, and monitor all orders.
 *
 * <p>The Administrator role follows the Principle of Least Privilege:
 * it can view and manage, but all destructive actions are logged
 * for auditability.</p>
 */
public class Administrator extends User {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private String adminLevel;   // e.g., "SUPER_ADMIN", "SUPPORT"

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates an Administrator account.
     *
     * @param name       full name
     * @param email      e-mail address
     * @param password   password
     * @param adminLevel privilege level of this admin
     */
    public Administrator(String name, String email, String password, String adminLevel) {
        super(name, email, password);
        this.adminLevel = adminLevel;
    }

    // -----------------------------------------------------------------------
    // Business methods
    // -----------------------------------------------------------------------

    /**
     * Displays all users currently registered in the system.
     *
     * @param allUsers the full list of users to manage
     */
    public void manageUsers(List<User> allUsers) {
        System.out.println("\n[ADMIN] === User Management Report ===");
        System.out.printf("%-36s %-20s %-30s %-15s%n",
                "User ID", "Name", "Email", "Role");
        System.out.println("-".repeat(105));
        allUsers.forEach(u -> System.out.printf("%-36s %-20s %-30s %-15s%n",
                u.getUserId(), u.getName(), u.getEmail(), u.getRole()));
        System.out.println("[ADMIN] Total users: " + allUsers.size());
    }

    /**
     * Generates a summary report of all orders in the system.
     *
     * @param allOrders the full list of orders to report on
     */
    public void generateReports(List<Order> allOrders) {
        System.out.println("\n[ADMIN] === Order Summary Report ===");
        long pending   = allOrders.stream().filter(o -> o.getStatus().name().equals("PENDING")).count();
        long delivered = allOrders.stream().filter(o -> o.getStatus().name().equals("DELIVERED")).count();
        long cancelled = allOrders.stream().filter(o -> o.getStatus().name().equals("CANCELLED")).count();
        double revenue = allOrders.stream()
                .filter(o -> !o.getStatus().name().equals("CANCELLED"))
                .mapToDouble(Order::getTotalAmount)
                .sum();

        System.out.println("  Total Orders   : " + allOrders.size());
        System.out.println("  Pending        : " + pending);
        System.out.println("  Delivered      : " + delivered);
        System.out.println("  Cancelled      : " + cancelled);
        System.out.printf("  Total Revenue  : INR %.2f%n", revenue);
    }

    /**
     * Monitors and prints details of all active (non-delivered, non-cancelled) orders.
     *
     * @param allOrders the full list of orders to monitor
     */
    public void monitorOrders(List<Order> allOrders) {
        System.out.println("\n[ADMIN] === Active Orders Monitor ===");
        allOrders.stream()
                .filter(o -> !o.getStatus().name().equals("DELIVERED")
                          && !o.getStatus().name().equals("CANCELLED"))
                .forEach(o -> System.out.printf(
                        "  Order %-36s | Customer: %-15s | Status: %-20s | Amount: INR %.2f%n",
                        o.getOrderId(),
                        o.getCustomer().getName(),
                        o.getStatus(),
                        o.getTotalAmount()));
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public String getAdminLevel()                   { return adminLevel; }
    public void   setAdminLevel(String adminLevel)  { this.adminLevel = adminLevel; }

    // -----------------------------------------------------------------------
    // User overrides
    // -----------------------------------------------------------------------

    @Override
    public String getRole() {
        return "Administrator";
    }

    @Override
    public String toString() {
        return String.format("Administrator{id='%s', name='%s', level='%s'}",
                getUserId(), getName(), adminLevel);
    }
}
