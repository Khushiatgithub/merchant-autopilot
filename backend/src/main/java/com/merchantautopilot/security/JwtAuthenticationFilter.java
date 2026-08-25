package com.merchantautopilot.security;

import io.jsonwebtoken.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  public JwtAuthenticationFilter(JwtService jwt) { this.jwt = jwt; }
  @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException { String header = request.getHeader("Authorization"); if (header != null && header.startsWith("Bearer ")) { try { var claims = jwt.parse(header.substring(7)); var auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null, AuthorityUtils.NO_AUTHORITIES); SecurityContextHolder.getContext().setAuthentication(auth); } catch (JwtException | IllegalArgumentException ignored) {} } chain.doFilter(request, response); }
}
