package store.model;

import store.exception.ErrorCode;
import store.validator.Validator;
import java.util.Optional;

public class Product {
    private final String name;
    private final int price;
    private int promotionStock;
    private int normalStock;
    private final String promotion;

    public Product(String name, int price, int stock, String promotion) {
        Validator.validateNotBlank(name, ErrorCode.NON_EXISTENT_PRODUCT);
        Validator.validatePositiveNumber(price);

        this.name = name;
        this.price = price;
        initializeStock(stock, promotion);
        this.promotion = promotion;
    }

    private void initializeStock(int stock, String promotion) {
        this.promotionStock = Optional.ofNullable(promotion)
                .filter(p -> !p.equals("null"))
                .map(p -> stock)
                .orElse(0);

        this.normalStock = Optional.ofNullable(promotion)
                .filter(p -> !p.equals("null"))
                .map(p -> 0)
                .orElse(stock);
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getTotalStock() {
        return promotionStock + normalStock;
    }

    public int getPromotionalStock() {
        return promotionStock;
    }

    public int getNormalStock() {
        return normalStock;
    }

    public String getPromotion() {
        return promotion;
    }

    public boolean hasPromotion() {
        return Optional.ofNullable(promotion)
                .filter(p -> !p.equals("null"))
                .isPresent();
    }

    public void transferPromotionStockToNormal(int quantity) {
        promotionStock -= quantity;
        normalStock += quantity;
    }

    public void decreaseNormalStock(int quantity) {
        normalStock -= quantity;
    }

    public void decreasePromotionStock(int quantity) {
        promotionStock -= quantity;
    }

    public String getStockText() {
        return Optional.of(getTotalStock())
                .filter(stock -> stock > 0)
                .map(stock -> stock + "개")
                .orElse("재고 없음");
    }

    public boolean canFulfillOrder(int quantity) {
        return getTotalStock() >= quantity;
    }

    public boolean hasAvailableNormalStock(int quantity) {
        return normalStock >= quantity;
    }
}