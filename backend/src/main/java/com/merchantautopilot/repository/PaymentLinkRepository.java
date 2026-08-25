package com.merchantautopilot.repository;

import com.merchantautopilot.domain.PaymentLink;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentLinkRepository extends JpaRepository<PaymentLink, UUID> { Optional<PaymentLink> findByRazorpayLinkId(String razorpayLinkId); }
