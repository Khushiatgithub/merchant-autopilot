package com.merchantautopilot.service;

import com.merchantautopilot.domain.Merchant;
import com.merchantautopilot.dto.AuthDtos.*;
import com.merchantautopilot.repository.MerchantRepository;
import com.merchantautopilot.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final MerchantRepository merchants; private final PasswordEncoder encoder; private final JwtService jwt;
  public AuthService(MerchantRepository merchants, PasswordEncoder encoder, JwtService jwt) { this.merchants = merchants; this.encoder = encoder; this.jwt = jwt; }
  @Transactional public AuthResponse register(RegisterRequest request) { if (merchants.findByEmailIgnoreCase(request.email()).isPresent()) throw new IllegalArgumentException("Email already registered"); var merchant = merchants.save(new Merchant(request.merchantName(), request.email(), encoder.encode(request.password()))); return response(merchant); }
  @Transactional(readOnly = true) public AuthResponse login(LoginRequest request) { var merchant = merchants.findByEmailIgnoreCase(request.email()).orElseThrow(() -> new IllegalArgumentException("Invalid credentials")); if (!encoder.matches(request.password(), merchant.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials"); return response(merchant); }
  private AuthResponse response(Merchant merchant) { return new AuthResponse(jwt.create(merchant.getEmail(), merchant.getId()), merchant.getName(), merchant.getEmail()); }
}
