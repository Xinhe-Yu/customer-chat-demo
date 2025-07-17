package com.ycyw.chat.dto.response;

public class ErrorResponseDto implements ApiResponseDto {
  private String error;

  public ErrorResponseDto(String error) {
    this.error = error;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
