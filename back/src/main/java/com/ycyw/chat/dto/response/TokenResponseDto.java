package com.ycyw.chat.dto.response;

public class TokenResponseDto implements ApiResponseDto {
  private String token;

  public TokenResponseDto(String token) {
    this.token = token;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
