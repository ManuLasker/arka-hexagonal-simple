package com.enyoi.arka.domain.valueobjects;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

public record Money(BigDecimal amount, Currency currency) {
    public Money(BigDecimal amount, Currency currency) {
        this.amount = Objects.requireNonNull(amount, "Amount must not be null");
        this.currency = Objects.requireNonNull(currency, "Currency must not be null");

        if (BigDecimal.ZERO.compareTo(amount) > 0) {
            throw new ArithmeticException("Amount must be greater than zero");
        }
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, Currency.getInstance(currency));
    }

    public Money add(Money money) {
        if (!currency.equals(money.currency())) throw new ArithmeticException("Currencies do not match");
        return new Money(amount.add(money.amount), currency);
    }

    public Money multiply(int factor) {
        return new Money(amount.multiply(BigDecimal.valueOf(factor)), currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }
}
