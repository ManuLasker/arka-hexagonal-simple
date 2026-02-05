package com.enyoi.arka.domain.ports.in;

import com.enyoi.arka.domain.entities.Product;
import com.enyoi.arka.domain.valueobjects.Money;
import com.enyoi.arka.domain.valueobjects.ProductId;

import java.util.List;

public interface InventoryService {
    Product registerProduct(String name, String description, Money price, int stock, String category);
    Product getProductById(ProductId id);
    List<Product> getAllProducts();
    Product updateStock(ProductId id, int newStock);
    void reduceStock(ProductId id, int quantity);
    List<Product> getLowStockProducts();
    void generateRestockReport();
}
