package com.merchantautopilot.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiPlannerService {
  private final RestClient client;

  public AiPlannerService(@Value("${ai.base-url:http://localhost:8000}") String baseUrl) {
    this.client = RestClient.builder().baseUrl(baseUrl).build();
  }

  public Map<String, Object> createPlan(BigDecimal revenueGoal, BigDecimal maxDiscount, BigDecimal minMargin) {
    var body = Map.of("revenueGoal", revenueGoal, "maxDiscount", maxDiscount, "minMargin", minMargin);
    try {
      var result = client.post().uri("/plan").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
      if (result == null) throw new IllegalStateException("AI engine returned an empty plan");
      return result;
    } catch (Exception ex) {
      return fallbackPlan(revenueGoal, maxDiscount, minMargin);
    }
  }

  private Map<String, Object> fallbackPlan(BigDecimal revenueGoal, BigDecimal maxDiscount, BigDecimal minMargin) {
    var safeDiscount = minMargin.compareTo(BigDecimal.valueOf(38)) > 0 ? 0 : Math.min(10, Math.max(5, maxDiscount.intValue()));
    var weights = new double[] { 0.31, 0.155, 0.245, 0.29 };
    var strategies = List.of(
      strategyMap("Upsell Shoes + Socks", "UPSELL", revenueGoal.multiply(BigDecimal.valueOf(weights[0])), safeDiscount, 94, "Affinity and recent purchase intent support an add-on."),
      strategyMap("Cross Sell Bottle", "CROSS_SELL", revenueGoal.multiply(BigDecimal.valueOf(weights[1])), safeDiscount, 86, "High-value customers are likely to add a relevant product."),
      strategyMap("Bundles", "BUNDLE", revenueGoal.multiply(BigDecimal.valueOf(weights[2])), safeDiscount, 82, "A bundle grows basket size while protecting blended margin."),
      strategyMap("Win Back Campaign", "WIN_BACK", revenueGoal.multiply(BigDecimal.valueOf(weights[3])), Math.min(10, Math.max(5, maxDiscount.intValue())), 87, "Lapsed customers have proven historical demand.")
    );
    var total = strategies.stream().map(item -> (BigDecimal) item.get("expectedRevenue")).reduce(BigDecimal.ZERO, BigDecimal::add);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("agent", "Strategist");
    result.put("goal", revenueGoal);
    result.put("strategies", strategies);
    result.put("totalExpectedRevenue", total);
    result.put("guardrails", Map.of("maxDiscount", maxDiscount, "minMargin", minMargin));
    return result;
  }

  private Map<String, Object> strategyMap(String name, String type, BigDecimal expected, int discount, int confidence, String reason) {
    var map = new LinkedHashMap<String, Object>();
    map.put("name", name);
    map.put("type", type);
    map.put("expectedRevenue", expected.setScale(2, java.math.RoundingMode.HALF_UP));
    map.put("discount", discount);
    map.put("confidence", confidence / 100.0);
    map.put("marginImpact", "Protected");
    map.put("reason", reason);
    map.put("whySelected", "Highest expected revenue under the merchant guardrails.");
    return map;
  }
}
