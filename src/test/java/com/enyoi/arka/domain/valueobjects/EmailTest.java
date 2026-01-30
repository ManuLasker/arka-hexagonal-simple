package com.enyoi.arka.domain.valueobjects;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Email - Value Object")
class EmailTest {

   @Nested
   @DisplayName("of()")
   class OfTests {

       @Test
       @DisplayName("Debe crear Email con valor válido")
       void debeCrearEmailConValorValido() {
           // When
           Email email = Email.of("usuario@dominio.com");

           // Then
           assertThat(email).isNotNull();
           assertThat(email.value()).isEqualTo("usuario@dominio.com");
       }

       @ParameterizedTest
       @ValueSource(strings = {
               "test@example.com",
               "user.name@domain.co",
               "user+tag@example.org",
               "test123@test.com"
       })
       @DisplayName("Debe aceptar emails válidos")
       void debeAceptarEmailsValidos(String emailValue) {
           // When
           Email email = Email.of(emailValue);

           // Then
           assertThat(email.value()).isEqualTo(emailValue);
       }

       @ParameterizedTest
       @ValueSource(strings = {
               "invalido",
               "sin-arroba.com",
               "@sinusuario.com",
               "usuario@",
               "usuario@.com",
               "usuario@dominio",
               ""
       })
       @DisplayName("Debe rechazar emails inválidos")
       void debeRechazarEmailsInvalidos(String emailInvalido) {
           // When & Then
           assertThatThrownBy(() -> Email.of(emailInvalido))
                   .isInstanceOf(IllegalArgumentException.class)
                   .hasMessageContaining("Invalid email");
       }
   }

   @Nested
   @DisplayName("Igualdad")
   class IgualdadTests {

       @Test
       @DisplayName("Dos Email con mismo valor deben ser iguales")
       void dosEmailConMismoValorDebenSerIguales() {
           // Given
           Email email1 = Email.of("test@example.com");
           Email email2 = Email.of("test@example.com");

           // Then
           assertThat(email1).isEqualTo(email2);
           assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
       }

       @Test
       @DisplayName("Dos Email con diferente valor no deben ser iguales")
       void dosEmailConDiferenteValorNoDebenSerIguales() {
           // Given
           Email email1 = Email.of("test1@example.com");
           Email email2 = Email.of("test2@example.com");

           // Then
           assertThat(email1).isNotEqualTo(email2);
       }
   }
}
