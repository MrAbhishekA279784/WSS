package com.womensafety.services;

/**
 * Generic typed result wrapper for service layer operations.
 */
public record ServiceResult<T>(boolean success, String message, T data) {

    public static <T> ServiceResult<T> ok(T data, String message) {
        return new ServiceResult<>(true, message, data);
    }

    public static <T> ServiceResult<T> ok(T data) {
        return new ServiceResult<>(true, "", data);
    }

    public static <T> ServiceResult<T> fail(String message) {
        return new ServiceResult<>(false, message, null);
    }
}
