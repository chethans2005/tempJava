package com.quickcommerce.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.quickcommerce.enums.OrderStatus;
import com.quickcommerce.exception.ResourceNotFoundException;
import com.quickcommerce.model.DeliveryPartner;
import com.quickcommerce.model.Order;
import com.quickcommerce.model.OrderItem;
import com.quickcommerce.model.Payment;
import com.quickcommerce.persistence.entity.OrderEntity;
import com.quickcommerce.persistence.entity.OrderItemEntity;
import com.quickcommerce.persistence.entity.PaymentEntity;
import com.quickcommerce.persistence.repo.OrderItemRepository;
import com.quickcommerce.persistence.repo.OrderRepository;
import com.quickcommerce.persistence.repo.PaymentRepository;

/**
 * OrderService supports optional persistence via repositories; otherwise
 * it operates in-memory as before. Persisted fields are written on
 * register/update operations (no eager rehydrate of existing orders).
 */
/**
 * OrderService — handles order lifecycle and partner assignment.
 *
 * GRASP: Controller delegates orchestration to this Information Expert.
 * SOLID: - SRP: focuses on order domain logic; persistence mapping kept local but
 *   could be moved to mappers for clearer separation.
 *        - DIP: implements `IOrderService` so callers can depend on an abstraction.
 */
public class OrderService implements IOrderService {

    private final List<Order> allOrders;
    private final List<DeliveryPartner> deliveryPartners;

    private final OrderRepository orderRepository; // nullable
    private final OrderItemRepository orderItemRepository; // nullable
    private final PaymentRepository paymentRepository; // nullable

    public OrderService() {
        this.allOrders = new ArrayList<>();
        this.deliveryPartners = new ArrayList<>();
        this.orderRepository = null;
        this.orderItemRepository = null;
        this.paymentRepository = null;
    }

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        PaymentRepository paymentRepository) {
        this.allOrders = new ArrayList<>();
        this.deliveryPartners = new ArrayList<>();
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void registerOrder(Order order) {
        allOrders.add(order);
        if (orderRepository != null) {
            try {
                // persist payment first if present
                Payment p = order.getPayment();
                if (p != null && paymentRepository != null) {
                    PaymentEntity pe = toPaymentEntity(p);
                    paymentRepository.save(pe);
                }

                OrderEntity oe = toOrderEntity(order);
                orderRepository.save(oe);

                // persist order items
                if (orderItemRepository != null) {
                    for (OrderItem oi : order.getOrderItems()) {
                        OrderItemEntity oie = toOrderItemEntity(order.getOrderId(), oi);
                        orderItemRepository.save(oie);
                    }
                }
            } catch (Exception ex) {
                System.out.println("[ORDER-SERVICE] Warning: failed to persist order: " + ex.getMessage());
            }
        }
        System.out.println("[ORDER-SERVICE] Order " + order.getOrderId() + " registered.");
    }

    @Override
    public void registerDeliveryPartner(DeliveryPartner partner) {
        deliveryPartners.add(partner);
        System.out.println("[ORDER-SERVICE] Delivery partner '" + partner.getName() + "' registered.");
    }

    @Override
    public void assignDeliveryPartner(String orderId) {
        Order order = findOrderById(orderId);

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            System.out.println("[ORDER-SERVICE] Order " + orderId
                    + " cannot be dispatched — status is: " + order.getStatus());
            return;
        }

        DeliveryPartner availablePartner = deliveryPartners.stream()
                .filter(DeliveryPartner::isAvailable)
                .findFirst()
                .orElse(null);

        if (availablePartner == null) {
            System.out.println("[ORDER-SERVICE] No delivery partners available right now.");
            return;
        }

        availablePartner.assignOrder(order);
        order.setAssignedPartner(availablePartner);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);

        if (orderRepository != null) {
            try {
                OrderEntity oe = toOrderEntity(order);
                oe.setAssignedPartnerId(availablePartner.getPartnerId());
                oe.setStatus(order.getStatus().name());
                orderRepository.save(oe);
            } catch (Exception ignored) {}
        }

        System.out.println("[ORDER-SERVICE] Order " + orderId
                + " assigned to partner: " + availablePartner.getName());
    }

    public Order findOrderById(String orderId) {
        return allOrders.stream()
                .filter(o -> o.getOrderId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        List<Order> result = new ArrayList<>();
        for (Order o : allOrders) {
            if (o.getStatus() == status) result.add(o);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = findOrderById(orderId);
        order.setStatus(newStatus);
        if (orderRepository != null) {
            try {
                OrderEntity oe = toOrderEntity(order);
                oe.setStatus(newStatus.name());
                orderRepository.save(oe);
            } catch (Exception ignored) {}
        }
        System.out.println("[ORDER-SERVICE] Order " + orderId + " status → " + newStatus);
    }

    @Override
    public List<Order> getAllOrders() {
        return Collections.unmodifiableList(allOrders);
    }

    public List<DeliveryPartner> getDeliveryPartners() {
        return Collections.unmodifiableList(deliveryPartners);
    }

    // ------------------- Mapping helpers -------------------
    private OrderEntity toOrderEntity(Order o) {
        OrderEntity e = new OrderEntity();
        e.setOrderId(o.getOrderId());
        e.setOrderDate(o.getOrderDate());
        e.setStatus(o.getStatus().name());
        e.setTotalAmount(o.getTotalAmount());
        e.setCustomerId(o.getCustomer().getUserId());
        if (o.getPayment() != null) e.setPaymentId(o.getPayment().getPaymentId());
        if (o.getAssignedPartner() != null) e.setAssignedPartnerId(o.getAssignedPartner().getPartnerId());
        return e;
    }

    private OrderItemEntity toOrderItemEntity(String orderId, OrderItem oi) {
        OrderItemEntity e = new OrderItemEntity();
        e.setOrderId(orderId);
        e.setProductId(oi.getProduct().getProductId());
        e.setQuantity(oi.getQuantity());
        e.setPrice(oi.getPrice());
        return e;
    }

    private PaymentEntity toPaymentEntity(Payment p) {
        PaymentEntity e = new PaymentEntity();
        e.setPaymentId(p.getPaymentId());
        e.setAmount(p.getAmount());
        e.setPaymentMethod(p.getPaymentMethod().name());
        e.setPaymentStatus(p.getPaymentStatus().name());
        e.setCreatedAt(LocalDateTime.now());
        e.setProcessedAt(p.getProcessedAt());
        return e;
    }
}
