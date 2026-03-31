package com.quickcommerce.model;

import com.quickcommerce.enums.PaymentMethod;
import com.quickcommerce.enums.PaymentStatus;
import com.quickcommerce.exception.InvalidOperationException;
import com.quickcommerce.interfaces.Processable;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a Payment transaction associated with an {@link Order}.
 *
 * <p>Implements {@link Processable} — concrete processing and validation
 * logic live here rather than in the Order, respecting the Single
 * Responsibility Principle (SRP).</p>
 *
 * <p>Relationship summary (from UML):
 * <ul>
 *   <li>Order → Payment : 1-to-1 composition (diamond on Order side)</li>
 * </ul>
 * </p>
 */
public class Payment implements Processable {

    // -----------------------------------------------------------------------
    // Fields
    // -----------------------------------------------------------------------

    private final String        paymentId;
    private final double        amount;
    private final PaymentMethod paymentMethod;
    private       PaymentStatus paymentStatus;
    private final LocalDateTime createdAt;
    private       LocalDateTime processedAt;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Creates a new Payment in {@link PaymentStatus#PENDING} state.
     *
     * @param amount        the amount to be charged (must be > 0)
     * @param paymentMethod the method of payment
     */
    public Payment(double amount, PaymentMethod paymentMethod) {
        if (amount <= 0)
            throw new InvalidOperationException("Payment amount must be greater than zero.");

        this.paymentId     = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.amount        = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = PaymentStatus.PENDING;
        this.createdAt     = LocalDateTime.now();
    }

    // -----------------------------------------------------------------------
    // Processable implementation
    // -----------------------------------------------------------------------

    /**
     * Validates the payment before processing.
     * In a real system this would call a payment gateway validation.
     *
     * @return {@code true} if amount is positive and status is PENDING
     */
    @Override
    public boolean validate() {
        boolean valid = amount > 0 && paymentStatus == PaymentStatus.PENDING;
        System.out.println("[PAYMENT] Validation " + (valid ? "passed" : "failed")
                + " for payment " + paymentId);
        return valid;
    }

    /**
     * Processes the payment. Simulates a gateway call.
     * Sets status to {@link PaymentStatus#SUCCESS} on success.
     *
     * @return {@code true} if the payment was processed successfully
     */
    @Override
    public boolean process() {
        if (!validate()) {
            System.out.println("[PAYMENT] Cannot process — validation failed.");
            return false;
        }

        // ---- Simulate gateway call ----------------------------------------
        // In production, replace this block with a real payment-gateway API.
        boolean gatewaySuccess = simulateGateway();
        // -------------------------------------------------------------------

        if (gatewaySuccess) {
            this.paymentStatus = PaymentStatus.SUCCESS;
            this.processedAt   = LocalDateTime.now();
            System.out.printf("[PAYMENT] Payment %s processed. Amount: INR %.2f via %s%n",
                    paymentId, amount, paymentMethod.getDisplayName());
        } else {
            this.paymentStatus = PaymentStatus.FAILED;
            System.out.println("[PAYMENT] Payment " + paymentId + " FAILED.");
        }
        return gatewaySuccess;
    }

    /**
     * Validates the payment (alias kept for UML method naming).
     * Delegates to {@link #validate()}.
     *
     * @return {@code true} if payment details are valid
     */
    public boolean validatePayment() {
        return validate();
    }

    /**
     * Processes the payment (alias kept for UML method naming).
     * Delegates to {@link #process()}.
     *
     * @return {@code true} if payment succeeded
     */
    public boolean processPayment() {
        return process();
    }

    /**
     * Issues a refund, transitioning status to {@link PaymentStatus#REFUNDED}.
     */
    public void refund() {
        if (paymentStatus != PaymentStatus.SUCCESS) {
            throw new InvalidOperationException("Only successful payments can be refunded.");
        }
        this.paymentStatus = PaymentStatus.REFUNDED;
        System.out.println("[PAYMENT] Payment " + paymentId + " refunded.");
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    /**
     * Stub simulating a payment-gateway response.
     * Replace with real gateway SDK calls in production.
     */
    private boolean simulateGateway() {
        // Always succeeds in this simulation
        return true;
    }

    // -----------------------------------------------------------------------
    // Getters (no setters — Payment is largely immutable after creation)
    // -----------------------------------------------------------------------

    public String        getPaymentId()     { return paymentId; }
    public double        getAmount()        { return amount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public LocalDateTime getProcessedAt()   { return processedAt; }

    // -----------------------------------------------------------------------
    // Object overrides
    // -----------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("Payment{id='%s', amount=INR %.2f, method=%s, status=%s}",
                paymentId, amount, paymentMethod, paymentStatus);
    }
}
