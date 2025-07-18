package com.ycyw.chat.services;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor
@Getter
public class ClientDetails implements UserDetails {
  private static final long serialVersionUID = 1L;

  private UUID id;
  private String username;
  private String email;

  @JsonIgnore
  private String password;

  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Set.of(new SimpleGrantedAuthority("ROLE_CLIENT"));
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // Default to true
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // Default to true
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // Default to true
  }

  @Override
  public boolean isEnabled() {
    return true; // Default to true
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ClientDetails user = (ClientDetails) o;
    return Objects.equals(id, user.id);
  }
}
