package com.quickcommerce;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.model.Administrator;
import com.quickcommerce.model.Customer;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.Product;
import com.quickcommerce.model.Vendor;
import com.quickcommerce.service.ProductPresenter;

/**
 * Entry point for the Quick Commerce Grocery Delivery Application.
 *
 * <p>This class acts as the Application Layer / Demo Runner.
 * It wires together all services and model objects to simulate
 * a complete end-to-end order flow:</p>
 *
 * <ol>
 *   <li>Vendor registers and adds products</li>
 *   <li>Customer registers, logs in, and fills their cart</li>
 *   <li>Customer places an order and checks out</li>
 *   <li>Vendor confirms the order</li>
 *   <li>Delivery partner is assigned and delivers the order</li>
 *   <li>Admin generates reports</li>
 * </ol>
 */
public class Main {

    public static void main(String[] args) {

        // ===================================================================
        // 1. Initialise services
        // ===================================================================
        com.quickcommerce.service.IUserService    userService    = new com.quickcommerce.service.UserService();
        com.quickcommerce.service.IProductService productService = new com.quickcommerce.service.ProductService();
        com.quickcommerce.service.IOrderService   orderService   = new com.quickcommerce.service.OrderService();

        printSectionHeader("QUICK COMMERCE — APPLICATION START");

        // ===================================================================
        // 2. Register users
        // ===================================================================
        printSectionHeader("STEP 1 — Register Users");

        // Vendor
        Vendor vendor = new Vendor(
                "Ravi Kumar", "ravi@freshmart.com", "vendor123", "FreshMart Groceries");
        userService.registerUser(vendor);

        // Customer
        Customer customer = new Customer(
                "Priya Sharma", "priya@gmail.com", "priya123",
                "42, Anna Nagar, Chennai - 600040");
        userService.registerUser(customer);

        // Delivery Partner
        DeliveryPartner deliveryPartner = new DeliveryPartner(
                "Arjun Raj", "arjun@delivery.com", "arjun123");
        userService.registerUser(deliveryPartner);
        orderService.registerDeliveryPartner(deliveryPartner);

        // Administrator
        Administrator admin = new Administrator(
                "Admin Alice", "alice@quickcommerce.com", "admin123", "SUPER_ADMIN");
        userService.registerUser(admin);

        // ===================================================================
        // 3. Vendor: Add products to catalogue
        // ===================================================================
        printSectionHeader("STEP 2 — Vendor Lists Products");

        vendor.login("ravi@freshmart.com", "vendor123");

        Product milk     = new Product("Full Cream Milk 1L",  "Dairy",   60.00, 100);
        Product bread    = new Product("Whole Wheat Bread",   "Bakery",  45.00,  50);
        Product eggs     = new Product("Farm Fresh Eggs (12)","Dairy",   89.00,  80);
        Product rice     = new Product("Basmati Rice 5kg",    "Grains", 350.00,  30);
        Product chocolate= new Product("Dark Chocolate 100g", "Snacks",  99.00,  60);

        vendor.addProduct(milk);
        vendor.addProduct(bread);
        vendor.addProduct(eggs);
        vendor.addProduct(rice);
        vendor.addProduct(chocolate);

        // Register products in the platform-wide catalogue
        productService.addProduct(milk);
        productService.addProduct(bread);
        productService.addProduct(eggs);
        productService.addProduct(rice);
        productService.addProduct(chocolate);

        ProductPresenter.displayAllProducts(productService.getAllProducts());

        // ===================================================================
        // 4. Customer: Log in, browse, and add items to cart
        // ===================================================================
        printSectionHeader("STEP 3 — Customer Shops");

        customer.login("priya@gmail.com", "priya123");

        customer.addToCart(milk,      3);
        customer.addToCart(bread,     2);
        customer.addToCart(eggs,      1);
        customer.addToCart(chocolate, 2);

        System.out.println("\n[CART] " + customer.getCart());

        // ===================================================================
        // 5. Customer: Place order
        // ===================================================================
        printSectionHeader("STEP 4 — Customer Places Order");

        Order order = customer.placeOrder();
        orderService.registerOrder(order);

        // ===================================================================
        // 6. Customer: Checkout (make payment)
        // ===================================================================
        printSectionHeader("STEP 5 — Payment / Checkout");

        boolean paymentSuccess = order.checkout(PaymentMethod.UPI);
        System.out.println("[CHECKOUT] Payment successful: " + paymentSuccess);
        System.out.println("[CHECKOUT] " + order.getPayment());

        // ===================================================================
        // 7. Vendor: Confirm the order
        // ===================================================================
        printSectionHeader("STEP 6 — Vendor Confirms Order");

        vendor.processOrder(order);

        // ===================================================================
        // 8. Assign delivery partner
        // ===================================================================
        printSectionHeader("STEP 7 — Assign Delivery Partner");

        orderService.assignDeliveryPartner(order.getOrderId());

        // ===================================================================
        // 9. Delivery partner: Update status to DELIVERED
        // ===================================================================
        printSectionHeader("STEP 8 — Order Delivered");

        deliveryPartner.login("arjun@delivery.com", "arjun123");
        deliveryPartner.viewAssignedOrders();
        deliveryPartner.updateDeliveryStatus(order.getOrderId(), OrderStatus.DELIVERED);

        // ===================================================================
        // 10. Customer: Track order
        // ===================================================================
        printSectionHeader("STEP 9 — Customer Tracks Order");

        customer.trackOrder(order.getOrderId());

        // ===================================================================
        // 11. Place a second order and cancel it (to demonstrate cancellation)
        // ===================================================================
        printSectionHeader("STEP 10 — Second Order (Cancellation Demo)");

        customer.addToCart(rice, 2);
        Order order2 = customer.placeOrder();
        orderService.registerOrder(order2);
        order2.checkout(PaymentMethod.CREDIT_CARD);
        System.out.println("[ORDER2] Before cancellation: " + order2.getStatus());
        order2.cancelOrder();
        System.out.println("[ORDER2] After cancellation:  " + order2.getStatus());
        System.out.println("[ORDER2] Rice stock after cancel: " + rice.getStockQuantity());

        // ===================================================================
        // 12. Admin: Reports and monitoring
        // ===================================================================
        printSectionHeader("STEP 11 — Admin Reports & Monitoring");

        admin.login("alice@quickcommerce.com", "admin123");
        admin.manageUsers(userService.getAllUsers());
        admin.generateReports(orderService.getAllOrders());
        admin.monitorOrders(orderService.getAllOrders());

        // ===================================================================
        // 13. Product search demo
        // ===================================================================
        printSectionHeader("STEP 12 — Product Search Demo");

        System.out.println("[SEARCH] Searching for 'milk':");
        productService.searchByName("milk").forEach(System.out::println);

        System.out.println("\n[SEARCH] Browsing category 'Dairy':");
        productService.getByCategory("Dairy").forEach(System.out::println);

        printSectionHeader("APPLICATION DEMO COMPLETE");
    }

    // -----------------------------------------------------------------------
    // Utility
    // -----------------------------------------------------------------------

    private static void printSectionHeader(String title) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("  " + title);
        System.out.println("=".repeat(70));
    }
}
