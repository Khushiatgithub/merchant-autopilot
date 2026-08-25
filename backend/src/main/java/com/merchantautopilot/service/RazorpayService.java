package com.merchantautopilot.service;

import com.merchantautopilot.dto.AutopilotDtos.PaymentRequest;
import com.merchantautopilot.repository.CampaignRepository;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {
  private final String keyId; private final String keySecret; private final CampaignRepository campaigns; private final HttpClient http = HttpClient.newHttpClient();
  public RazorpayService(@Value("${razorpay.key-id:}") String keyId, @Value("${razorpay.key-secret:}") String keySecret, CampaignRepository campaigns) { this.keyId = keyId; this.keySecret = keySecret; this.campaigns = campaigns; }
  public Map<String,Object> createTestModeLink(PaymentRequest request) {
    var campaign = campaigns.findById(request.campaignId()).orElseThrow(() -> new NoSuchElementException("Campaign not found"));
    if (!"APPROVED".equals(campaign.getStatus())) throw new IllegalStateException("Campaign requires merchant approval before payment link creation");
    if (keyId.isBlank() || keySecret.isBlank()) return Map.of("status", "CONFIGURATION_REQUIRED", "requiresApproval", true, "message", "Set RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET");
    String json = "{\"amount\":" + request.amountPaise() + ",\"currency\":\"INR\",\"description\":\"" + escape(request.description()) + "\",\"reference_id\":\"" + request.campaignId() + "\",\"expire_by\":" + (System.currentTimeMillis() / 1000 + 86400) + "}";
    try { var response = http.send(HttpRequest.newBuilder(URI.create("https://api.razorpay.com/v1/payment_links")).header("Content-Type", "application/json").header("Authorization", basicAuth()).POST(HttpRequest.BodyPublishers.ofString(json)).build(), HttpResponse.BodyHandlers.ofString()); if (response.statusCode() / 100 != 2) throw new IllegalStateException("Razorpay returned HTTP " + response.statusCode()); return Map.of("status", "CREATED", "testMode", true, "rawResponse", response.body()); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new IllegalStateException("Razorpay request interrupted", exception); } catch (Exception exception) { throw new IllegalStateException("Unable to create Razorpay payment link", exception); }
  }
  public boolean verifySignature(String paymentLinkId, String signature) { if (keySecret.isBlank()) throw new IllegalStateException("RAZORPAY_KEY_SECRET is not configured"); return constantTime(hmac(paymentLinkId, keySecret), signature); }
  private String basicAuth() { return "Basic " + Base64.getEncoder().encodeToString((keyId + ":" + keySecret).getBytes(StandardCharsets.UTF_8)); }
  private String hmac(String value, String secret) { try { var mac = Mac.getInstance("HmacSHA256"); mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256")); var bytes = mac.doFinal(value.getBytes(StandardCharsets.UTF_8)); var result = new StringBuilder(); for (byte item : bytes) result.append(String.format("%02x", item)); return result.toString(); } catch (Exception exception) { throw new IllegalStateException("Unable to verify signature", exception); } }
  private boolean constantTime(String expected, String actual) { return expected.length() == actual.length() && java.security.MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8)); }
  private String escape(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
