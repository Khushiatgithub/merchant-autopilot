package com.merchantautopilot.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {
  @Id @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;
  @Column(nullable = false, length = 120) private String name;
  @Column(nullable = false, unique = true, length = 180) private String email;
  @Column(name = "password_hash", nullable = false, length = 255) private String passwordHash;
  @Column(name = "created_at", nullable = false) private Instant createdAt = Instant.now();
  protected Merchant() {}
  public Merchant(String name, String email) { this.name = name; this.email = email; }
  public Merchant(String name, String email, String passwordHash) { this.name = name; this.email = email; this.passwordHash = passwordHash; }
  public UUID getId() { return id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getEmail() { return email; }
  public String getPasswordHash() { return passwordHash; }
  public void setEmail(String email) { this.email = email; }
  public Instant getCreatedAt() { return createdAt; }
}
