package com.merchantautopilot.repository;

import com.merchantautopilot.domain.Order;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, UUID> { @Query("select coalesce(sum(o.amount), 0) from Order o where o.merchant.id = :merchantId and o.status = 'PAID'") java.math.BigDecimal paidRevenue(@Param("merchantId") UUID merchantId); }
