package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrderItem - Entidad de Dominio")
class OrderItemTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Debe crear OrderItem con todos los campos")
        void debeCrearOrderItemConTodosLosCampos() {
            // When
            OrderItem item = OrderItem.builder()
                    .productId(ProductId.of("prod-001"))
                    .quantity(5)
                    .unitPrice(Money.of(new BigDecimal("50000.00"), "COP"))
                    .build();

            // Then
            assertThat(item.getProductId().value()).isEqualTo("prod-001");
            assertThat(item.getQuantity()).isEqualTo(5);
            assertThat(item.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        }

        @Test
        @DisplayName("Debe fallar sin productId")
        void debeFallarSinProductId() {
            assertThatThrownBy(() -> OrderItem.builder()
                    .quantity(5)
                    .unitPrice(Money.of(new BigDecimal("100"), "COP"))
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Product id must not be null");
        }

        @Test
        @DisplayName("Debe fallar con cantidad cero")
        void debeFallarConCantidadCero() {
            assertThatThrownBy(() -> OrderItem.builder()
                    .productId(ProductId.of("prod-001"))
                    .quantity(0)
                    .unitPrice(Money.of(new BigDecimal("100"), "COP"))
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Debe fallar con cantidad negativa")
        void debeFallarConCantidadNegativa() {
            assertThatThrownBy(() -> OrderItem.builder()
                    .productId(ProductId.of("prod-001"))
                    .quantity(-1)
                    .unitPrice(Money.of(new BigDecimal("100"), "COP"))
                    .build())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Quantity must be greater than zero");
        }

        @Test
        @DisplayName("Debe fallar sin precio unitario")
        void debeFallarSinPrecioUnitario() {
            assertThatThrownBy(() -> OrderItem.builder()
                    .productId(ProductId.of("prod-001"))
                    .quantity(5)
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("Unit price must not be null");
        }
    }

    @Nested
    @DisplayName("getTotalPrice()")
    class GetTotalPriceTests {

        @Test
        @DisplayName("Debe calcular precio total correctamente")
        void debeCalcularPrecioTotalCorrectamente() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productId(ProductId.of("prod-001"))
                    .quantity(3)
                    .unitPrice(Money.of(new BigDecimal("25000.00"), "COP"))
                    .build();

            // When
            Money total = item.getTotalPrice();

            // Then
            assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("75000.00"));
            assertThat(total.currency().getCurrencyCode()).isEqualTo("COP");
        }

        @Test
        @DisplayName("Debe calcular precio total con cantidad 1")
        void debeCalcularPrecioTotalConCantidadUno() {
            // Given
            OrderItem item = OrderItem.builder()
                    .productId(ProductId.of("prod-001"))
                    .quantity(1)
                    .unitPrice(Money.of(new BigDecimal("100000.00"), "COP"))
                    .build();

            // When
            Money total = item.getTotalPrice();

            // Then
            assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("100000.00"));
        }
    }
}
