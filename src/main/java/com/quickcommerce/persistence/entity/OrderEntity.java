package com.quickcommerce.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", length = 64)
    private String orderId;

    private LocalDateTime orderDate;
    private String status;
    private double totalAmount;

    @Column(name = "customer_id", length = 64)
    private String customerId;

    @Column(name = "payment_id", length = 64)
    private String paymentId;

    @Column(name = "assigned_partner_id", length = 64)
    private String assignedPartnerId;

    public OrderEntity() {}

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getAssignedPartnerId() { return assignedPartnerId; }
    public void setAssignedPartnerId(String assignedPartnerId) { this.assignedPartnerId = assignedPartnerId; }
}
