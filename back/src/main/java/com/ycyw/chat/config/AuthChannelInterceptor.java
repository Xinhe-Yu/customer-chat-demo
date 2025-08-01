package com.ycyw.chat.config;

import java.util.List;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.ycyw.chat.services.AuthorizationService;
import com.ycyw.chat.services.JWTService;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
  private final JWTService jwtService;
  private final AuthorizationService authorizationService;

  public AuthChannelInterceptor(JWTService jwtService, AuthorizationService authorizationService) {
    this.jwtService = jwtService;
    this.authorizationService = authorizationService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null) {
      return message;
    }

    StompCommand command = accessor.getCommand();

    if (StompCommand.CONNECT.equals(command)) {
      handleConnect(accessor);
    } else if (StompCommand.SUBSCRIBE.equals(command)) {
      handleSubscribe(accessor);
    }

    return message;
  }

  private void handleConnect(StompHeaderAccessor accessor) {
    String token = accessor.getFirstNativeHeader("Authorization");

    if (token != null && token.startsWith("Bearer ")) {
      token = token.substring(7);
      if (jwtService.isValidToken(token)) {
        String emailOrId = jwtService.getSubject(token);
        String role = jwtService.getRoleFromToken(token);

        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(emailOrId, null,
            authorities);

        accessor.setUser(auth);
      } else {
        throw new IllegalArgumentException("Invalid JWT");
      }
    } else {
      throw new IllegalArgumentException("Missing JWT");
    }
  }

  private void handleSubscribe(StompHeaderAccessor accessor) {
    UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) accessor.getUser();
    String destination = accessor.getDestination();

    if (auth == null) {
      throw new IllegalArgumentException("User not authenticated");
    }

    if (!isAuthorizedToSubscribe(auth, destination)) {
      throw new IllegalArgumentException("Not authorized to subscribe to this topic");
    }

  }

  private boolean isAuthorizedToSubscribe(UsernamePasswordAuthenticationToken auth, String destination) {
    try {
      if (auth.getAuthorities() == null || auth.getAuthorities().isEmpty()) {
        return false;
      }

      String role = auth.getAuthorities().iterator().next().getAuthority();
      String userId = (String) auth.getPrincipal();

      if ("ROLE_AGENT".equals(role) || "AGENT".equals(role)) {
        return true;
      }

      if ("ROLE_CLIENT".equals(role) || "CLIENT".equals(role)) {
        if (destination != null && destination.startsWith("/topic/tickets/")) {
          String ticketId = destination.substring("/topic/tickets/".length());
          boolean authorized = isClientAuthorizedForTicket(userId, ticketId);
          return authorized;
        }
        return false;
      }

      return false;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isClientAuthorizedForTicket(String userId, String ticketId) {
    try {
      return authorizationService.isUserAuthorizedForTicket(userId, ticketId);
    } catch (Exception e) {
      return false;
    }
  }
}
