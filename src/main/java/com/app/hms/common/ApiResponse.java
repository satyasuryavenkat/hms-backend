package com.app.hms.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success, String message, T data, Map<String, String> errors, OffsetDateTime timestamp) {
  public static <T> ApiResponse<T> ok(String message, T data) {
    return new ApiResponse<>(true, message, data, null, null);
  }

  public static <T> ApiResponse<T> ok(T data) {
    return ok("Operation completed successfully", data);
  }

  public static ApiResponse<Void> error(String message, Map<String, String> errors) {
    return new ApiResponse<>(false, message, null, errors, OffsetDateTime.now());
  }
}
