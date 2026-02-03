package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.CustomerId;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.OrderId;

import java.time.LocalDateTime;
import java.util.*;

public class Order {

    private final OrderId id;
    private final CustomerId customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private final LocalDateTime createdAt;

    public Order(Builder builder) {
        this.id = builder.id;
        this.customerId = builder.customerId;
        this.items = builder.items.isEmpty() ? new ArrayList<>() : builder.items;
        this.status = builder.status.orElse(OrderStatus.PENDIENTE);
        this.createdAt = builder.createdAt.orElse(LocalDateTime.now());
    }

    public void addItem(OrderItem item){
        if (status == OrderStatus.PENDIENTE) {
            Optional<OrderItem> itemFound = this.items.stream().filter(
                    innerItem -> !innerItem.
                            getUnitPrice().currency()
                            .equals(item.getUnitPrice().currency())
            ).findFirst();

            if (itemFound.isPresent()) {
                throw new IllegalStateException("only same currency items is allowed");
            }
            this.items.add(item);
        } else {
            throw new IllegalStateException("only pending orders can be modified");
        }
    }

    public void remove(OrderItem item){
        if (status == OrderStatus.PENDIENTE) {
            this.items.remove(item);
        } else {
            throw new IllegalStateException("only pending orders can be removed");
        }
    }

    public void confirm() {
        if (status == OrderStatus.PENDIENTE) {
            status = OrderStatus.CONFIRMADO;
        } else {
            throw new IllegalStateException("only pending orders can be confirmed");
        }
    }

    public void ship() {
        if (status == OrderStatus.CONFIRMADO) {
            status = OrderStatus.EN_DESPACHO;
        } else {
            throw new IllegalStateException("only confirmed orders can be shipped");
        }
    }

    public void deliver() {
        if (status == OrderStatus.EN_DESPACHO) {
            status = OrderStatus.ENTREGADO;
        } else {
            throw new IllegalStateException("only shipped orders can be delivered");
        }
    }

    public Money getTotal() {
        if ( items.isEmpty() ) { return Money.zero(Currency.getInstance("COP")); }
        Currency currency = items.get(0).getUnitPrice().currency();
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(Money.zero(currency), Money::add);
    }

    public boolean isPending() {
        return status == OrderStatus.PENDIENTE;
    }

    public static Builder builder() {
        return new Builder();
    }

    public OrderId getId() {
        return id;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return new ArrayList<>(items);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static class Builder {
        private OrderId id;
        private CustomerId customerId;
        private List<OrderItem> items = new ArrayList<>();
        private Optional<OrderStatus> status = Optional.empty();
        private Optional<LocalDateTime> createdAt = Optional.empty();

        public Builder id(OrderId id){
            this.id = id;
            return this;
        }

        public Builder items(List<OrderItem> items){
            this.items = items;
            return this;
        }

        public Builder customerId(CustomerId customerId){
            this.customerId = customerId;
            return this;
        }

        public Builder addItem(OrderItem item){
            this.items.add(item);
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = Optional.of(createdAt);
            return  this;
        }

        public Builder status(OrderStatus status) {
            this.status = Optional.of(status);
            return  this;
        }


        public Order build() {
            Objects.requireNonNull(id, "id is required");
            Objects.requireNonNull(customerId, "customerId is required");
            return new Order(this);
        }
    }
}
