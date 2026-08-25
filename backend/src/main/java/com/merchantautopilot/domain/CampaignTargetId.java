package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class CampaignTargetId implements Serializable {
  @Column(name = "campaign_id") private UUID campaignId;
  @Column(name = "customer_id") private UUID customerId;
  protected CampaignTargetId() {}
  public CampaignTargetId(UUID campaignId, UUID customerId) { this.campaignId = campaignId; this.customerId = customerId; }
  @Override public boolean equals(Object object) { if (this == object) return true; if (!(object instanceof CampaignTargetId other)) return false; return campaignId.equals(other.campaignId) && customerId.equals(other.customerId); }
  @Override public int hashCode() { return 31 * campaignId.hashCode() + customerId.hashCode(); }
}
