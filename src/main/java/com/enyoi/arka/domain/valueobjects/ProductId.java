package com.enyoi.arka.domain.valueobjects;

public record ProductId(String value) {
    public static ProductId of(String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer id cannot be empty");
        }
        return new ProductId(value);
    }
}
