package com.enyoi.arka.domain.entities;

import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;

import java.util.Objects;

public class Product {
    private final ProductId id;
    private final String name;
    private final String description;
    private final Money price;
    private int stock;
    private final ProductCategory category;

    public Product(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.stock = builder.stock;
        this.category = builder.category;

    }

    public void reduceStock(int stockToReduce) {
        if (stockToReduce > this.stock ) throw new IllegalArgumentException("stock exceeded");
        this.stock -= stockToReduce;
    }

    public void increaseStock(int stockToIncrease) {
        this.stock += stockToIncrease;
    }

    public boolean isLowStock(int lowStock) {
        return this.stock < lowStock;
    }

    public static Builder builder() {
        return new Builder();
    }

    public ProductId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public static class Builder {
        private ProductId id;
        private String name;
        private String description;
        private Money price;
        private int stock;
        private ProductCategory category;

        public Builder id(ProductId id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(Money price) {
            this.price = price;
            return this;
        }

        public Builder stock(int stock) {
            this.stock = stock;
            return this;
        }

        public Builder category(ProductCategory category) {
            this.category = category;
            return this;
        }

        public Product build() {
            Objects.requireNonNull(id, "id is required");
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(price, "price is required");
            Objects.requireNonNull(category, "category is required");
            return new Product(this);
        }

    }
}
