package com.enyoi.arka.domain.ports.out;

import com.enyoi.arka.domain.entities.Order;
import com.enyoi.arka.domain.entities.OrderStatus;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.OrderId;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId id);
    List<Order> findAll();
    List<Order> findByCustomerId(CustomerId customerId);
    List<Order> findByStatus(OrderStatus status);
    List<Order> findPendingOrders();
    boolean existsById(OrderId id);
    void deleteById(OrderId id);
}
