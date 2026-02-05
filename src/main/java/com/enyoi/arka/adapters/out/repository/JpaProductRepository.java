package com.enyoi.arka.adapters.out.repository;

import com.enyoi.arka.adapters.out.repository.entity.OrderEntity;
import com.enyoi.arka.adapters.out.repository.entity.ProductEntity;
import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.entities.ProductCategory;
import com.enyoi.arka.domain.ports.out.ProductRepository;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

public class JpaProductRepository implements ProductRepository {
    private final EntityManager entityManager;

    public JpaProductRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Product save(Product product) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            ProductEntity productEntity = this.toEntity(product);
            entityManager.merge(productEntity);
            transaction.commit();
            return product;
        } catch (Exception ex) {
            if(transaction.isActive()) transaction.rollback();
            throw ex;
        }
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(
                entityManager.find(ProductEntity.class, id.value())
        ).map(this::toDomain);
    }

    @Override
    public List<Product> findAll() {
        return entityManager.createQuery(
                        "FROM ProductEntity",
                        ProductEntity.class
                ).getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> findByCategory(String category) {
        return entityManager.createQuery(
                        "FROM ProductEntity WHERE category = :category",
                        ProductEntity.class
                ).setParameter("category", ProductCategory.valueOf(category))
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Product> findLowStockProducts(int threshold) {
        return entityManager.createQuery(
                        "FROM ProductEntity WHERE stock < :threshold",
                        ProductEntity.class
                ).setParameter("threshold", threshold)
                .getResultList()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsById(ProductId id) {
        return findById(id).isPresent();
    }

    @Override
    public void deleteById(ProductId id) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            ProductEntity productEntity = entityManager.find(ProductEntity.class, id.value());
            if (productEntity != null) {
                entityManager.remove(productEntity);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction.isActive()) transaction.rollback();
            throw e;
        }

    }

    private Product toDomain(ProductEntity entity) {
        return Product.builder()
                .id(ProductId.of(entity.getId()))
                .stock(entity.getStock())
                .name(entity.getName())
                .description(entity.getDescription())
                .price(Money.of(entity.getPrice(), entity.getCurrency()))
                .category(entity.getCategory())
                .build();
    }

    private ProductEntity toEntity(Product product) {
        return new ProductEntity(
                product.getId().value(),
                product.getName(),
                product.getDescription(),
                product.getPrice().amount(),
                product.getPrice().currency().toString(),
                product.getStock(),
                product.getCategory()
        );
    }
}
