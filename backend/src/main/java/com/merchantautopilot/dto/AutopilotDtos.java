package com.merchantautopilot.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

public final class AutopilotDtos {
  private AutopilotDtos() {}
  public record GoalRequest(@Positive BigDecimal revenueGoal, @DecimalMin("0") @DecimalMax("100") BigDecimal maxDiscount, @DecimalMin("0") @DecimalMax("100") BigDecimal minMargin) {}
  public record CampaignRequest(@NotBlank String name, @NotBlank String type, @Positive BigDecimal expectedRevenue, @DecimalMin("0") @DecimalMax("100") BigDecimal discountPercent, @Size(max=2000) String rationale, @DecimalMin("0") @DecimalMax("100") BigDecimal confidence) {}
  public record PaymentRequest(@NotNull UUID campaignId, @Positive long amountPaise, @NotBlank String description) {}
  public record VerifyPaymentRequest(@NotBlank String paymentLinkId, @NotBlank String signature) {}
}
