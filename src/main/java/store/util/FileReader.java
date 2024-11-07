package store.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import store.exception.ErrorCode;
import store.model.Product;
import store.model.Promotion;

public class FileReader {
    private static final String PRODUCTS_PATH = "src/main/resources/products.md";
    private static final String PROMOTIONS_PATH = "src/main/resources/promotions.md";

    public List<Product> getProducts() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PRODUCTS_PATH));
            return parseProducts(lines);
        } catch (IOException e) {
            throw new IllegalStateException(ErrorCode.FAILED_TO_READ_PRODUCT.getMessage());
        }
    }

    public List<Promotion> getPromotions() {
        try {
            List<String> lines = Files.readAllLines(Paths.get(PROMOTIONS_PATH));
            return parsePromotions(lines);
        } catch (IOException e) {
            throw new IllegalStateException(ErrorCode.FAILED_TO_READ_PRODUCT.getMessage());
        }
    }

    private List<Product> parseProducts(List<String> lines) {
        List<Product> products = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            products.add(createProduct(lines.get(i)));
        }
        return products;
    }

    private Product createProduct(String line) {
        String[] parts = line.split(",");
        return new Product(
                parts[0],
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                parts[3]
        );
    }

    private List<Promotion> parsePromotions(List<String> lines) {
        List<Promotion> promotions = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            promotions.add(createPromotion(lines.get(i)));
        }
        return promotions;
    }

    private Promotion createPromotion(String line) {
        String[] parts = line.split(",");
        return new Promotion(
                parts[0],
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2]),
                parseDateTime(parts[3]),
                parseDateTime(parts[4])
        );
    }

    private LocalDateTime parseDateTime(String date) {
        return LocalDateTime.parse(date + "T00:00:00");
    }
}