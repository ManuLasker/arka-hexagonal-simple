package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.OrderId;
import com.enyoi.arka.domain.valueobjects.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Order - Entidad de Dominio")
class OrderTest {

    private OrderItem crearItem(String productId, int cantidad, BigDecimal precio) {
        return OrderItem.builder()
                .productId(ProductId.of(productId))
                .quantity(cantidad)
                .unitPrice(Money.of(precio, "COP"))
                .build();
    }

    private Order crearOrdenBasica() {
        return Order.builder()
                .id(OrderId.of("order-001"))
                .customerId(CustomerId.of("cust-001"))
                .build();
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Debe crear orden con campos requeridos")
        void debeCrearOrdenConCamposRequeridos() {
            // When
            Order orden = crearOrdenBasica();

            // Then
            assertThat(orden.getId().value()).isEqualTo("order-001");
            assertThat(orden.getCustomerId().value()).isEqualTo("cust-001");
            assertThat(orden.getStatus()).isEqualTo(OrderStatus.PENDIENTE);
            assertThat(orden.getItems()).isEmpty();
            assertThat(orden.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Debe crear orden con items")
        void debeCrearOrdenConItems() {
            // When
            Order orden = Order.builder()
                    .id(OrderId.of("order-002"))
                    .customerId(CustomerId.of("cust-001"))
                    .items(List.of(
                            crearItem("prod-001", 2, new BigDecimal("50000")),
                            crearItem("prod-002", 1, new BigDecimal("75000"))
                    ))
                    .build();

            // Then
            assertThat(orden.getItems()).hasSize(2);
        }

        @Test
        @DisplayName("Debe fallar sin ID")
        void debeFallarSinId() {
            assertThatThrownBy(() -> Order.builder()
                    .customerId(CustomerId.of("cust-001"))
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("id is required");
        }

        @Test
        @DisplayName("Debe fallar sin customerId")
        void debeFallarSinCustomerId() {
            assertThatThrownBy(() -> Order.builder()
                    .id(OrderId.of("order-001"))
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("customerId is required");
        }
    }

    @Nested
    @DisplayName("confirm()")
    class ConfirmTests {

        @Test
        @DisplayName("Debe confirmar orden pendiente")
        void debeConfirmarOrdenPendiente() {
            // Given
            Order orden = crearOrdenBasica();

            // When
            orden.confirm();

            // Then
            assertThat(orden.getStatus()).isEqualTo(OrderStatus.CONFIRMADO);
        }

        @Test
        @DisplayName("Debe fallar al confirmar orden no pendiente")
        void debeFallarAlConfirmarOrdenNoPendiente() {
            // Given
            Order orden = crearOrdenBasica();
            orden.confirm(); // Ahora está CONFIRMADO

            // When & Then
            assertThatThrownBy(orden::confirm)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only pending orders can be confirmed");
        }
    }

    @Nested
    @DisplayName("ship()")
    class ShipTests {

        @Test
        @DisplayName("Debe enviar orden confirmada")
        void debeEnviarOrdenConfirmada() {
            // Given
            Order orden = crearOrdenBasica();
            orden.confirm();

            // When
            orden.ship();

            // Then
            assertThat(orden.getStatus()).isEqualTo(OrderStatus.EN_DESPACHO);
        }

        @Test
        @DisplayName("Debe fallar al enviar orden no confirmada")
        void debeFallarAlEnviarOrdenNoConfirmada() {
            // Given
            Order orden = crearOrdenBasica(); // PENDIENTE

            // When & Then
            assertThatThrownBy(orden::ship)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only confirmed orders can be shipped");
        }
    }

    @Nested
    @DisplayName("deliver()")
    class DeliverTests {

        @Test
        @DisplayName("Debe entregar orden en despacho")
        void debeEntregarOrdenEnDespacho() {
            // Given
            Order orden = crearOrdenBasica();
            orden.confirm();
            orden.ship();

            // When
            orden.deliver();

            // Then
            assertThat(orden.getStatus()).isEqualTo(OrderStatus.ENTREGADO);
        }

        @Test
        @DisplayName("Debe fallar al entregar orden no enviada")
        void debeFallarAlEntregarOrdenNoEnviada() {
            // Given
            Order orden = crearOrdenBasica();
            orden.confirm(); // CONFIRMADO pero no EN_DESPACHO

            // When & Then
            assertThatThrownBy(orden::deliver)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only shipped orders can be delivered");
        }
    }

    @Nested
    @DisplayName("addItem()")
    class AddItemTests {

        @Test
        @DisplayName("Debe agregar item a orden pendiente")
        void debeAgregarItemAOrdenPendiente() {
            // Given
            Order orden = crearOrdenBasica();
            OrderItem item = crearItem("prod-001", 2, new BigDecimal("50000"));

            // When
            orden.addItem(item);

            // Then
            assertThat(orden.getItems()).hasSize(1);
            assertThat(orden.getItems().get(0).getProductId().value()).isEqualTo("prod-001");
        }

        @Test
        @DisplayName("Debe fallar al agregar item a orden no pendiente")
        void debeFallarAlAgregarItemAOrdenNoPendiente() {
            // Given
            Order orden = crearOrdenBasica();
            orden.confirm();
            OrderItem item = crearItem("prod-001", 2, new BigDecimal("50000"));

            // When & Then
            assertThatThrownBy(() -> orden.addItem(item))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only pending orders can be modified");
        }

        @Test
        @DisplayName("Debe fallar al agregar item con diferente currency a orden con items")
        void debeFallarAlAgregarItemDiferenteCurrencyAOrden() {
            // Given
            Order orden = crearOrdenBasica();
            OrderItem item = crearItem("prod-001", 2, new BigDecimal("50000"));
            orden.addItem(item);
            OrderItem item2 = OrderItem.builder()
                    .productId(ProductId.of("prod-002"))
                    .quantity(3)
                    .unitPrice(Money.of(BigDecimal.valueOf(100), "USD"))
                    .build();


            // When & Then
            assertThatThrownBy(() -> orden.addItem(item2))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only same currency items is allowed");
        }
    }

    @Nested
    @DisplayName("remove()")
    class RemoveTests {

        @Test
        @DisplayName("Debe remover item de orden pendiente")
        void debeRemoverItemDeOrdenPendiente() {
            // Given
            OrderItem item = crearItem("prod-001", 2, new BigDecimal("50000"));
            Order orden = Order.builder()
                    .id(OrderId.of("order-001"))
                    .customerId(CustomerId.of("cust-001"))
                    .addItem(item)
                    .build();

            // When
            orden.remove(item);

            // Then
            assertThat(orden.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Debe fallar al remover item de orden no pendiente")
        void debeFallarAlRemoverItemDeOrdenNoPendiente() {
            // Given
            OrderItem item = crearItem("prod-001", 2, new BigDecimal("50000"));
            Order orden = Order.builder()
                    .id(OrderId.of("order-001"))
                    .customerId(CustomerId.of("cust-001"))
                    .addItem(item)
                    .build();
            orden.confirm();

            // When & Then
            assertThatThrownBy(() -> orden.remove(item))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("only pending orders can be removed");
        }
    }

    @Nested
    @DisplayName("getTotal()")
    class GetTotalTests {

        @Test
        @DisplayName("Debe calcular total de orden con items")
        void debeCalcularTotalDeOrdenConItems() {
            // Given
            Order orden = Order.builder()
                    .id(OrderId.of("order-001"))
                    .customerId(CustomerId.of("cust-001"))
                    .items(List.of(
                            crearItem("prod-001", 2, new BigDecimal("50000")),  // 100000
                            crearItem("prod-002", 3, new BigDecimal("30000"))   // 90000
                    ))
                    .build();

            // When
            Money total = orden.getTotal();

            // Then
            assertThat(total.amount()).isEqualByComparingTo(new BigDecimal("190000.00"));
        }

        @Test
        @DisplayName("Debe retornar cero para orden sin items")
        void debeRetornarCeroParaOrdenSinItems() {
            // Given
            Order orden = crearOrdenBasica();

            // When
            Money total = orden.getTotal();

            // Then
            assertThat(total.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("isPending()")
    class IsPendingTests {

        @Test
        @DisplayName("Debe retornar true para orden pendiente")
        void debeRetornarTrueParaOrdenPendiente() {
            // Given
            Order orden = crearOrdenBasica();

            // Then
            assertThat(orden.isPending()).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false para orden confirmada")
        void debeRetornarFalseParaOrdenConfirmada() {
            // Given
            Order orden = crearOrdenBasica();
            orden.confirm();

            // Then
            assertThat(orden.isPending()).isFalse();
        }
    }
}
