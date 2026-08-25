package com.merchantautopilot.repository;

import com.merchantautopilot.domain.Campaign;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> { List<Campaign> findByMerchantIdOrderByCreatedAtDesc(UUID merchantId); }
