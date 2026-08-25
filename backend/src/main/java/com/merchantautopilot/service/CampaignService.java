package com.merchantautopilot.service;

import com.merchantautopilot.domain.*;
import com.merchantautopilot.dto.AutopilotDtos.CampaignRequest;
import com.merchantautopilot.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampaignService {
  private final CampaignRepository campaigns; private final MerchantRepository merchants; private final AuditLogRepository audit;
  public CampaignService(CampaignRepository campaigns, MerchantRepository merchants, AuditLogRepository audit) { this.campaigns = campaigns; this.merchants = merchants; this.audit = audit; }
  @Transactional public Campaign create(CampaignRequest request, String email) { var merchant = merchant(email); if (request.discountPercent().compareTo(new BigDecimal("15")) > 0) throw new IllegalArgumentException("Discount exceeds the merchant guardrail"); var campaign = new Campaign(request.name(), request.type(), request.expectedRevenue(), request.discountPercent(), request.rationale(), request.confidence()); campaign.setMerchant(merchant); campaign = campaigns.save(campaign); audit.save(new AuditLog(merchant, "Merchant Autopilot", "Campaign created", request.rationale(), request.confidence(), "PENDING", "Awaiting merchant approval")); return campaign; }
  @Transactional public Campaign approve(UUID id, String email) { var campaign = campaigns.findById(id).orElseThrow(() -> new NoSuchElementException("Campaign not found")); if (!campaign.getMerchant().getEmail().equalsIgnoreCase(email)) throw new SecurityException("Campaign does not belong to merchant"); campaign.setStatus("APPROVED"); audit.save(new AuditLog(campaign.getMerchant(), email, "Campaign approved", "Merchant explicitly approved campaign", campaign.getConfidence(), "APPROVED", "Ready for executor")); return campaigns.save(campaign); }
  @Transactional(readOnly = true) public List<Campaign> list(String email) { return campaigns.findByMerchantIdOrderByCreatedAtDesc(merchant(email).getId()); }
  private Merchant merchant(String email) { return merchants.findByEmailIgnoreCase(email).orElseThrow(() -> new NoSuchElementException("Merchant not found")); }
}
