package com.merchantautopilot.dto;

import jakarta.validation.constraints.*;

public final class AuthDtos {
  private AuthDtos() {}
  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
  public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min=8) String password, @NotBlank String merchantName) {}
  public record AuthResponse(String token, String merchantName, String email) {}
}
