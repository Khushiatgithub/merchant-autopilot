package com.merchantautopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merchantautopilot.domain.PaymentLink;
import com.merchantautopilot.dto.AutopilotDtos.*;
import com.merchantautopilot.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {
  private final RazorpayService razorpay; private final PaymentLinkRepository links; private final CampaignRepository campaigns; private final AuditLogRepository audit; private final ObjectMapper json;
  public PaymentService(RazorpayService razorpay, PaymentLinkRepository links, CampaignRepository campaigns, AuditLogRepository audit, ObjectMapper json) { this.razorpay = razorpay; this.links = links; this.campaigns = campaigns; this.audit = audit; this.json = json; }
  @Transactional public Map<String,Object> create(PaymentRequest request) {
    var response = razorpay.createTestModeLink(request);
    if (!"CREATED".equals(response.get("status"))) return response;
    try { var campaign = campaigns.findById(request.campaignId()).orElseThrow(); JsonNode raw = json.readTree((String) response.get("rawResponse")); String id = raw.path("id").asText(); String url = raw.path("short_url").asText(); var link = links.save(new PaymentLink(campaign, id, url)); audit.save(new com.merchantautopilot.domain.AuditLog(campaign.getMerchant(), "Executor Agent", "Link created", "Approved campaign payment link created", campaign.getConfidence(), "APPROVED", id)); return Map.of("status", "CREATED", "testMode", true, "paymentLinkId", id, "shortUrl", url, "orderId", link.getId().toString()); } catch (Exception exception) { throw new IllegalStateException("Could not persist Razorpay payment link", exception); }
  }
  @Transactional public Map<String,Object> verify(VerifyPaymentRequest request) { var link = links.findByRazorpayLinkId(request.paymentLinkId()).orElseThrow(() -> new NoSuchElementException("Payment link not found")); if (!razorpay.verifySignature(request.paymentLinkId(), request.signature())) throw new SecurityException("Invalid Razorpay signature"); link.markPaid(); link.getCampaign().addRevenue(link.getCampaign().getExpectedRevenue()); links.save(link); campaigns.save(link.getCampaign()); return Map.of("verified", true, "status", "PAID", "orderId", link.getId().toString(), "revenueUpdated", true); }
  @Transactional public void webhook(String linkId, String status) { links.findByRazorpayLinkId(linkId).ifPresent(link -> { if ("paid".equalsIgnoreCase(status)) { link.markPaid(); link.getCampaign().addRevenue(link.getCampaign().getExpectedRevenue()); links.save(link); campaigns.save(link.getCampaign()); } }); }
  @Transactional public Map<String, Object> expire(UUID linkId) { var link = links.findById(linkId).orElseThrow(() -> new NoSuchElementException("Payment link not found")); var campaign = link.getCampaign(); link.markFailed(); audit.save(new com.merchantautopilot.domain.AuditLog(campaign.getMerchant(), "Razorpay", "Payment expired", "Payment link expired without payment", campaign.getConfidence(), "APPROVED", "FAILED")); if (link.getRetryCount() == 0) { link.incrementRetry(); audit.save(new com.merchantautopilot.domain.AuditLog(campaign.getMerchant(), "Executor Agent", "Retry initiated", "One retry allowed after first expiration", campaign.getConfidence(), "APPROVED", "RETRY_REQUIRED")); links.save(link); return Map.of("status", "RETRY_REQUIRED", "retryCount", link.getRetryCount(), "reason", "First expiration; one retry initiated"); } audit.save(new com.merchantautopilot.domain.AuditLog(campaign.getMerchant(), "Executor Agent", "Retry failed", "Second payment attempt expired", campaign.getConfidence(), "APPROVED", "FAILED")); audit.save(new com.merchantautopilot.domain.AuditLog(campaign.getMerchant(), "Executor Agent", "Campaign stopped", "Payment failed twice; execution stopped", campaign.getConfidence(), "APPROVED", "STOPPED")); links.save(link); return Map.of("status", "STOPPED", "retryCount", link.getRetryCount(), "reason", "Second expiration; campaign stopped"); }
}
