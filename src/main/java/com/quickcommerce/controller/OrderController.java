package com.quickcommerce.controller;

import java.util.List;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Order;
import com.quickcommerce.service.IOrderService;

/**
 * OrderController — coordinates order-related UI requests.
 *
 * GRASP: Controller delegates orchestration to the Information Expert (`IOrderService`).
 * SOLID: - SRP: only routes UI actions to the domain service.
 *        - DIP: depends on `IOrderService` abstraction.
 */
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    public void registerOrder(Order o) { orderService.registerOrder(o); }

    public List<Order> getAllOrders() { return orderService.getAllOrders(); }

    public void assignDeliveryPartner(String orderId) { orderService.assignDeliveryPartner(orderId); }

    public void registerDeliveryPartner(DeliveryPartner dp) { orderService.registerDeliveryPartner(dp); }

    public void updateOrderStatus(String orderId, OrderStatus ns) { orderService.updateOrderStatus(orderId, ns); }
}
