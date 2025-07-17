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

import com.ycyw.chat.services.JWTService;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor {
  private final JWTService jwtService;

  public AuthChannelInterceptor(JWTService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    if (accessor == null) {
      throw new IllegalArgumentException("Message is not a STOMP message");
    }
    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");
      if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7);
        if (jwtService.isValidToken(token)) {
          String emailOrId = jwtService.getSubject(token);
          String role = jwtService.getRoleFromToken(token);

          List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
          UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(emailOrId, null,
              authorities);

          accessor.setUser(auth); // ✅ Attach to session Principal
        } else {
          throw new IllegalArgumentException("Invalid JWT");
        }
      } else {
        throw new IllegalArgumentException("Missing JWT");
      }
    }
    return message;
  }

}
