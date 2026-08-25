package com.merchantautopilot.api;

import com.merchantautopilot.dto.AuthDtos.*;
import com.merchantautopilot.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
  private final AuthService auth;
  public AuthController(AuthService auth) { this.auth = auth; }
  @PostMapping("/register") public AuthResponse register(@Valid @RequestBody RegisterRequest request) { return auth.register(request); }
  @PostMapping("/login") public AuthResponse login(@Valid @RequestBody LoginRequest request) { return auth.login(request); }
}
