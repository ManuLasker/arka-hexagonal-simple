package com.enyoi.arka.adapters.in;

import com.enyoi.arka.domain.entities.Customer;
import com.enyoi.arka.domain.entities.Order;
import com.enyoi.arka.domain.entities.OrderItem;
import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.exception.InsufficientStockException;
import com.enyoi.arka.domain.exception.ProductNotFoundException;
import com.enyoi.arka.domain.ports.in.OrderService;
import com.enyoi.arka.domain.ports.out.CustomerRepository;
import com.enyoi.arka.domain.ports.out.NotificationService;
import com.enyoi.arka.domain.ports.out.OrderRepository;
import com.enyoi.arka.domain.ports.out.ProductRepository;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    public OrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository,
                        CustomerRepository customerRepository, NotificationService notificationService) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.customerRepository = Objects.requireNonNull(customerRepository);
        this.notificationService = Objects.requireNonNull(notificationService);
    }

    /**
     * Crea una nueva orden de compra.
     */
    public Order createOrder(CustomerId customerId, List<OrderItem> items) {
        // Validar que el cliente existe
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Validar stock para cada item
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId().value()));

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(item.getProductId().value(),
                        item.getQuantity(), product.getStock());
            }
        }

        // Crear orden
        OrderId orderId = OrderId.of(UUID.randomUUID().toString());
        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .items(items)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Reducir stock
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId().value()));
            product.reduceStock(item.getQuantity());
            productRepository.save(product);
        }

        // Notificar
        notificationService.notifyOrderStatusChange(orderId.value(), customer.getEmail().value(), "PENDIENTE");

        return savedOrder;
    }

    /**
     * Obtiene una orden por ID.
     */
    public Order getOrderById(OrderId id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @Override
    public List<Order> getOrdersByCustomerId(CustomerId customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    /**
     * Confirma una orden pendiente.
     */
    public Order confirmOrder(OrderId id) {
        Order order = getOrderById(id);
        order.confirm();
        Order savedOrder = orderRepository.save(order);

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        notificationService.notifyOrderStatusChange(id.value(), customer.getEmail().value(), "CONFIRMADO");

        return savedOrder;
    }

    /**
     * Agrega un item a una orden pendiente.
     */
    public Order addItemToOrder(OrderId orderId, OrderItem item) {
        Order order = getOrderById(orderId);
        if (!order.isPending()) {
            throw new IllegalStateException("Can only modify pending orders");
        }

        // Validar stock
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId().value()));

        if (product.getStock() < item.getQuantity()) {
            throw new InsufficientStockException(item.getProductId().value(),
                    item.getQuantity(), product.getStock());
        }

        order.addItem(item);
        return orderRepository.save(order);
    }

    /**
     * Remueve un item de una orden pendiente.
     */
    public Order removeItemFromOrder(OrderId orderId, OrderItem item) {
        Order order = getOrderById(orderId);
        if (!order.isPending()) {
            throw new IllegalStateException("Can only modify pending orders");
        }

        order.remove(item);
        // Devolver stock al producto
        Product product = productRepository.findById(item.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(item.getProductId().value()));

        product.increaseStock(item.getQuantity());
        productRepository.save(product);

        return orderRepository.save(order);
    }

    /**
     * Envía una orden confirmada.
     */
    public Order shipOrder(OrderId id) {
        Order order = getOrderById(id);
        order.ship();
        Order savedOrder = orderRepository.save(order);

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        notificationService.notifyOrderStatusChange(id.value(), customer.getEmail().value(), "EN_DESPACHO");

        return savedOrder;
    }

    /**
     * Entrega una orden enviada.
     */
    public Order deliverOrder(OrderId id) {
        Order order = getOrderById(id);
        order.deliver();
        Order savedOrder = orderRepository.save(order);

        Customer customer = customerRepository.findById(order.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        notificationService.notifyOrderStatusChange(id.value(), customer.getEmail().value(), "ENTREGADO");

        return savedOrder;
    }

    /**
     * Lista todas las órdenes.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
