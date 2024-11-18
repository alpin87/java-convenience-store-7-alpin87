package store.model;

import store.exception.ErrorCode;
import store.validator.Validator;

public class Order {
    private final Product product;
    private final int quantity;
    private final boolean isPromotional;
    private final boolean isPromotionalGift;

    public Order(Product product, int quantity, boolean isPromotional) {
        this(product, quantity, isPromotional, false);
    }

    public Order(Product product, int quantity, boolean isPromotional, boolean isPromotionalGift) {
        validateOrderDetails(product, quantity);
        this.product = product;
        this.quantity = quantity;
        this.isPromotional = isPromotional;
        this.isPromotionalGift = isPromotionalGift;
    }

    private void validateOrderDetails(Product product, int quantity) {
        Validator.validateNotNull(product, ErrorCode.NON_EXISTENT_PRODUCT);
        Validator.validatePositiveNumber(quantity);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isPromotional() {
        return isPromotional;
    }

    public boolean isPromotionalGift() {
        return isPromotionalGift;
    }

    public int calculateTotalPrice() {
        if (isPromotionalGift) {
            return 0;
        }
        return product.getPrice() * quantity;
    }
}