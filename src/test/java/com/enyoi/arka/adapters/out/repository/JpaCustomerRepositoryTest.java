package com.enyoi.arka.adapters.out.repository;

import com.enyoi.arka.domain.entities.Customer;
import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.entities.ProductCategory;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Email;
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

@DisplayName("JpaCustomerRepository - Tests de Integracion")
public class JpaCustomerRepositoryTest {
    private static EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    private JpaCustomerRepository repository;

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
        repository = new JpaCustomerRepository(entityManager);
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
        entityManager.createQuery("DELETE FROM CustomerEntity").executeUpdate();
        entityManager.getTransaction().commit();
    }


    private Customer crearCustomer(String id, String name, String email, String city) {
        return Customer.builder()
                .id(CustomerId.of(id))
                .name(name)
                .email(Email.of(email))
                .city(city)
                .build();
    }


    @Nested
    @DisplayName("save()")
    class SaveTests {

        @Test
        @DisplayName("Debe guardar un customer nuevo correctamente")
        void debeGuardarCustomerNuevo() {
            // Given
            Customer customer = crearCustomer("cus-001", "manu", "manu@test.com", "quilla");

            // When
            Customer resultado = repository.save(customer);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado.getId().value()).isEqualTo("cus-001");
            assertThat(resultado.getName()).isEqualTo("manu");
        }

        @Test
        @DisplayName("Debe actualizar un customer existente")
        void debeActualizarCustomerExistente() {
            // Given

            Customer customer = crearCustomer("cus-001", "manu", "manu@test.com", "quilla");
            repository.save(customer);

            Customer customerActualizado =  crearCustomer("cus-001", "ema", "manu@test.com", "bogota");


            // When
            repository.save(customerActualizado);

            // Then
            Optional<Customer> encontrado = repository.findById(CustomerId.of("cus-001"));
            assertThat(encontrado).isPresent();
            assertThat(encontrado.get().getName()).isEqualTo("ema");
            assertThat(encontrado.get().getCity()).isEqualTo("bogota");
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindByIdTests {

        @Test
        @DisplayName("Debe encontrar customer por ID existente")
        void debeEncontrarCustomeroPorIdExistente() {
            // Given
            Customer customer = crearCustomer("cus-003", "manu", "manu@test.com", "quilla");
            repository.save(customer);

            // When
            Optional<Customer> resultado = repository.findById(CustomerId.of("cus-003"));

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado.get().getName()).isEqualTo("manu");
            assertThat(resultado.get().getEmail()).isEqualTo(Email.of("manu@test.com"));
        }

        @Test
        @DisplayName("Debe retornar vacío para ID inexistente")
        void debeRetornarVacioParaIdInexistente() {
            // When
            Optional<Customer> resultado = repository.findById(CustomerId.of("no-existe"));

            // Then
            assertThat(resultado).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAllTests {

        @Test
        @DisplayName("Debe retornar lista vacía cuando no hay customers")
        void debeRetornarListaVaciaCuandoNoHayCustomeros() {
            // When
            List<Customer> resultado = repository.findAll();

            // Then
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Debe retornar todos los customers")
        void debeRetornarTodosLosCustomeros() {
            // Given
            repository.save(crearCustomer("cus-004", "manu", "manu@test.com", "quilla"));
            repository.save(crearCustomer("cus-005", "manuel", "manuel@test.com", "quilla"));
            repository.save(crearCustomer("cus-006", "manuela", "manuela@test.com", "quilla"));

            // When
            List<Customer> resultado = repository.findAll();

            // Then
            assertThat(resultado).hasSize(3);
        }
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailTests {

        @Test
        @DisplayName("Debe encontrar customers por email")
        void debeEncontrarCustomerosPorCategoria() {
            // Given
            repository.save(crearCustomer("cus-004", "manu", "manu@test.com", "quilla"));
            repository.save(crearCustomer("cus-005", "manuel", "manuel@test.com", "quilla"));
            repository.save(crearCustomer("cus-006", "manuela", "manuela@test.com", "quilla"));

            // When
            Optional<Customer> resultado = repository.findByEmail("manu@test.com");

            // Then
            assertThat(resultado).isPresent();
            assertThat(resultado).get().hasFieldOrPropertyWithValue("email",
                    Email.of("manu@test.com"));
        }

        @Test
        @DisplayName("Debe retornar lista vacía si no hay customers en email")
        void debeRetornarListaVaciaSiNoHayCustomerosEnCategoria() {
            // Given
            repository.save(crearCustomer("cus-006", "manuela", "manuela@test.com", "quilla"));

            // When
            Optional<Customer> resultado = repository.findByEmail("non-existent@test.com");

            // Then
            assertThat(resultado).isNotPresent();
        }
    }

    @Nested
    @DisplayName("existsById()")
    class ExistsByIdTests {

        @Test
        @DisplayName("Debe retornar true si customer existe")
        void debeRetornarTrueSiCustomeroExiste() {
            // Given
            repository.save(crearCustomer("cus-004", "manu", "manu@test.com", "quilla"));

            // When
            boolean existe = repository.existsById(CustomerId.of("cus-004"));

            // Then
            assertThat(existe).isTrue();
        }

        @Test
        @DisplayName("Debe retornar false si customer no existe")
        void debeRetornarFalseSiCustomeroNoExiste() {
            // When
            boolean existe = repository.existsById(CustomerId.of("no-existe"));

            // Then
            assertThat(existe).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById()")
    class DeleteByIdTests {

        @Test
        @DisplayName("Debe eliminar customer existente")
        void debeEliminarCustomeroExistente() {
            // Given
            repository.save(crearCustomer("cus-004", "manu", "manu@test.com", "quilla"));
            assertThat(repository.existsById(CustomerId.of("cus-004"))).isTrue();

            // When
            repository.deleteById(CustomerId.of("cus-004"));

            // Then
            assertThat(repository.existsById(CustomerId.of("cus-004"))).isFalse();
        }

        @Test
        @DisplayName("No debe lanzar excepción al eliminar customer inexistente")
        void noDebeLanzarExcepcionAlEliminarCustomeroInexistente() {
            // When & Then - No debería lanzar excepción
            repository.deleteById(CustomerId.of("no-existe"));
        }
    }
}
