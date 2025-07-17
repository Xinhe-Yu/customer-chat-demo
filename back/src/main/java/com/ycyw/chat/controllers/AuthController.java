package com.ycyw.chat.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ycyw.chat.dto.ClientDTO;
import com.ycyw.chat.dto.request.LoginDto;
import com.ycyw.chat.dto.response.ApiResponseDto;
import com.ycyw.chat.dto.response.ErrorResponseDto;
import com.ycyw.chat.dto.response.TokenResponseDto;
import com.ycyw.chat.mappers.ClientMapper;
import com.ycyw.chat.models.Client;
import com.ycyw.chat.services.ClientDetails;
import com.ycyw.chat.services.ClientDetailsService;
import com.ycyw.chat.services.JWTService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private JWTService jwtService;

  @Autowired
  private ClientDetailsService clientDetailsService;

  @Autowired
  private ClientMapper clientMapper;

  @PostMapping("/login")
  public ResponseEntity<ApiResponseDto> loginUser(@RequestBody LoginDto loginDto) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              loginDto.getIdentifier(),
              loginDto.getPassword()));

      ClientDetails clientDetails = (ClientDetails) authentication.getPrincipal();
      String token = jwtService.generateClientToken(clientDetails);
      TokenResponseDto response = new TokenResponseDto(token);
      return ResponseEntity.ok(response);
    } catch (AuthenticationException e) {
      ErrorResponseDto response = new ErrorResponseDto("Échec authentication");
      return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
  }

  @GetMapping("/me")
  public ResponseEntity<ClientDTO> getCurrentUser(@AuthenticationPrincipal ClientDetails clientDetails) {
    Client client = clientDetailsService.getCurrentUser(clientDetails.getEmail());

    ClientDTO clientDto = clientMapper.toDto(client);
    return ResponseEntity.ok(clientDto);
  }
}
