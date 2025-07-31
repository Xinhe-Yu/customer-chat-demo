package com.ycyw.chat.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ycyw.chat.dto.request.LoginDto;
import com.ycyw.chat.dto.response.ApiResponseDto;
import com.ycyw.chat.dto.response.ErrorResponseDto;
import com.ycyw.chat.dto.response.TokenResponseDto;
import com.ycyw.chat.services.AgentDetails;
import com.ycyw.chat.services.JWTService;

@RestController
@RequestMapping("/api/agent")
public class AgentController {

  private final AuthenticationManager authenticationManager;
  private final JWTService jwtService;

  public AgentController(
      AuthenticationManager authenticationManager,
      JWTService jwtService) {
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
  }

  @PostMapping("/auth")
  public ResponseEntity<ApiResponseDto> loginUser(@RequestBody LoginDto loginDto) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginDto.getIdentifier(),
              loginDto.getPassword()));

      AgentDetails agentDetails = (AgentDetails) authentication.getPrincipal();
      String token = jwtService.generateAgentToken(agentDetails);
      TokenResponseDto response = new TokenResponseDto(token);
      return ResponseEntity.ok(response);
    } catch (AuthenticationException e) {
      ErrorResponseDto response = new ErrorResponseDto("Échec authentication");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
  }

}
