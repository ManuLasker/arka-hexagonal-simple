package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;

import java.util.Objects;

public class OrderItem {
    private final ProductId productId;
    private final int quantity;
    private final Money unitPrice;

    public OrderItem(Builder builder) {
        this.productId = builder.productId;
        this.quantity = builder.quantity;
        this.unitPrice = builder.unitPrice;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getQuantity() {
        return quantity;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public Money getTotalPrice() {
        return unitPrice.multiply(quantity);
    }

    public static class Builder {

        private ProductId productId;
        private int quantity;
        private Money unitPrice;

        public Builder productId(ProductId productId) {
            this.productId = productId;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitPrice(Money unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public OrderItem build() {
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
            Objects.requireNonNull(unitPrice, "Unit price must not be null");
            Objects.requireNonNull(productId, "Product id must not be null");
            return new OrderItem(this);
        }
    }
}
