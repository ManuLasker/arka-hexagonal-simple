package com.enyoi.arka.domain.ports.out;

import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.valueobjects.ProductId;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findById(ProductId id);
    List<Product> findAll();
    List<Product> findByCategory(String category);
    List<Product> findLowStockProducts(int threshold);
    boolean existsById(ProductId id);
    void deleteById(ProductId id);
}
