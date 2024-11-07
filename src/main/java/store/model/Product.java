package store.model;

import store.exception.ErrorCode;

;

public class Product {
    private final String name;
    private final int price;
    private int stock;
    private String promotion;

    public Product(String name, int price, int stock, String promotion) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.promotion = promotion;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getStock() {
        return stock;
    }

    public String getPromotion() {
        return promotion;
    }

    public boolean hasPromotion() {
        return promotion != null && !promotion.equals("null");
    }

    public void decreaseStock(int quantity) {
        validateStock(quantity);
        this.stock -= quantity;
    }

    private void validateStock(int quantity) {
        if (quantity > this.stock) {
            throw new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage());
        }
    }
}
