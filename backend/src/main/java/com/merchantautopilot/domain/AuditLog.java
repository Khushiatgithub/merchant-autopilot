package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = @Index(name = "idx_audit_merchant_created", columnList = "merchant_id,created_at"))
public class AuditLog {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "merchant_id", nullable = false) private Merchant merchant;
  @Column(nullable = false, length = 80) private String actor;
  @Column(nullable = false, length = 120) private String action;
  @Column(columnDefinition = "TEXT") private String reason;
  @Column(precision = 5, scale = 2) private BigDecimal confidence;
  @Column(length = 30) private String approval;
  @Column(columnDefinition = "TEXT") private String result;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  protected AuditLog() {}
  public AuditLog(Merchant merchant, String actor, String action, String reason, BigDecimal confidence, String approval, String result) { this.merchant = merchant; this.actor = actor; this.action = action; this.reason = reason; this.confidence = confidence; this.approval = approval; this.result = result; }
  public UUID getId() { return id; }
  public String getActor() { return actor; }
  public String getAction() { return action; }
  public String getReason() { return reason; }
  public BigDecimal getConfidence() { return confidence; }
  public String getApproval() { return approval; }
  public String getResult() { return result; }
  public Instant getCreatedAt() { return createdAt; }
}
