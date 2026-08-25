package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaigns", indexes = @Index(name = "idx_campaigns_merchant_status", columnList = "merchant_id,status"))
public class Campaign {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "merchant_id", nullable = false) private Merchant merchant;
  @Column(nullable = false, length = 160) private String name;
  @Column(nullable = false, length = 40) private String type;
  @Column(name = "expected_revenue", nullable = false, precision = 12, scale = 2) private BigDecimal expectedRevenue = BigDecimal.ZERO;
  @Column(name = "actual_revenue", nullable = false, precision = 12, scale = 2) private BigDecimal actualRevenue = BigDecimal.ZERO;
  @Column(name = "discount_percent", nullable = false, precision = 5, scale = 2) private BigDecimal discountPercent = BigDecimal.ZERO;
  @Column(nullable = false, length = 30) private String status = "PENDING_APPROVAL";
  @Column(columnDefinition = "TEXT") private String rationale;
  @Column(precision = 5, scale = 2) private BigDecimal confidence;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  protected Campaign() {}
  public void setStatus(String status) { this.status = status; }
  public Campaign(String name, String type, BigDecimal expectedRevenue, BigDecimal discountPercent, String rationale, BigDecimal confidence) {
    this.name = name;
    this.type = type;
    this.expectedRevenue = expectedRevenue;
    this.discountPercent = discountPercent;
    this.rationale = rationale;
    this.confidence = confidence;
  }
  public void setMerchant(Merchant merchant) { this.merchant = merchant; }
  public UUID getId() { return id; }
  public Merchant getMerchant() { return merchant; }
  public String getName() { return name; }
  public String getType() { return type; }
  public String getStatus() { return status; }
  public BigDecimal getExpectedRevenue() { return expectedRevenue; }
  public BigDecimal getActualRevenue() { return actualRevenue; }
  public BigDecimal getDiscountPercent() { return discountPercent; }
  public BigDecimal getConfidence() { return confidence; }
  public void addRevenue(BigDecimal amount) { this.actualRevenue = this.actualRevenue.add(amount); this.status = "COMPLETED"; }
}
