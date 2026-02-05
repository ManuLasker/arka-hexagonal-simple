package com.enyoi.arka;

import com.enyoi.arka.adapters.in.InventoryServiceImpl;
import com.enyoi.arka.adapters.in.OrderServiceImpl;
import com.enyoi.arka.adapters.out.repository.JpaCustomerRepository;
import com.enyoi.arka.adapters.out.repository.JpaOrderRepository;
import com.enyoi.arka.adapters.out.repository.JpaProductRepository;
import com.enyoi.arka.adapters.out.repository.config.DatabaseConfig;
import com.enyoi.arka.adapters.out.service.ConsoleNotificationService;
import com.enyoi.arka.domain.entities.*;
import com.enyoi.arka.domain.ports.in.InventoryService;
import com.enyoi.arka.domain.ports.in.OrderService;
import com.enyoi.arka.domain.ports.out.CustomerRepository;
import com.enyoi.arka.domain.ports.out.NotificationService;
import com.enyoi.arka.domain.ports.out.OrderRepository;
import com.enyoi.arka.domain.ports.out.ProductRepository;
import com.enyoi.arka.domain.valueobjects.*;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class ArkaApplication {
    private static final Scanner scanner = new Scanner(System.in);

    // Servicios
    private static InventoryService inventoryService;
    private static OrderService orderService;
    private static CustomerRepository customerRepository;


    public static void main(String[] args) {
        initializeServices();

        System.out.println("Bienvenido a Arka - Sistema de Distribución");
        System.out.println("==============================================");

        boolean running = true;
        while (running) {
            showMenu();
            int option = readInt("Seleccione una opción: ");

            switch (option) {
                case 1 -> manageInventory();
                case 2 -> manageCustomers();
                case 3 -> manageOrders();
                case 0 -> {
                    System.out.println("¡Hasta luego!");
                    running = false;
                }
                default -> System.out.println("Opción inválida");
            }
        }

        DatabaseConfig.shutdown();
    }

    private static void initializeServices() {
        // EntityManager compartido
        EntityManager entityManager = DatabaseConfig.createEntityManager();

        // Repositorios
        ProductRepository productRepo = new JpaProductRepository(entityManager);
        CustomerRepository customerRepo = new JpaCustomerRepository(entityManager);
        OrderRepository orderRepo = new JpaOrderRepository(entityManager);

        // Notificaciones
        NotificationService notificationService = new ConsoleNotificationService();

        // Servicios
        inventoryService = new InventoryServiceImpl(productRepo, notificationService);
        orderService = new OrderServiceImpl(orderRepo, productRepo, customerRepo, notificationService);
        customerRepository = customerRepo;
    }

    private static void showMenu() {
        System.out.println("\nMENÚ PRINCIPAL");
        System.out.println("1. Gestión de Inventario");
        System.out.println("2. Gestión de Clientes");
        System.out.println("3. Gestión de Órdenes");
        System.out.println("0. Salir");
        System.out.println("==================");
    }

    private static void manageInventory() {
        System.out.println("\nGESTIÓN DE INVENTARIO");
        System.out.println("1. Registrar producto");
        System.out.println("2. Ver productos");
        System.out.println("3. Actualizar stock");
        System.out.println("4. Ver productos con stock bajo");
        System.out.println("0. Volver");

        int option = readInt("Seleccione: ");
        switch (option) {
            case 1 -> registerProduct();
            case 2 -> viewProducts();
            case 3 -> updateStock();
            case 4 -> viewLowStock();
            case 0 -> {}
            default -> System.out.println("Opción inválida");
        }
    }

    private static void manageCustomers() {
        System.out.println("\nGESTIÓN DE CLIENTES");
        System.out.println("1. Registrar cliente");
        System.out.println("2. Ver clientes");
        System.out.println("0. Volver");

        int option = readInt("Seleccione: ");
        switch (option) {
            case 1 -> registerCustomer();
            case 2 -> viewCustomers();
            case 0 -> {}
            default -> System.out.println("Opción inválida");
        }
    }

    private static void manageOrders() {
        System.out.println("\nGESTIÓN DE ÓRDENES");
        System.out.println("1. Crear orden");
        System.out.println("2. Ver órdenes pendientes");
        System.out.println("3. Confirmar orden");
        System.out.println("0. Volver");

        int option = readInt("Seleccione: ");
        switch (option) {
            case 1 -> createOrder();
            case 2 -> viewPendingOrders();
            case 3 -> confirmOrder();
            case 0 -> {}
            default -> System.out.println("Opción inválida");
        }
    }


    // Métodos de implementación
    private static void registerProduct() {
        String name = readString("Nombre del producto: ");
        String description = readString("Descripción: ");
        BigDecimal priceAmount = new BigDecimal(readString("Precio: "));
        Money price = Money.of(priceAmount, "COP");
        int stock = readInt("Stock inicial: ");

        System.out.println("Categorías disponibles:");
        for (ProductCategory cat : ProductCategory.values()) {
            System.out.println("- " + cat.name());
        }
        String categoryStr = readString("Categoría: ");

        try {
            Product product = inventoryService.registerProduct(name, description, price, stock, categoryStr);
            System.out.println("Producto registrado: " + product.getName());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewProducts() {
        List<Product> products = inventoryService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No hay productos registrados.");
            return;
        }
        System.out.println("PRODUCTOS REGISTRADOS:");
        for (Product p : products) {
            System.out.println("- " + p.getName() + " (ID: " + p.getId().value() + ") - Stock: " + p.getStock() + " - Precio: " + p.getPrice());
        }
    }

    private static void updateStock() {
        String productIdStr = readString("ID del producto: ");
        ProductId productId = ProductId.of(productIdStr);
        int newStock = readInt("Nuevo stock: ");
        try {
            Product product = inventoryService.updateStock(productId, newStock);
            System.out.println("Stock actualizado para " + product.getName() + ": " + product.getStock());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewLowStock() {
        List<Product> lowStock = inventoryService.getLowStockProducts();
        if (lowStock.isEmpty()) {
            System.out.println("No hay productos con stock bajo.");
            return;
        }
        System.out.println("PRODUCTOS CON STOCK BAJO:");
        for (Product p : lowStock) {
            System.out.println("- " + p.getName() + " - Stock: " + p.getStock());
        }
    }

    private static void createOrder() {
        String customerIdStr = readString("ID del cliente: ");
        CustomerId customerId = CustomerId.of(customerIdStr);
        String productIdStr = readString("ID del producto: ");
        ProductId productId = ProductId.of(productIdStr);
        int quantity = readInt("Cantidad: ");
        try {
            Product product = inventoryService.getProductById(productId);
            OrderItem item = OrderItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build();
            List<OrderItem> items = List.of(item);
            Order order = orderService.createOrder(customerId, items);
            System.out.println("Orden creada: " + order.getId().value());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewPendingOrders() {
        List<Order> orders = orderService.getAllOrders().stream()
                .filter(Order::isPending)
                .toList();
        if (orders.isEmpty()) {
            System.out.println("No hay órdenes pendientes.");
            return;
        }
        System.out.println("ÓRDENES PENDIENTES:");
        for (Order o : orders) {
            System.out.println("- ID: " + o.getId().value() + " - Total: " + o.getTotal());
        }
    }

    private static void confirmOrder() {
        String orderIdStr = readString("ID de la orden: ");
        OrderId orderId = OrderId.of(orderIdStr);
        try {
            Order order = orderService.confirmOrder(orderId);
            System.out.println("Orden confirmada: " + order.getId().value());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private static void registerCustomer() {
        System.out.println("\nREGISTRAR CLIENTE");

        String name = readString("Nombre del cliente: ");
        String emailStr = readString("Email: ");
        String city = readString("Ciudad: ");

        try {
            Email email = Email.of(emailStr);
            CustomerId id = CustomerId.of(java.util.UUID.randomUUID().toString());
            Customer customer = Customer.builder()
                    .id(id)
                    .name(name)
                    .email(email)
                    .city(city)
                    .build();
            customerRepository.save(customer);
            System.out.println("Cliente registrado: " + customer.getName() + " (ID: " + customer.getId().value() + ")");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewCustomers() {
        List<Customer> customers = customerRepository.findAll();
        if (customers.isEmpty()) {
            System.out.println("No hay clientes registrados.");
            return;
        }
        System.out.println("CLIENTES REGISTRADOS:");
        for (Customer c : customers) {
            System.out.println("- " + c.getName() + " (ID: " + c.getId().value() + ") - Email: " + c.getEmail().value() + " - Ciudad: " + c.getCity());
        }
    }

    private static int readInt(String prompt) {
        System.out.print(prompt);
        while (!scanner.hasNextInt()) {
            System.out.println("Por favor ingrese un número válido");
            System.out.print(prompt);
            scanner.next();
        }
        int value = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return value;
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }
}
