package com.merchantautopilot.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long expirationMinutes;
  public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration-minutes}") long expirationMinutes) { key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expirationMinutes = expirationMinutes; }
  public String create(String email, UUID merchantId) { return Jwts.builder().subject(email).claim("merchantId", merchantId.toString()).issuedAt(new Date()).expiration(new Date(System.currentTimeMillis() + expirationMinutes * 60_000)).signWith(key).compact(); }
  public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
}
