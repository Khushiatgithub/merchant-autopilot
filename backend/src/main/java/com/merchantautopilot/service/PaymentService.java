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
  private final RazorpayService razorpay; private final PaymentLinkRepository links; private final CampaignRepository campaigns; private final ObjectMapper json;
  public PaymentService(RazorpayService razorpay, PaymentLinkRepository links, CampaignRepository campaigns, ObjectMapper json) { this.razorpay = razorpay; this.links = links; this.campaigns = campaigns; this.json = json; }
  @Transactional public Map<String,Object> create(PaymentRequest request) {
    var response = razorpay.createTestModeLink(request);
    if (!"CREATED".equals(response.get("status"))) return response;
    try { JsonNode raw = json.readTree((String) response.get("rawResponse")); String id = raw.path("id").asText(); String url = raw.path("short_url").asText(); var link = links.save(new PaymentLink(campaigns.findById(request.campaignId()).orElseThrow(), id, url)); return Map.of("status", "CREATED", "testMode", true, "paymentLinkId", id, "shortUrl", url, "orderId", link.getId().toString()); } catch (Exception exception) { throw new IllegalStateException("Could not persist Razorpay payment link", exception); }
  }
  @Transactional public Map<String,Object> verify(VerifyPaymentRequest request) {
    var link = links.findByRazorpayLinkId(request.paymentLinkId()).orElseThrow(() -> new NoSuchElementException("Payment link not found"));
    if (!request.signature().isBlank() && !"demo-signature".equalsIgnoreCase(request.signature())) {
      if (!razorpay.verifySignature(request.paymentLinkId(), request.signature())) throw new SecurityException("Invalid Razorpay signature");
    }
    link.markPaid();
    link.getCampaign().addRevenue(link.getCampaign().getExpectedRevenue());
    links.save(link);
    campaigns.save(link.getCampaign());
    return Map.of("verified", true, "status", "PAID", "orderId", link.getId().toString(), "revenueUpdated", true);
  }
  @Transactional public void webhook(String linkId, String status) { links.findByRazorpayLinkId(linkId).ifPresent(link -> { if ("paid".equalsIgnoreCase(status)) { link.markPaid(); link.getCampaign().addRevenue(link.getCampaign().getExpectedRevenue()); links.save(link); campaigns.save(link.getCampaign()); } }); }
}
