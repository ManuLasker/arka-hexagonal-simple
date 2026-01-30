package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Email;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Customer - Entidad de Dominio")
class CustomerTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTests {

        @Test
        @DisplayName("Debe crear cliente con todos los campos")
        void debeCrearClienteConTodosLosCampos() {
            // When
            Customer cliente = Customer.builder()
                    .id(CustomerId.of("cust-001"))
                    .name("Juan Pérez")
                    .email(Email.of("juan@email.com"))
                    .city("Bogotá")
                    .build();

            // Then
            assertThat(cliente.getId().value()).isEqualTo("cust-001");
            assertThat(cliente.getName()).isEqualTo("Juan Pérez");
            assertThat(cliente.getEmail().value()).isEqualTo("juan@email.com");
            assertThat(cliente.getCity()).isEqualTo("Bogotá");
        }

        @Test
        @DisplayName("Debe fallar sin ID")
        void debeFallarSinId() {
            assertThatThrownBy(() -> Customer.builder()
                    .name("Juan")
                    .email(Email.of("juan@email.com"))
                    .city("Bogotá")
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("id is required");
        }

        @Test
        @DisplayName("Debe fallar sin nombre")
        void debeFallarSinNombre() {
            assertThatThrownBy(() -> Customer.builder()
                    .id(CustomerId.of("cust-001"))
                    .email(Email.of("juan@email.com"))
                    .city("Bogotá")
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("name is required");
        }

        @Test
        @DisplayName("Debe fallar sin email")
        void debeFallarSinEmail() {
            assertThatThrownBy(() -> Customer.builder()
                    .id(CustomerId.of("cust-001"))
                    .name("Juan")
                    .city("Bogotá")
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("email is required");
        }

        @Test
        @DisplayName("Debe fallar sin ciudad")
        void debeFallarSinCiudad() {
            assertThatThrownBy(() -> Customer.builder()
                    .id(CustomerId.of("cust-001"))
                    .name("Juan")
                    .email(Email.of("juan@email.com"))
                    .build())
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("city is required");
        }
    }

    @Nested
    @DisplayName("Getters")
    class GettersTests {

        @Test
        @DisplayName("Debe retornar todos los valores correctamente")
        void debeRetornarTodosLosValoresCorrectamente() {
            // Given
            Customer cliente = Customer.builder()
                    .id(CustomerId.of("cust-002"))
                    .name("María García")
                    .email(Email.of("maria@email.com"))
                    .city("Medellín")
                    .build();

            // Then
            assertThat(cliente.getId()).isNotNull();
            assertThat(cliente.getId().value()).isEqualTo("cust-002");
            assertThat(cliente.getName()).isEqualTo("María García");
            assertThat(cliente.getEmail()).isNotNull();
            assertThat(cliente.getEmail().value()).isEqualTo("maria@email.com");
            assertThat(cliente.getCity()).isEqualTo("Medellín");
        }
    }
}
