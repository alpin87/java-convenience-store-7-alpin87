package store.model;

import store.exception.ErrorCode;

public record OrderRequest(String productName, int quantity) {
    public OrderRequest {
        validateQuantity(quantity);
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(ErrorCode.QUANTITY_SHOULD_BE_POSITIVE.getMessage());
        }
    }
}