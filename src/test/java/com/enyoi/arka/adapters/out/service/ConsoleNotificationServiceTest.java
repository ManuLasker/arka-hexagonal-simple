package com.enyoi.arka.adapters.out.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ConsoleNotificationService - Tests")
class ConsoleNotificationServiceTest {

   private ConsoleNotificationService notificationService;
   private ByteArrayOutputStream outputStream;
   private PrintStream originalOut;

   @BeforeEach
   void setUp() {
       notificationService = new ConsoleNotificationService();
       outputStream = new ByteArrayOutputStream();
       originalOut = System.out;
       System.setOut(new PrintStream(outputStream));
   }

   @org.junit.jupiter.api.AfterEach
   void tearDown() {
       System.setOut(originalOut);
   }

   @Test
   @DisplayName("Debe notificar cambio de estado de orden")
   void debeNotificarCambioDeEstadoDeOrden() {
       // When
       notificationService.notifyOrderStatusChange("order-001", "cliente@email.com", "CONFIRMADO");

       // Then
       String output = outputStream.toString();
       assertThat(output).contains("cliente@email.com");
       assertThat(output).contains("order-001");
       assertThat(output).contains("CONFIRMADO");
   }

   @Test
   @DisplayName("Debe notificar alerta de stock bajo")
   void debeNotificarAlertaDeStockBajo() {
       // When
       notificationService.notifyLowStockAlert("Teclado Mecánico", 5);

       // Then
       String output = outputStream.toString();
       assertThat(output).contains("Teclado Mecánico");
       assertThat(output).contains("5");
       assertThat(output).contains("stock bajo");
   }
}
