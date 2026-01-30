package com.enyoi.arka.domain.valueobjects;

public record OrderId(String value) {
    public static OrderId of(String value) {
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException("Order id cannot be empty");
        }
        return new OrderId(value);
    }
}


