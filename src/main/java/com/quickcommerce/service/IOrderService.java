package com.quickcommerce.service;

import java.util.List;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Order;

/**
 * Abstraction for order operations used by controllers.
 */
public interface IOrderService {
    void registerOrder(Order order);
    List<Order> getAllOrders();
    void assignDeliveryPartner(String orderId);
    void registerDeliveryPartner(DeliveryPartner dp);
    void updateOrderStatus(String orderId, OrderStatus ns);
}
