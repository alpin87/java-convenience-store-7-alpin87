package store.service;

import store.exception.ErrorCode;
import store.model.Product;
import store.model.Promotion;
import store.util.FileReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductService {
    private static final double MEMBERSHIP_DISCOUNT_RATE = 0.3;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;

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

    public Promotion getPromotion(String productName) {
        Product product = findProduct(productName);
        return product.hasPromotion() ? promotions.get(product.getPromotion()) : null;
    }

    public boolean hasValidPromotion(String productName) {
        Product product = findProduct(productName);
        return product.hasPromotion() &&
                promotions.containsKey(product.getPromotion());
    }

    public int calculateFreeQuantity(String productName, int purchaseQuantity) {
        Product product = findProduct(productName);
        if (!hasValidPromotion(productName)) {
            return 0;
        }

        Promotion promotion = promotions.get(product.getPromotion());
        return (purchaseQuantity / promotion.getBuyQuantity()) * promotion.getFreeQuantity();
    }

    public boolean isPromotionAvailable(String productName, int quantity) {
        Product product = findProduct(productName);
        if (!hasValidPromotion(productName)) {
            return false;
        }

        Promotion promotion = promotions.get(product.getPromotion());
        return quantity >= promotion.getBuyQuantity();
    }

    public int calculatePromotionDiscount(String productName, int quantity) {
        Product product = findProduct(productName);
        if (!hasValidPromotion(productName)) {
            return 0;
        }

        int freeQuantity = calculateFreeQuantity(productName, quantity);
        return freeQuantity * product.getPrice();
    }

    public int calculateMembershipDiscount(int totalPrice, int promotionDiscount) {
        int priceAfterPromotion = totalPrice - promotionDiscount;
        int membershipDiscount = (int) (priceAfterPromotion * MEMBERSHIP_DISCOUNT_RATE);
        return Math.min(membershipDiscount, MAX_MEMBERSHIP_DISCOUNT);
    }
}
