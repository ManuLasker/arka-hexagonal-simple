package com.enyoi.arka.domain.valueobjects;

public record CustomerId(String value) {
    public static CustomerId of(String value) {
        if (value.trim().isBlank()) {
            throw new IllegalArgumentException("Customer id cannot be empty");
        }
        return new CustomerId(value);
    }
}
