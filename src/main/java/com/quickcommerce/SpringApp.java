package com.quickcommerce;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

import com.quickcommerce.gui.AppContext;
import com.quickcommerce.gui.LoginFrame;
import com.quickcommerce.persistence.repo.ProductRepository;
import com.quickcommerce.service.IOrderService;
import com.quickcommerce.service.IProductService;
import com.quickcommerce.service.IUserService;

@SpringBootApplication
public class SpringApp implements ApplicationListener<ApplicationReadyEvent> {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringApp.class);
        // Ensure AWT is not forced into headless mode so Swing UI can start
        app.setHeadless(false);
        app.run(args);
    }

    @Bean
    public IUserService userService(com.quickcommerce.persistence.repo.UserRepository userRepository) {
        return new com.quickcommerce.service.UserService(userRepository);
    }

    @Bean
    public IProductService productService(ProductRepository productRepository) {
        return new com.quickcommerce.service.ProductService(productRepository);
    }

    @Bean
    public IOrderService orderService(com.quickcommerce.persistence.repo.OrderRepository orderRepository,
                                     com.quickcommerce.persistence.repo.OrderItemRepository orderItemRepository,
                                     com.quickcommerce.persistence.repo.PaymentRepository paymentRepository) {
        return new com.quickcommerce.service.OrderService(orderRepository, orderItemRepository, paymentRepository);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // Wire Spring-managed services into the legacy AppContext used by the GUI
        IUserService us = event.getApplicationContext().getBean(IUserService.class);
        IProductService ps = event.getApplicationContext().getBean(IProductService.class);
        IOrderService os = event.getApplicationContext().getBean(IOrderService.class);

        AppContext.setUserService(us);
        AppContext.setProductService(ps);
        AppContext.setOrderService(os);

        // Seed demo data (moves seeding responsibility to AppContext)
        AppContext.seedData();

        // Launch Swing UI on EDT
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new LoginFrame(AppContext.getUserController()).setVisible(true));
    }
}
