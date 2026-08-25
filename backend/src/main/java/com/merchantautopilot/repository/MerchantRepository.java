package com.merchantautopilot.repository;

import com.merchantautopilot.domain.Merchant;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MerchantRepository extends JpaRepository<Merchant, UUID> { Optional<Merchant> findByEmailIgnoreCase(String email); }
