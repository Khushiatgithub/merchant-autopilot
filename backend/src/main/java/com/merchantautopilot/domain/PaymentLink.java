package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_links", indexes = @Index(name = "idx_payment_links_status", columnList = "status"))
public class PaymentLink {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "campaign_id") private Campaign campaign;
  @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id") private Order order;
  @Column(name = "razorpay_link_id", unique = true, length = 120) private String razorpayLinkId;
  @Column(name = "short_url") private String shortUrl;
  @Column(nullable = false, length = 30) private String status = "CREATED";
  @Column(name = "retry_count", nullable = false) private int retryCount;
  @Column(name = "verified_at") private Instant verifiedAt;
  protected PaymentLink() {}
  public PaymentLink(Campaign campaign, String razorpayLinkId, String shortUrl) { this.campaign = campaign; this.razorpayLinkId = razorpayLinkId; this.shortUrl = shortUrl; }
  public void markPaid() { this.status = "PAID"; this.verifiedAt = Instant.now(); }
  public UUID getId() { return id; }
  public String getRazorpayLinkId() { return razorpayLinkId; }
  public String getShortUrl() { return shortUrl; }
  public String getStatus() { return status; }
  public Campaign getCampaign() { return campaign; }
  public int getRetryCount() { return retryCount; }
  public Instant getVerifiedAt() { return verifiedAt; }
}
