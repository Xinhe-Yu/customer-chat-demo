package com.ycyw.chat.config;

import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.ycyw.chat.services.AgentDetailsService;
import com.ycyw.chat.services.ClientDetailsService;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
@EnableWebSecurity
public class SpringSecurityConfig {
  private final String jwtKey;

  public SpringSecurityConfig() {
    Dotenv dotenv = Dotenv.load();
    this.jwtKey = dotenv.get("JWT_KEY");
    if (this.jwtKey == null || this.jwtKey.trim().isEmpty()) {
      throw new IllegalStateException("JWT_KEY environment variable is not set or is empty");
    }
  }

  @Bean
  public JWTAuthenticationFilter jwtAuthenticationFilter() {
    return new JWTAuthenticationFilter();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable())
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/ws/**").permitAll()
            .requestMatchers("/api/auth/login", "/api/auth/me", "/api/agent/auth").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .anyRequest().authenticated())
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .build();
  }

  @Bean
  public JwtDecoder jwtDecoder() {
    SecretKeySpec secretKey = new SecretKeySpec(this.jwtKey.getBytes(), 0, this.jwtKey.getBytes().length, "HmacSHA256");
    return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
  }

  @Bean
  public JwtEncoder jwtEncoder() {
    return new NimbusJwtEncoder(new ImmutableSecret<>(this.jwtKey.getBytes()));
  }

  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.addAllowedOrigin("http://localhost:4200"); // Angular dev server
    configuration.addAllowedOrigin("http://localhost:80"); // Docker frontend
    configuration.addAllowedOrigin("http://localhost"); // Docker frontend (no port)
    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationManager authenticationManager(ClientDetailsService clientDetailsService,
      AgentDetailsService agentDetailsService,
      BCryptPasswordEncoder passwordEncoder) {
    DaoAuthenticationProvider clientProvider = new DaoAuthenticationProvider(clientDetailsService);
    clientProvider.setPasswordEncoder(passwordEncoder);

    DaoAuthenticationProvider agentProvider = new DaoAuthenticationProvider(agentDetailsService);
    agentProvider.setPasswordEncoder(passwordEncoder);

    return new ProviderManager(List.of(clientProvider, agentProvider));
  }
}
