package store.model;

import store.exception.ErrorCode;

public class Order {
    private final Product product;
    private final int quantity;

    public Order(Product product, int quantity) {
        validateOrder(product, quantity);
        this.product = product;
        this.quantity = quantity;
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

    private void validateOrder(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException(ErrorCode.NON_EXISTENT_PRODUCT.getMessage());
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(ErrorCode.QUANTITY_SHOULD_BE_POSITIVE.getMessage());
        }
    }
}
