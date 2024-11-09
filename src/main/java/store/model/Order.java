package store.model;

import store.exception.ErrorCode;
import store.model.Product;
import store.validator.Validator;

public class Order {
    private final Product product;
    private final int quantity;
    private final boolean isPromotional;

    public Order(Product product, int quantity, boolean isPromotional) {
        validateOrderDetails(product, quantity);
        this.product = product;
        this.quantity = quantity;
        this.isPromotional = isPromotional;
    }

    public boolean isPromotional() {
        return isPromotional;
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

    public int calculateTotalPrice() {
        return product.getPrice() * quantity;
    }
}