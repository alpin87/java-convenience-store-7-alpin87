package store.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import store.exception.ErrorCode;
import store.model.Product;

public class FileReader {
    private static final String PRODUCTS_PATH = "src/main/resources/products.md";
    private static final String PROMOTIONS_PATH = "src/main/resources/promotions.md";

    public List<String> readProducts() {
        try {
            return Files.readAllLines(Paths.get(PRODUCTS_PATH));
        } catch (IOException e) {
            throw new IllegalStateException(ErrorCode.NOT_FOUND_PRODUCT.getMessage());
        }
    }

    public List<String> readPromotions() {
        try {
            return Files.readAllLines(Paths.get(PROMOTIONS_PATH));
        } catch (IOException e) {
            throw new IllegalStateException(ErrorCode.NOT_FOUND_PROMOTION.getMessage());
        }
    }

    private List<Product> parseProducts(List<String> lines) {
        List<Product> products = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            products.add(new Product(
                    parts[0],                    // name
                    Integer.parseInt(parts[1]),  // price
                    Integer.parseInt(parts[2]),  // quantity
                    parts[3]                     // promotion
            ));
        }
        return products;
    }
}
