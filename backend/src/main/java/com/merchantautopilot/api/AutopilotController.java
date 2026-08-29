package com.merchantautopilot.api;

import com.merchantautopilot.domain.AuditLog;
import com.merchantautopilot.domain.Campaign;
import com.merchantautopilot.dto.AutopilotDtos.*;
import com.merchantautopilot.repository.*;
import com.merchantautopilot.service.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api") @CrossOrigin(origins = "*")
public class AutopilotController {
  private final CampaignService campaigns; private final RazorpayService razorpay; private final CustomerRepository customers; private final OrderRepository orders; private final AuditLogRepository audit; private final MerchantRepository merchants; private final AiPlannerService planner; private final PaymentService payments;
  public AutopilotController(CampaignService campaigns, RazorpayService razorpay, CustomerRepository customers, OrderRepository orders, AuditLogRepository audit, MerchantRepository merchants, AiPlannerService planner, PaymentService payments) { this.campaigns = campaigns; this.razorpay = razorpay; this.customers = customers; this.orders = orders; this.audit = audit; this.merchants = merchants; this.planner = planner; this.payments = payments; }
  @PostMapping("/goals") public Map<String,Object> createPlan(@Valid @RequestBody GoalRequest request) { return planner.createPlan(request.revenueGoal(), request.maxDiscount(), request.minMargin()); }
  @PostMapping("/campaigns") public Campaign create(@Valid @RequestBody CampaignRequest request) { return campaigns.create(request, email()); }
  @GetMapping("/campaigns") public List<Campaign> listCampaigns() { return campaigns.list(email()); }
  @PostMapping("/campaigns/{id}/approve") public Map<String, Object> approve(@PathVariable UUID id) { return campaigns.approve(id, email()); }
  @PostMapping("/payments/create-link") public Map<String,Object> createLink(@Valid @RequestBody PaymentRequest request) { return payments.create(request); }
  @PostMapping("/payments/verify") public Map<String,Object> verify(@Valid @RequestBody VerifyPaymentRequest request) { return payments.verify(request); }
  @PostMapping("/payments/failure") public Map<String,Object> failedPayment(@RequestBody Map<String,Object> payload) {
    var campaignId = payload.get("campaignId") == null ? campaigns.list(email()).stream().findFirst().map(Campaign::getId).orElseThrow(() -> new NoSuchElementException("No campaign found")) : UUID.fromString(String.valueOf(payload.get("campaignId")));
    var campaign = campaigns.findById(campaignId).orElseThrow(() -> new NoSuchElementException("Campaign not found"));
    campaign.setStatus("STOPPED");
    campaigns.save(campaign);
    audit.save(new AuditLog(campaign.getMerchant(), email(), "Payment failed", "Stop rule engaged after failed payment", campaign.getConfidence(), "STOPPED", "Execution stopped after payment failure"));
    return Map.of("status", "STOPPED", "campaignId", campaignId, "message", "Stop rule engaged after failed payment.");
  }
  @PostMapping("/payments/webhook") public Map<String,Object> webhook(@RequestBody Map<String,Object> payload, @RequestHeader(value = "X-Razorpay-Signature", defaultValue = "") String signature) { payments.webhook(String.valueOf(payload.get("payment_link_id")), String.valueOf(payload.getOrDefault("status", ""))); return Map.of("received", true, "signaturePresent", !signature.isBlank()); }
  @GetMapping("/customers") public List<?> customerList() { return customers.findTop50ByMerchantIdOrderByLastPurchaseDesc(merchantId()).stream().map(customer -> Map.of("id", customer.getId(), "name", customer.getName(), "email", customer.getEmail(), "segment", customer.getSegment(), "avgOrderValue", customer.getAvgOrderValue(), "favoriteCategory", customer.getFavoriteCategory(), "purchaseFrequency", customer.getPurchaseFrequency(), "lastPurchase", customer.getLastPurchase(), "preferredHour", customer.getPreferredHour())).toList(); }
  @GetMapping("/customers/{id}") public Map<String,Object> customerDetail(@PathVariable UUID id) { var customer = customers.findById(id).orElseThrow(() -> new NoSuchElementException("Customer not found")); var digitalTwin = digitalTwin(customer); var payload = new LinkedHashMap<String,Object>(); payload.put("id", customer.getId()); payload.put("name", customer.getName()); payload.put("email", customer.getEmail()); payload.put("segment", customer.getSegment()); payload.put("avgOrderValue", customer.getAvgOrderValue()); payload.put("favoriteCategory", customer.getFavoriteCategory()); payload.put("purchaseFrequency", customer.getPurchaseFrequency()); payload.put("lastPurchase", customer.getLastPurchase()); payload.put("preferredHour", customer.getPreferredHour()); payload.put("nextPredictedProduct", digitalTwin.get("nextPredictedProduct")); payload.put("confidenceScore", digitalTwin.get("confidenceScore")); return payload; }
  @GetMapping("/dashboard") public Map<String,Object> dashboard() {
    var merchant = merchants.findByEmailIgnoreCase(email()).orElseThrow(() -> new NoSuchElementException("Merchant not found"));
    var revenue = orders.paidRevenue(merchant.getId());
    var campaignRevenue = campaigns.list(email()).stream().map(Campaign::getActualRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
    return Map.of("revenue", revenue.add(campaignRevenue), "activeCustomers", customers.count(), "goal", 200000, "conversionRate", 8.42);
  }
  private Map<String,Object> digitalTwin(com.merchantautopilot.domain.Customer customer) {
    var category = Optional.ofNullable(customer.getFavoriteCategory()).orElse("Footwear");
    var product = switch (category) {
      case "Footwear" -> "Running Socks";
      case "Activewear" -> "Performance Tee";
      case "Hydration" -> "Hydration Bottle";
      case "Accessories" -> "Premium Cap";
      default -> "Performance Running Socks";
    };
    var score = Math.min(96, Math.max(71, 72 + customer.getPurchaseFrequency().doubleValue() * 4 + (customer.getAvgOrderValue().doubleValue() > 3000 ? 9 : 5)));
    return Map.of("nextPredictedProduct", product, "confidenceScore", Math.round(score));
  }
  @GetMapping("/audit") public List<?> auditList() { return audit.findTop100ByMerchantIdOrderByCreatedAtDesc(merchantId()); }
  private String email() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
  private UUID merchantId() { return merchants.findByEmailIgnoreCase(email()).orElseThrow(() -> new NoSuchElementException("Merchant not found")).getId(); }
}
