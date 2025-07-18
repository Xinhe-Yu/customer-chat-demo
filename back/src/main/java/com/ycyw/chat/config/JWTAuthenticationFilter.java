package com.ycyw.chat.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ycyw.chat.services.AgentDetailsService;
import com.ycyw.chat.services.ClientDetailsService;
import com.ycyw.chat.services.JWTService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
  @Autowired
  private JWTService jwtService;

  @Autowired
  private ClientDetailsService clientDetailsService;

  @Autowired
  private AgentDetailsService agentDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String path = request.getRequestURI();
    if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = extractToken(request);

    if (token != null && jwtService.isValidToken(token)) {
      String role = jwtService.getRoleFromToken(token);
      String subject = jwtService.getSubject(token); // email or agent id

      UserDetails userDetails = null;
      if ("ROLE_AGENT".equals(role)) {
        userDetails = agentDetailsService.loadUserByUsername(subject);
      } else {
        userDetails = clientDetailsService.loadUserByUsername(subject);
      }
      Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
          userDetails.getAuthorities());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
    filterChain.doFilter(request, response);
  }

  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      return header.substring(7);
    }
    return null;
  }
}
