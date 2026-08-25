package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {@Index(name = "idx_orders_merchant_created", columnList = "merchant_id,created_at"), @Index(name = "idx_orders_customer_created", columnList = "customer_id,created_at"), @Index(name = "idx_orders_product", columnList = "product_id")})
public class Order {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "merchant_id", nullable = false) private Merchant merchant;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "customer_id", nullable = false) private Customer customer;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "product_id", nullable = false) private Product product;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
  @Column(nullable = false, length = 30) private String status;
  @Column(name = "payment_method", nullable = false, length = 40) private String paymentMethod;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  protected Order() {}
  public UUID getId() { return id; }
  public Customer getCustomer() { return customer; }
  public Product getProduct() { return product; }
  public BigDecimal getAmount() { return amount; }
  public String getStatus() { return status; }
  public String getPaymentMethod() { return paymentMethod; }
  public Instant getCreatedAt() { return createdAt; }
}
