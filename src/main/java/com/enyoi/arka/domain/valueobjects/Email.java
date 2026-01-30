package com.enyoi.arka.domain.valueobjects;

import java.util.regex.Pattern;

public record Email(String value) {
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[a-zA-Z0-9._+-]+@[a-zA-Z0-9]+\\.[a-zA-Z0-9]+");

    public static Email of(String value) {
        if (!EMAIL_REGEX.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
        return new Email(value);
    }
}
