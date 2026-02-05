package com.enyoi.arka.adapters.in;

import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.entities.ProductCategory;
import com.enyoi.arka.domain.exception.ProductNotFoundException;
import com.enyoi.arka.domain.ports.out.NotificationService;
import com.enyoi.arka.domain.ports.out.ProductRepository;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("InventoryServiceImpl - Tests")
class InventoryServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NotificationService notificationService;

    private InventoryServiceImpl inventoryService;

    @BeforeEach
    void setUp() {
        inventoryService = new InventoryServiceImpl(productRepository, notificationService);
    }

    private Product crearProducto(String id, String nombre, int stock) {
        return Product.builder()
                .id(ProductId.of(id))
                .name(nombre)
                .description("Descripción")
                .price(Money.of(new BigDecimal("100000.00"), "COP"))
                .stock(stock)
                .category(ProductCategory.PERIFERICOS)
                .build();
    }

    @Nested
    @DisplayName("registerProduct()")
    class RegisterProductTests {

        @Test
        @DisplayName("Debe registrar producto nuevo")
        void debeRegistrarProductoNuevo() {
            // Given
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Product resultado = inventoryService.registerProduct(
                    "Teclado",
                    "Teclado mecánico",
                    Money.of(new BigDecimal("150000"), "COP"),
                    50,
                    "PERIFERICOS"
            );

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getName()).isEqualTo("Teclado");
            assertThat(resultado.getStock()).isEqualTo(50);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("getProductById()")
    class GetProductByIdTests {

        @Test
        @DisplayName("Debe retornar producto existente")
        void debeRetornarProductoExistente() {
            // Given
            Product producto = crearProducto("prod-001", "Teclado", 50);
            when(productRepository.findById(ProductId.of("prod-001"))).thenReturn(Optional.of(producto));

            // When
            Product resultado = inventoryService.getProductById(ProductId.of("prod-001"));

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getName()).isEqualTo("Teclado");
        }

        @Test
        @DisplayName("Debe lanzar excepción si producto no existe")
        void debeLanzarExcepcionSiProductoNoExiste() {
            // Given
            when(productRepository.findById(any())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> inventoryService.getProductById(ProductId.of("no-existe")))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProductsTests {

        @Test
        @DisplayName("Debe retornar todos los productos")
        void debeRetornarTodosLosProductos() {
            // Given
            List<Product> productos = List.of(
                    crearProducto("prod-001", "Producto 1", 10),
                    crearProducto("prod-002", "Producto 2", 20)
            );
            when(productRepository.findAll()).thenReturn(productos);

            // When
            List<Product> resultado = inventoryService.getAllProducts();

            // Then
            assertThat(resultado).hasSize(2);
        }
    }

    @Nested
    @DisplayName("updateStock()")
    class UpdateStockTests {

        @Test
        @DisplayName("Debe actualizar stock de producto")
        void debeActualizarStockDeProducto() {
            // Given
            Product producto = crearProducto("prod-001", "Teclado", 50);
            when(productRepository.findById(ProductId.of("prod-001"))).thenReturn(Optional.of(producto));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            Product resultado = inventoryService.updateStock(ProductId.of("prod-001"), 100);

            // Then
            assertThat(resultado.getStock()).isEqualTo(100);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("reduceStock()")
    class ReduceStockTests {

        @Test
        @DisplayName("Debe reducir stock de producto")
        void debeReducirStockDeProducto() {
            // Given
            Product producto = crearProducto("prod-001", "Teclado", 50);
            when(productRepository.findById(ProductId.of("prod-001"))).thenReturn(Optional.of(producto));
            when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

            // When
            inventoryService.reduceStock(ProductId.of("prod-001"), 10);

            // Then
            verify(productRepository).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("getLowStockProducts()")
    class GetLowStockProductsTests {

        @Test
        @DisplayName("Debe retornar productos con stock bajo")
        void debeRetornarProductosConStockBajo() {
            // Given
            List<Product> productosConStockBajo = List.of(
                    crearProducto("prod-001", "Producto 1", 5)
            );
            when(productRepository.findLowStockProducts(10)).thenReturn(productosConStockBajo);

            // When
            List<Product> resultado = inventoryService.getLowStockProducts();

            // Then
            assertThat(resultado).hasSize(1);
        }
    }

    @Nested
    @DisplayName("generateRestockReport()")
    class GenerateRestockReportTests {

        @Test
        @DisplayName("Debe notificar por cada producto con stock bajo")
        void debeNotificarPorCadaProductoConStockBajo() {
            // Given
            List<Product> productosConStockBajo = List.of(
                    crearProducto("prod-001", "Producto 1", 5),
                    crearProducto("prod-002", "Producto 2", 3)
            );
            when(productRepository.findLowStockProducts(10)).thenReturn(productosConStockBajo);

            // When
            inventoryService.generateRestockReport();

            // Then
            verify(notificationService, times(2)).notifyLowStockAlert(anyString(), anyInt());
            verify(notificationService).notifyLowStockAlert("Producto 1", 5);
            verify(notificationService).notifyLowStockAlert("Producto 2", 3);
        }

        @Test
        @DisplayName("No debe notificar si no hay productos con stock bajo")
        void noDebeNotificarSiNoHayProductosConStockBajo() {
            // Given
            when(productRepository.findLowStockProducts(10)).thenReturn(List.of());

            // When
            inventoryService.generateRestockReport();

            // Then
            verify(notificationService, never()).notifyLowStockAlert(anyString(), anyInt());
        }
    }

    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {

        @Test
        @DisplayName("Debe fallar con ProductRepository null")
        void debeFallarConProductRepositoryNull() {
            assertThatThrownBy(() -> new InventoryServiceImpl(null, notificationService))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Debe fallar con NotificationService null")
        void debeFallarConNotificationServiceNull() {
            assertThatThrownBy(() -> new InventoryServiceImpl(productRepository, null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
