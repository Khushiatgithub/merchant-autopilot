package com.merchantautopilot.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "campaign_targets")
public class CampaignTarget {
  @EmbeddedId private CampaignTargetId id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @MapsId("campaignId") @JoinColumn(name = "campaign_id") private Campaign campaign;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @MapsId("customerId") @JoinColumn(name = "customer_id") private Customer customer;
  protected CampaignTarget() {}
  public CampaignTarget(Campaign campaign, Customer customer) { this.campaign = campaign; this.customer = customer; this.id = new CampaignTargetId(campaign.getId(), customer.getId()); }
}
