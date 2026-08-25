package com.merchantautopilot.api;

import com.merchantautopilot.domain.Campaign;
import com.merchantautopilot.dto.AutopilotDtos.*;
import com.merchantautopilot.repository.*;
import com.merchantautopilot.service.*;
import jakarta.validation.Valid;
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
  @PostMapping("/campaigns/{id}/approve") public Campaign approve(@PathVariable UUID id) { return campaigns.approve(id, email()); }
  @PostMapping("/payments/create-link") public Map<String,Object> createLink(@Valid @RequestBody PaymentRequest request) { return payments.create(request); }
  @PostMapping("/payments/verify") public Map<String,Object> verify(@Valid @RequestBody VerifyPaymentRequest request) { return payments.verify(request); }
  @PostMapping("/payments/webhook") public Map<String,Object> webhook(@RequestBody Map<String,Object> payload, @RequestHeader(value = "X-Razorpay-Signature", defaultValue = "") String signature) { payments.webhook(String.valueOf(payload.get("payment_link_id")), String.valueOf(payload.getOrDefault("status", ""))); return Map.of("received", true, "signaturePresent", !signature.isBlank()); }
  @GetMapping("/customers") public List<?> customerList() { return customers.findTop50ByMerchantIdOrderByLastPurchaseDesc(merchantId()).stream().map(customer -> Map.of("id", customer.getId(), "name", customer.getName(), "email", customer.getEmail(), "segment", customer.getSegment(), "avgOrderValue", customer.getAvgOrderValue(), "favoriteCategory", customer.getFavoriteCategory(), "purchaseFrequency", customer.getPurchaseFrequency(), "lastPurchase", customer.getLastPurchase(), "preferredHour", customer.getPreferredHour())).toList(); }
  @GetMapping("/dashboard") public Map<String,Object> dashboard() { return Map.of("revenue", orders.paidRevenue(merchantId()), "activeCustomers", customers.count(), "goal", 200000, "conversionRate", 8.42); }
  @GetMapping("/audit") public List<?> auditList() { return audit.findTop100ByMerchantIdOrderByCreatedAtDesc(merchantId()); }
  private String email() { return SecurityContextHolder.getContext().getAuthentication().getName(); }
  private UUID merchantId() { return merchants.findByEmailIgnoreCase(email()).orElseThrow(() -> new NoSuchElementException("Merchant not found")).getId(); }
}
