package com.quickcommerce.gui;

import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.model.Administrator;
import com.quickcommerce.model.Customer;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.Product;
import com.quickcommerce.model.Vendor;

/**
 * Application-level singleton that seeds all services with demo data
 * so every GUI panel has consistent shared state to work with.
 */
public class AppContext {

    private static com.quickcommerce.service.IUserService    userService;
    private static com.quickcommerce.service.IProductService productService;
    private static com.quickcommerce.service.IOrderService   orderService;
    private static com.quickcommerce.controller.UserController    userController;
    private static com.quickcommerce.controller.ProductController productController;
    private static com.quickcommerce.controller.OrderController   orderController;

    private static boolean seeded = false;

    private AppContext() {}

    // Allow external wiring (e.g. from Spring) to inject service singletons.
    public static void setUserService(com.quickcommerce.service.IUserService us)    { userService = us; }
    public static void setProductService(com.quickcommerce.service.IProductService ps) { productService = ps; }
    public static void setOrderService(com.quickcommerce.service.IOrderService os)  { orderService = os; }

    // -----------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------
    public static com.quickcommerce.service.IUserService    getUserService()    { return userService; }
    public static com.quickcommerce.service.IProductService getProductService() { return productService; }
    public static com.quickcommerce.service.IOrderService   getOrderService()   { return orderService; }

    // Controllers (lazy-initialized wrappers around services)
    public static com.quickcommerce.controller.UserController getUserController() {
        if (userController == null) userController = new com.quickcommerce.controller.UserController(userService);
        return userController;
    }

    public static com.quickcommerce.controller.ProductController getProductController() {
        if (productController == null) productController = new com.quickcommerce.controller.ProductController(productService);
        return productController;
    }

    public static com.quickcommerce.controller.OrderController getOrderController() {
        if (orderController == null) orderController = new com.quickcommerce.controller.OrderController(orderService);
        return orderController;
    }

    // -----------------------------------------------------------------------
    // Demo data seed
    // -----------------------------------------------------------------------
    public static void seedData() {
        if (seeded) return;
        seeded = true;

        // ---- Users ----
        Vendor vendor = new Vendor("Ravi Kumar", "ravi@freshmart.com", "vendor123", "FreshMart Groceries");
        userService.registerUser(vendor);

        Customer customer = new Customer("Priya Sharma", "priya@gmail.com", "priya123",
                "42, Anna Nagar, Chennai - 600040");
        userService.registerUser(customer);

        DeliveryPartner dp1 = new DeliveryPartner("Arjun Raj", "arjun@delivery.com", "arjun123");
        userService.registerUser(dp1);
        orderService.registerDeliveryPartner(dp1);

        DeliveryPartner dp2 = new DeliveryPartner("Kiran Mehta", "kiran@delivery.com", "kiran123");
        userService.registerUser(dp2);
        orderService.registerDeliveryPartner(dp2);

        Administrator admin = new Administrator("Alice Admin", "alice@quickcommerce.com", "admin123", "SUPER_ADMIN");
        userService.registerUser(admin);

        // ---- Products ----
        Product milk      = new Product("Full Cream Milk 1L",     "Dairy",   60.00, 100);
        Product bread     = new Product("Whole Wheat Bread",       "Bakery",  45.00,  50);
        Product eggs      = new Product("Farm Fresh Eggs (12)",    "Dairy",   89.00,  80);
        Product rice      = new Product("Basmati Rice 5kg",        "Grains", 350.00,  30);
        Product choc      = new Product("Dark Chocolate 100g",     "Snacks",  99.00,  60);
        Product butter    = new Product("Amul Butter 500g",        "Dairy",   55.00,  70);
        Product atta      = new Product("Aashirvaad Atta 10kg",    "Grains", 420.00,  25);
        Product tomato    = new Product("Tomatoes (1kg)",          "Veggies", 40.00, 120);
        Product onion     = new Product("Onions (1kg)",            "Veggies", 35.00, 150);
        Product banana    = new Product("Bananas (dozen)",         "Fruits",  60.00,  90);
        Product apple     = new Product("Royal Gala Apples (6)",   "Fruits", 120.00,  55);
        Product yogurt    = new Product("Greek Yogurt 400g",       "Dairy",   85.00,  40);

        for (Product p : new Product[]{milk, bread, eggs, rice, choc, butter, atta, tomato, onion, banana, apple, yogurt}) {
            vendor.addProduct(p);
            productService.addProduct(p);
        }

        // ---- Sample orders ----
        // Order 1 — Delivered
        customer.login("priya@gmail.com", "priya123");
        customer.addToCart(milk, 2);
        customer.addToCart(eggs, 1);
        customer.addToCart(bread, 3);
        Order o1 = customer.placeOrder();
        o1.checkout(PaymentMethod.UPI);
        vendor.processOrder(o1);
        orderService.registerOrder(o1);
        orderService.assignDeliveryPartner(o1.getOrderId());
        dp1.updateDeliveryStatus(o1.getOrderId(), com.quickcommerce.enums.OrderStatus.DELIVERED);

        // Order 2 — Out for delivery
        customer.addToCart(rice, 1);
        customer.addToCart(choc, 2);
        Order o2 = customer.placeOrder();
        o2.checkout(PaymentMethod.CREDIT_CARD);
        vendor.processOrder(o2);
        orderService.registerOrder(o2);
        orderService.assignDeliveryPartner(o2.getOrderId());

        // Order 3 — Pending
        customer.addToCart(apple, 2);
        customer.addToCart(banana, 1);
        Order o3 = customer.placeOrder();
        o3.checkout(PaymentMethod.DEBIT_CARD);
        orderService.registerOrder(o3);

        customer.logout();
    }
}
