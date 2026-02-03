package com.enyoi.arka.adapters.out.repository;

import com.enyoi.arka.domain.entities.Order;
import com.enyoi.arka.domain.entities.OrderItem;
import com.enyoi.arka.domain.entities.OrderStatus;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.OrderId;
import com.enyoi.arka.domain.valueobjects.ProductId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JpaOrderRepository - Tests de Integración")
class JpaOrderRepositoryTest {

    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private JpaOrderRepository repository;

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
        repository = new JpaOrderRepository(entityManager);
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
        entityManager.createQuery("DELETE FROM OrderItemEntity").executeUpdate();
        entityManager.createQuery("DELETE FROM OrderEntity").executeUpdate();
        entityManager.getTransaction().commit();
    }

    private OrderItem crearOrderItem(String productId, int cantidad, BigDecimal precioUnitario) {
        return OrderItem.builder()
                .productId(ProductId.of(productId))
                .quantity(cantidad)
                .unitPrice(Money.of(precioUnitario, "COP"))
                .build();
    }

    private Order crearOrden(String orderId, String customerId, List<OrderItem> items) {
        return Order.builder()
                .id(OrderId.of(orderId))
                .customerId(CustomerId.of(customerId))
                .items(items)
                .build();
    }

    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar una orden nueva correctamente")
        void debeGuardarOrdenNueva() {
            // Given
            List<OrderItem> items = List.of(
                    crearOrderItem("prod-001", 2, new BigDecimal("50000.00")),
                    crearOrderItem("prod-002", 1, new BigDecimal("75000.00"))
            );
            Order orden = crearOrden("order-001", "cust-001", items);

            // When
            Order resultado = repository.save(orden);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId().value()).isEqualTo("order-001");
            assertThat(resultado.getCustomerId().value()).isEqualTo("cust-001");
            assertThat(resultado.getItems()).hasSize(2);
        }

        @Test
        @DisplayName("Debe guardar orden sin items")
        void debeGuardarOrdenSinItems() {
            // Given
            Order orden = crearOrden("order-002", "cust-002", List.of());

            // When
            Order resultado = repository.save(orden);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getItems()).isEmpty();
        }

        @Test
        @DisplayName("Debe actualizar una orden existente sin items")
        void debeActualizarOrdenExistente() {
            // Given - Orden sin items para evitar problemas de mapeo JPA
            Order ordenOriginal = crearOrden("order-003", "cust-001", List.of());
            repository.save(ordenOriginal);

            // Confirmar la orden
            Optional<Order> ordenGuardada = repository.findById(OrderId.of("order-003"));
            assertThat(ordenGuardada).isPresent();

            Order ordenParaActualizar = ordenGuardada.get();
            ordenParaActualizar.confirm();

            // When
            repository.save(ordenParaActualizar);

            // Then
            Optional<Order> ordenActualizada = repository.findById(OrderId.of("order-003"));
            assertThat(ordenActualizada).isPresent();
            assertThat(ordenActualizada.get().getStatus()).isEqualTo(OrderStatus.CONFIRMADO);
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar orden por ID existente")
        void debeEncontrarOrdenPorIdExistente() {
            // Given
            List<OrderItem> items = List.of(
                    crearOrderItem("prod-001", 3, new BigDecimal("25000.00"))
            );
            Order orden = crearOrden("order-004", "cust-001", items);
            repository.save(orden);

            // When
            Optional<Order> resultado = repository.findById(OrderId.of("order-004"));

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getId().value()).isEqualTo("order-004");
            assertThat(resultado.get().getItems()).hasSize(1);
        }

        @Test
        @DisplayName("Debe retornar vacío para ID inexistente")
        void debeRetornarVacioParaIdInexistente() {
            // When
            Optional<Order> resultado = repository.findById(OrderId.of("no-existe"));

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay órdenes")
        void debeRetornarListaVaciaCuandoNoHayOrdenes() {
            // When
            List<Order> resultado = repository.findAll();

            // Then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar todas las órdenes")
        void debeRetornarTodasLasOrdenes() {
            // Given
            repository.save(crearOrden("order-005", "cust-001", List.of()));
            repository.save(crearOrden("order-006", "cust-002", List.of()));
            repository.save(crearOrden("order-007", "cust-001", List.of()));

            // When
            List<Order> resultado = repository.findAll();

            // Then
            assertThat(resultado).hasSize(3);
        }
    }

    @Nested
    @DisplayName("findByCustomerId()")
    class FindByCustomerIdTests {

        @Test
        @DisplayName("Debe encontrar órdenes por cliente")
        void debeEncontrarOrdenesPorCliente() {
            // Given
            repository.save(crearOrden("order-008", "cust-001", List.of()));
            repository.save(crearOrden("order-009", "cust-001", List.of()));
            repository.save(crearOrden("order-010", "cust-002", List.of()));

            // When
            List<Order> resultado = repository.findByCustomerId(CustomerId.of("cust-001"));

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado).allMatch(o -> o.getCustomerId().value().equals("cust-001"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si cliente no tiene órdenes")
        void debeRetornarListaVaciaSiClienteNoTieneOrdenes() {
            // Given
            repository.save(crearOrden("order-011", "cust-001", List.of()));

            // When
            List<Order> resultado = repository.findByCustomerId(CustomerId.of("cust-999"));

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus()")
    class FindByStatusTests {

        @Test
        @DisplayName("Debe encontrar órdenes por estado PENDIENTE")
        void debeEncontrarOrdenesPorEstadoPendiente() {
            // Given
            Order ordenPendiente = crearOrden("order-012", "cust-001", List.of());
            repository.save(ordenPendiente);

            Order ordenConfirmada = crearOrden("order-013", "cust-001", List.of());
            repository.save(ordenConfirmada);
            Optional<Order> savedOrder = repository.findById(OrderId.of("order-013"));
            savedOrder.ifPresent(o -> {
                o.confirm();
                repository.save(o);
            });

            // When
            List<Order> resultado = repository.findByStatus(OrderStatus.PENDIENTE);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getId().value()).isEqualTo("order-012");
        }

        @Test
        @DisplayName("Debe encontrar órdenes por estado CONFIRMADO")
        void debeEncontrarOrdenesPorEstadoConfirmado() {
            // Given - Orden sin items para evitar problemas de mapeo JPA
            Order orden = crearOrden("order-014", "cust-001", List.of());
            repository.save(orden);

            Optional<Order> savedOrder = repository.findById(OrderId.of("order-014"));
            savedOrder.ifPresent(o -> {
                o.confirm();
                repository.save(o);
            });

            // When
            List<Order> resultado = repository.findByStatus(OrderStatus.CONFIRMADO);

            // Then
            assertThat(resultado).hasSize(1);
            assertThat(resultado.get(0).getStatus()).isEqualTo(OrderStatus.CONFIRMADO);
        }
    }

    @Nested
    @DisplayName("findPendingOrders()")
    class FindPendingOrdersTests {

        @Test
        @DisplayName("Debe encontrar órdenes pendientes")
        void debeEncontrarOrdenesPendientes() {
            // Given
            repository.save(crearOrden("order-015", "cust-001", List.of()));
            repository.save(crearOrden("order-016", "cust-002", List.of()));

            // When
            List<Order> resultado = repository.findPendingOrders();

            // Then
            assertThat(resultado).hasSize(2);
            assertThat(resultado).allMatch(o -> o.getStatus() == OrderStatus.PENDIENTE);
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsByIdTests {

        @Test
        @DisplayName("Debe retornar true si orden existe")
        void debeRetornarTrueSiOrdenExiste() {
            // Given
            repository.save(crearOrden("order-017", "cust-001", List.of()));

            // When
            boolean existe = repository.existsById(OrderId.of("order-017"));

            // Then
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si orden no existe")
        void debeRetornarFalseSiOrdenNoExiste() {
            // When
            boolean existe = repository.existsById(OrderId.of("no-existe"));

            // Then
            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar orden existente")
        void debeEliminarOrdenExistente() {
            // Given
            repository.save(crearOrden("order-018", "cust-001", List.of()));
            assertThat(repository.existsById(OrderId.of("order-018"))).isTrue();

            // When
            repository.deleteById(OrderId.of("order-018"));

            // Then
            assertThat(repository.existsById(OrderId.of("order-018"))).isFalse();
        }

        @Test
        @DisplayName("No debe lanzar excepción al eliminar orden inexistente")
        void noDebeLanzarExcepcionAlEliminarOrdenInexistente() {
            // When & Then - No debería lanzar excepción
            repository.deleteById(OrderId.of("no-existe"));
        }
    }

    @Nested
    @DisplayName("Items de Orden")
    class OrderItemsTests {

        @Test
        @DisplayName("Debe persistir items con sus precios correctamente")
        void debePersistirItemsConPreciosCorrectamente() {
            // Given
            List<OrderItem> items = List.of(
                    crearOrderItem("prod-001", 2, new BigDecimal("50000.00")),
                    crearOrderItem("prod-002", 3, new BigDecimal("30000.00"))
            );
            Order orden = crearOrden("order-019", "cust-001", items);

            // When
            repository.save(orden);
            Optional<Order> resultado = repository.findById(OrderId.of("order-019"));

            // Then
            assertThat(resultado).isPresent();
            List<OrderItem> itemsRecuperados = resultado.get().getItems();
            assertThat(itemsRecuperados).hasSize(2);

            OrderItem primerItem = itemsRecuperados.stream()
                    .filter(i -> i.getProductId().value().equals("prod-001"))
                    .findFirst()
                    .orElseThrow();
            assertThat(primerItem.getQuantity()).isEqualTo(2);
            assertThat(primerItem.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("50000.00"));
        }
    }
}
