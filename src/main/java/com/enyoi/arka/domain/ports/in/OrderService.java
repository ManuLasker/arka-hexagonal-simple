package com.enyoi.arka.domain.ports.in;

import com.enyoi.arka.domain.entities.Order;
import com.enyoi.arka.domain.entities.OrderItem;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.OrderId;

import java.util.List;

public interface OrderService {
    Order createOrder(CustomerId customerId, List<OrderItem> items);
    Order getOrderById(OrderId id);
    List<Order> getOrdersByCustomerId(CustomerId customerId);
    Order confirmOrder(OrderId id);
    Order addItemToOrder(OrderId id, OrderItem item);
    Order removeItemFromOrder(OrderId id, OrderItem item);
    Order shipOrder(OrderId id);
    Order deliverOrder(OrderId id);
    List<Order> getAllOrders();
}
