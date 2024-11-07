package store.service;

import store.exception.ErrorCode;
import store.model.Product;
import store.model.Promotion;
import store.util.FileReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class ProductService {
    private final List<Product> products;
    private final Map<String, Promotion> promotions;

    public ProductService(FileReader fileReader) {
        this.products = fileReader.getProducts();
        this.promotions = fileReader.getPromotions().stream()
                .collect(Collectors.toMap(Promotion::getName, promotion -> promotion));
    }

    public List<Product> getProducts() {
        return products;
    }

    public Product findProduct(String name) {
        return products.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NON_EXISTENT_PRODUCT.getMessage()));
    }

    public void decreaseStock(String productName, int quantity) {
        Product product = findProduct(productName);
        product.decreaseStock(quantity);
    }

    public boolean checkStock(String productName, int quantity) {
        Product product = findProduct(productName);
        return product.getStock() >= quantity;
    }
}
