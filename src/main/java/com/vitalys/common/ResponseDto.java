package com.vitalys.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

/**
 * Standardized API response envelope for all endpoints.
 * All responses share the same shape: success, message, data, timestamp.
 *
 * @param <T> the type of the response payload
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {

    private final boolean success;
    private final String  message;
    private final T       data;
    private final OffsetDateTime timestamp;

    // ── Static factory methods ────────────────────────────────────────────

    public static <T> ResponseDto<T> ok(T data) {
        return ResponseDto.<T>builder()
                .success(true)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ResponseDto<T> ok(String message, T data) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ResponseDto<T> created(T data) {
        return ResponseDto.<T>builder()
                .success(true)
                .message("Resource created successfully")
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static <T> ResponseDto<T> created(String message, T data) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static ResponseDto<Void> noContent(String message) {
        return ResponseDto.<Void>builder()
                .success(true)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .build();
    }

    public static ResponseDto<Void> error(String message) {
        return ResponseDto.<Void>builder()
                .success(false)
                .message(message)
                .timestamp(OffsetDateTime.now())
                .build();
    }
}
