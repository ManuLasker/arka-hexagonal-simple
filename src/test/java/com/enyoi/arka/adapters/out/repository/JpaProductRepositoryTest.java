package com.enyoi.arka.adapters.out.repository;

import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.entities.ProductCategory;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JpaProductRepository - Tests de Integración")
class JpaProductRepositoryTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private JpaProductRepository repository;

    @BeforeAll
    static void setUpClass() {
        entityManagerFactory = Persistence.createEntityManagerFactory("test-persistence-unit");
    }

    @AfterAll
    static void tearDownClass() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        entityManager = entityManagerFactory.createEntityManager();
        repository = new JpaProductRepository(entityManager);
        limpiarBaseDeDatos();
    }

    @AfterEach
    void tearDown() {
        if (entityManager != null && entityManager.isOpen()) {
            entityManager.close();
        }
    }

    private void limpiarBaseDeDatos() {
        entityManager.getTransaction().begin();
        entityManager.createQuery("DELETE FROM ProductEntity").executeUpdate();
        entityManager.getTransaction().commit();
    }

    private Product crearProducto(String id, String nombre, int stock, ProductCategory categoria) {
        return Product.builder()
                .id(ProductId.of(id))
                .name(nombre)
                .description("Descripción de " + nombre)
                .price(Money.of(new BigDecimal("100.00"), "COP"))
                .stock(stock)
                .category(categoria)
                .build();
    }

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar un producto nuevo correctamente")
        void debeGuardarProductoNuevo() {
            // Given
            Product producto = crearProducto("prod-001", "Teclado Mecánico", 50, ProductCategory.PERIFERICOS);

            // When
            Product resultado = repository.save(producto);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId().value()).isEqualTo("prod-001");
            assertThat(resultado.getName()).isEqualTo("Teclado Mecánico");
        }

        @Test
        @DisplayName("Debe actualizar un producto existente")
        void debeActualizarProductoExistente() {
            // Given
            Product producto = crearProducto("prod-002", "Mouse Gaming", 30, ProductCategory.PERIFERICOS);
            repository.save(producto);

            Product productoActualizado = Product.builder()
                    .id(ProductId.of("prod-002"))
                    .name("Mouse Gaming RGB")
                    .description("Descripción actualizada")
                    .price(Money.of(new BigDecimal("150.00"), "COP"))
                    .stock(25)
                    .category(ProductCategory.PERIFERICOS)
                    .build();

            // When
            repository.save(productoActualizado);

            // Then
            Optional<Product> encontrado = repository.findById(ProductId.of("prod-002"));
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getName()).isEqualTo("Mouse Gaming RGB");
            assertThat(encontrado.get().getStock()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar producto por ID existente")
        void debeEncontrarProductoPorIdExistente() {
            // Given
            Product producto = crearProducto("prod-003", "Monitor 27\"", 15, ProductCategory.PERIFERICOS);
            repository.save(producto);

            // When
            Optional<Product> resultado = repository.findById(ProductId.of("prod-003"));

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getName()).isEqualTo("Monitor 27\"");
            assertThat(resultado.get().getStock()).isEqualTo(15);
        }

        @Test
        @DisplayName("Debe retornar vacío para ID inexistente")
        void debeRetornarVacioParaIdInexistente() {
            // When
            Optional<Product> resultado = repository.findById(ProductId.of("no-existe"));

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay productos")
        void debeRetornarListaVaciaCuandoNoHayProductos() {
            // When
            List<Product> resultado = repository.findAll();

            // Then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar todos los productos")
        void debeRetornarTodosLosProductos() {
            // Given
            repository.save(crearProducto("prod-004", "SSD 1TB", 20, ProductCategory.ALMACENAMIENTO));
            repository.save(crearProducto("prod-005", "HDD 2TB", 10, ProductCategory.ALMACENAMIENTO));
            repository.save(crearProducto("prod-006", "RAM 16GB", 25, ProductCategory.COMPONENTES));

            // When
            List<Product> resultado = repository.findAll();

            // Then
            assertThat(resultado).hasSize(3);
        }
    }

    @Nested
    @DisplayName("findByCategory()")
    class FindByCategoryTests {

        @Test
        @DisplayName("Debe encontrar productos por categoría")
        void debeEncontrarProductosPorCategoria() {
            // Given
            repository.save(crearProducto("prod-007", "Teclado", 10, ProductCategory.PERIFERICOS));
            repository.save(crearProducto("prod-008", "Mouse", 15, ProductCategory.PERIFERICOS));
            repository.save(crearProducto("prod-009", "SSD", 20, ProductCategory.ALMACENAMIENTO));

            // When
            List<Product> resultado = repository.findByCategory("PERIFERICOS");

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado).allMatch(p -> p.getCategory() == ProductCategory.PERIFERICOS);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay productos en categoría")
        void debeRetornarListaVaciaSiNoHayProductosEnCategoria() {
            // Given
            repository.save(crearProducto("prod-010", "SSD", 20, ProductCategory.ALMACENAMIENTO));

            // When
            List<Product> resultado = repository.findByCategory("ENFRIAMIENTO");

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findLowStockProducts()")
    class FindLowStockProductsTests {

        @Test
        @DisplayName("Debe encontrar productos con stock bajo")
        void debeEncontrarProductosConStockBajo() {
            // Given
            repository.save(crearProducto("prod-011", "Producto con stock bajo", 5, ProductCategory.OTROS));
            repository.save(crearProducto("prod-012", "Producto con stock normal", 50, ProductCategory.OTROS));
            repository.save(crearProducto("prod-013", "Producto sin stock", 0, ProductCategory.OTROS));

            // When
            List<Product> resultado = repository.findLowStockProducts(10);

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado).allMatch(p -> p.getStock() < 10);
        }

        @Test
        @DisplayName("Debe retornar lista vacía si todos tienen stock suficiente")
        void debeRetornarListaVaciaSiTodosConStockSuficiente() {
            // Given
            repository.save(crearProducto("prod-014", "Producto 1", 100, ProductCategory.OTROS));
            repository.save(crearProducto("prod-015", "Producto 2", 50, ProductCategory.OTROS));

            // When
            List<Product> resultado = repository.findLowStockProducts(10);

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsByIdTests {

        @Test
        @DisplayName("Debe retornar true si producto existe")
        void debeRetornarTrueSiProductoExiste() {
            // Given
            repository.save(crearProducto("prod-016", "Producto", 10, ProductCategory.OTROS));

            // When
            boolean existe = repository.existsById(ProductId.of("prod-016"));

            // Then
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si producto no existe")
        void debeRetornarFalseSiProductoNoExiste() {
            // When
            boolean existe = repository.existsById(ProductId.of("no-existe"));

            // Then
            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar producto existente")
        void debeEliminarProductoExistente() {
            // Given
            repository.save(crearProducto("prod-017", "Producto a eliminar", 10, ProductCategory.OTROS));
            assertThat(repository.existsById(ProductId.of("prod-017"))).isTrue();

            // When
            repository.deleteById(ProductId.of("prod-017"));

            // Then
            assertThat(repository.existsById(ProductId.of("prod-017"))).isFalse();
        }

        @Test
        @DisplayName("No debe lanzar excepción al eliminar producto inexistente")
        void noDebeLanzarExcepcionAlEliminarProductoInexistente() {
            // When & Then - No debería lanzar excepción
            repository.deleteById(ProductId.of("no-existe"));
        }
    }
}
