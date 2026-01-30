package com.enyoi.arka.domain.valueobjects;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Pruebas para value object Money")
class MoneyTest {

    @ParameterizedTest
    @CsvSource({
            "10, COP",
            "20.0, COP",
            "30.0, USD",
            "30.0, EUR"
    })
    @DisplayName("Crear monedas con COP Currency como string")
    void monedasConDiferenteCurrency(double amount, String currency) {
        Money money = Money.of(BigDecimal.valueOf(amount), currency);

        assertAll(
                () -> assertThat(money.currency()).isInstanceOf(Currency.class),
                () -> assertThat(money.currency()).isEqualTo(Currency.getInstance(currency)),
                () -> assertThat(money.amount()).isEqualTo(BigDecimal.valueOf(amount))
        );
    }


    @ParameterizedTest
    @CsvSource({
            "10, COP",
            "20.0, COP",
            "30.0, USD",
            "30.0, EUR"
    })
    @DisplayName("Crear monedas con COP Currency")
    void monedasConDiferenteCurrencyUsandoCurrencyObject(double amount, String currency) {
        Money money = Money.of(BigDecimal.valueOf(amount), Currency.getInstance(currency));

        assertAll(
                () -> assertThat(money.currency()).isInstanceOf(Currency.class),
                () -> assertThat(money.currency()).isEqualTo(Currency.getInstance(currency)),
                () -> assertThat(money.amount()).isEqualTo(BigDecimal.valueOf(amount))
        );
    }

    @Test
    @DisplayName("Validar la suma con currency igual")
    void testValidarSumaCurrencyValida() {
        Money money = Money.of(BigDecimal.valueOf(10), "COP");
        Money money2 = Money.of(BigDecimal.valueOf(20), "COP");
        Money suma = money.add(money2);

        assertThat(suma.amount()).isEqualTo(BigDecimal.valueOf(30));
        assertThat(suma.currency().toString()).isEqualTo("COP");
    }

    @Test
    @DisplayName("Multiplicar Moneda por un valor o factor")
    void testValidarMultiplicarMoneda() {
        Money money = Money.of(BigDecimal.valueOf(10), "COP");
        Money moneyResult = money.multiply(100);

        assertThat(moneyResult.amount()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(moneyResult.currency().toString()).isEqualTo("COP");
    }


    @Test
    @DisplayName("Creación de moneda con Zero de amount")
    void testValidarZeroMoneda() {
        Money money = Money.zero(Currency.getInstance("COP"));

        assertThat(money.amount()).isEqualTo(BigDecimal.valueOf(0));
        assertThat(money.currency().toString()).isEqualTo("COP");
    }
}