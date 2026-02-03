package com.enyoi.arka.adapters.out.repository;

import com.enyoi.arka.adapters.out.repository.entity.OrderEntity;
import com.enyoi.arka.adapters.out.repository.entity.OrderItemEntity;
import com.enyoi.arka.domain.entities.Order;
import com.enyoi.arka.domain.entities.OrderItem;
import com.enyoi.arka.domain.entities.OrderStatus;
import com.enyoi.arka.domain.ports.out.OrderRepository;
import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.OrderId;
import com.enyoi.arka.domain.valueobjects.ProductId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class JpaOrderRepository implements OrderRepository {
    private final EntityManager entityManager;


    public JpaOrderRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Order save(Order order) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            OrderEntity orderEntity = toEntity(order);
            entityManager.merge(orderEntity);
            transaction.commit();
            return order;
        } catch (Exception e) {
            if (transaction.isActive())  transaction.rollback();
            throw e;
        }
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(
                entityManager.find(OrderEntity.class, id.value())
        ).map(this::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return entityManager.createQuery(
                "FROM OrderEntity",
                OrderEntity.class
        ).getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByCustomerId(CustomerId customerId) {
        return entityManager.createQuery(
                "FROM OrderEntity WHERE customerId = :customerId",
                OrderEntity.class
        ).setParameter("customerId", customerId.value())
                .getResultList()
                .stream().map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return entityManager.createQuery(
                "FROM OrderEntity WHERE status = :status",
                OrderEntity.class
        ).setParameter("status", status)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Order> findPendingOrders() {
        return findByStatus(OrderStatus.PENDIENTE);
    }

    @Override
    public boolean existsById(OrderId id) {
        return findById(id).isPresent();
    }

    @Override
    public void deleteById(OrderId id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            OrderEntity orderEntity = entityManager.find(OrderEntity.class, id.value());
            if (orderEntity != null) {
                entityManager.remove(orderEntity);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }
    }

    private OrderEntity toEntity(Order order) {
        return new  OrderEntity(
                order.getId().value(),
                order.getCustomerId().value(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getItems()
                        .stream()
                        .map(orderItem -> toEntity(order.getId(),
                                orderItem))
                        .toList()
        );
    }

    private OrderItemEntity toEntity(OrderId id, OrderItem item) {
        return new OrderItemEntity(
                id.value(),
                item.getProductId().value(),
                item.getQuantity(),
                item.getUnitPrice().amount(),
                item.getUnitPrice().currency().toString()
        );
    }

    private Order toDomain(OrderEntity entity) {
        return Order.builder()
                .id(OrderId.of(entity.getId()))
                .customerId(CustomerId.of(entity.getCustomerId()))
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .items(entity.getItems().stream()
                        .map(this::toDomain)
                        .toList())
                .build();
    }

    private OrderItem toDomain(OrderItemEntity entity) {
        return OrderItem.builder()
                .productId(ProductId.of(entity.getProductId()))
                .quantity(entity.getQuantity())
                .unitPrice(Money.of(entity.getUnitPrice(), entity.getCurrency()))
                .build();
    }
}
