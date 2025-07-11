package com.ycyw.chat.services;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JWTService {
  private JwtDecoder jwtDecoder;
  private JwtEncoder jwtEncoder;

  public JWTService(JwtDecoder jwtDecoder, JwtEncoder jwtEncoder) {
    this.jwtDecoder = jwtDecoder;
    this.jwtEncoder = jwtEncoder;
  }

  public String generateClientToken(ClientDetails clientDetails) {
    return buildToken(clientDetails.getEmail(), "ROLE_CLIENT");
  }

  public String generateAgentToken(AgentDetails agentDetails) {
    return buildToken(agentDetails.getId().toString(), "ROLE_AGENT"); // usually agent.getId() or getSecret()
  }

  private String buildToken(String subject, String role) {
    Instant now = Instant.now();
    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("self")
        .issuedAt(now)
        .expiresAt(now.plus(1, ChronoUnit.DAYS))
        .subject(subject)
        .claim("role", role)
        .build();

    JwtEncoderParameters parameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
    return jwtEncoder.encode(parameters).getTokenValue();
  }

  public String getSubject(String token) {
    return jwtDecoder.decode(token).getSubject();
  }

  public String getRoleFromToken(String token) {
    return jwtDecoder.decode(token).getClaimAsString("role");
  }

  public boolean isValidToken(String token) {
    try {
      Jwt jwt = jwtDecoder.decode(token);
      return Instant.now().isBefore(jwt.getExpiresAt());
    } catch (Exception e) {
      return false;
    }
  }
}
