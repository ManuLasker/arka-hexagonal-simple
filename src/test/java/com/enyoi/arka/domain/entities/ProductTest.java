package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Product - Entidad de Dominio")
class ProductTest {

    private Product crearProducto(int stock) {
        return Product.builder()
                .id(ProductId.of("prod-001"))
                .name("Teclado Mecánico")
                .description("Teclado RGB")
                .price(Money.of(new BigDecimal("150000.00"), "COP"))
                .stock(stock)
                .category(ProductCategory.PERIFERICOS)
                .build();
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Debe crear producto con todos los campos")
        void debeCrearProductoConTodosLosCampos() {
            // When
            Product producto = crearProducto(50);

            // Then
            assertThat(producto.getId().value()).isEqualTo("prod-001");
            assertThat(producto.getName()).isEqualTo("Teclado Mecánico");
            assertThat(producto.getDescription()).isEqualTo("Teclado RGB");
            assertThat(producto.getPrice().amount()).isEqualByComparingTo(new BigDecimal("150000.00"));
            assertThat(producto.getStock()).isEqualTo(50);
            assertThat(producto.getCategory()).isEqualTo(ProductCategory.PERIFERICOS);
        }

        @Test
        @DisplayName("Debe fallar sin ID")
        void debeFallarSinId() {
            assertThatThrownBy(() -> Product.builder()
                    .name("Test")
                    .price(Money.of(new BigDecimal("100"), "COP"))
                    .category(ProductCategory.OTROS)
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("id is required");
        }

        @Test
        @DisplayName("Debe fallar sin nombre")
        void debeFallarSinNombre() {
            assertThatThrownBy(() -> Product.builder()
                    .id(ProductId.of("prod-001"))
                    .price(Money.of(new BigDecimal("100"), "COP"))
                    .category(ProductCategory.OTROS)
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        @DisplayName("Debe fallar sin precio")
        void debeFallarSinPrecio() {
            assertThatThrownBy(() -> Product.builder()
                    .id(ProductId.of("prod-001"))
                    .name("Test")
                    .category(ProductCategory.OTROS)
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("price is required");
        }

        @Test
        @DisplayName("Debe fallar sin categoría")
        void debeFallarSinCategoria() {
            assertThatThrownBy(() -> Product.builder()
                    .id(ProductId.of("prod-001"))
                    .name("Test")
                    .price(Money.of(new BigDecimal("100"), "COP"))
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("category is required");
        }
    }

    @Nested
    @DisplayName("reduceStock()")
    class ReduceStockTests {

        @Test
        @DisplayName("Debe reducir stock correctamente")
        void debeReducirStockCorrectamente() {
            // Given
            Product producto = crearProducto(50);

            // When
            producto.reduceStock(10);

            // Then
            assertThat(producto.getStock()).isEqualTo(40);
        }

        @Test
        @DisplayName("Debe lanzar excepción si cantidad excede stock")
        void debeLanzarExcepcionSiCantidadExcedeStock() {
            // Given
            Product producto = crearProducto(10);

            // When & Then
            assertThatThrownBy(() -> producto.reduceStock(15))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("stock exceeded");
        }

        @Test
        @DisplayName("Debe permitir reducir a cero")
        void debePermitirReducirACero() {
            // Given
            Product producto = crearProducto(10);

            // When
            producto.reduceStock(10);

            // Then
            assertThat(producto.getStock()).isZero();
        }
    }

    @Nested
    @DisplayName("increaseStock()")
    class IncreaseStockTests {

        @Test
        @DisplayName("Debe aumentar stock correctamente")
        void debeAumentarStockCorrectamente() {
            // Given
            Product producto = crearProducto(50);

            // When
            producto.increaseStock(25);

            // Then
            assertThat(producto.getStock()).isEqualTo(75);
        }

        @Test
        @DisplayName("Debe aumentar stock desde cero")
        void debeAumentarStockDesdeCero() {
            // Given
            Product producto = crearProducto(0);

            // When
            producto.increaseStock(100);

            // Then
            assertThat(producto.getStock()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("isLowStock()")
    class IsLowStockTests {

        @Test
        @DisplayName("Debe retornar true si stock está por debajo del umbral")
        void debeRetornarTrueSiStockBajoUmbral() {
            // Given
            Product producto = crearProducto(5);

            // Then
            assertThat(producto.isLowStock(10)).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si stock está por encima del umbral")
        void debeRetornarFalseSiStockSobreUmbral() {
            // Given
            Product producto = crearProducto(50);

            // Then
            assertThat(producto.isLowStock(10)).isFalse();
        }

        @Test
        @DisplayName("Debe retornar false si stock es igual al umbral")
        void debeRetornarFalseSiStockIgualAlUmbral() {
            // Given
            Product producto = crearProducto(10);

            // Then
            assertThat(producto.isLowStock(10)).isFalse();
        }
    }
}
