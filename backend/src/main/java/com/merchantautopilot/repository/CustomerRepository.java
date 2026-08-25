package com.merchantautopilot.repository;

import com.merchantautopilot.domain.Customer;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> { List<Customer> findTop50ByMerchantIdOrderByLastPurchaseDesc(UUID merchantId); }
