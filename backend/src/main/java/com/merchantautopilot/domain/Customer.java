package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customers", indexes = {@Index(name = "idx_customers_merchant_segment", columnList = "merchant_id,segment"), @Index(name = "idx_customers_last_purchase", columnList = "last_purchase")})
public class Customer {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "merchant_id", nullable = false) private Merchant merchant;
  @Column(nullable = false, length = 120) private String name;
  @Column(nullable = false, length = 180) private String email;
  @Column(nullable = false, length = 40) private String segment;
  @Column(name = "avg_order_value", nullable = false, precision = 12, scale = 2) private BigDecimal avgOrderValue;
  @Column(name = "favorite_category", nullable = false, length = 80) private String favoriteCategory;
  @Column(name = "purchase_frequency", nullable = false, precision = 8, scale = 2) private BigDecimal purchaseFrequency;
  @Column(name = "last_purchase") private Instant lastPurchase;
  @Column(name = "preferred_hour", nullable = false) private short preferredHour;
  protected Customer() {}
  public UUID getId() { return id; }
  public Merchant getMerchant() { return merchant; }
  public String getName() { return name; }
  public String getEmail() { return email; }
  public String getSegment() { return segment; }
  public BigDecimal getAvgOrderValue() { return avgOrderValue; }
  public String getFavoriteCategory() { return favoriteCategory; }
  public BigDecimal getPurchaseFrequency() { return purchaseFrequency; }
  public Instant getLastPurchase() { return lastPurchase; }
  public short getPreferredHour() { return preferredHour; }
}
