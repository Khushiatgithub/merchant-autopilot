package com.merchantautopilot.service;

import java.math.BigDecimal;
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
    var result = client.post().uri("/plan").contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
    if (result == null) throw new IllegalStateException("AI engine returned an empty plan");
    return result;
  }
}
