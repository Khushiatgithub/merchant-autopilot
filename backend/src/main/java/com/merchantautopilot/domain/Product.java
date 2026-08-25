package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_products_merchant_category", columnList = "merchant_id,category"))
public class Product {
  @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
  @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "merchant_id", nullable = false) private Merchant merchant;
  @Column(nullable = false, length = 160) private String name;
  @Column(nullable = false, length = 80) private String category;
  @Column(nullable = false, precision = 12, scale = 2) private BigDecimal price;
  @Column(name = "margin_percent", nullable = false, precision = 5, scale = 2) private BigDecimal marginPercent;
  @Column(name = "stock_count", nullable = false) private int stockCount;
  protected Product() {}
  public UUID getId() { return id; }
  public String getName() { return name; }
  public String getCategory() { return category; }
  public BigDecimal getPrice() { return price; }
  public BigDecimal getMarginPercent() { return marginPercent; }
  public int getStockCount() { return stockCount; }
}
