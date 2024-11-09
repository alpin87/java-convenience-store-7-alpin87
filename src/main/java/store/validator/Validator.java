package store.validator;

import store.exception.ErrorCode;
import java.util.Optional;

public class Validator {
    public static void validatePositiveNumber(int number) {
        Optional.of(number)
                .filter(n -> n > 0)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.QUANTITY_SHOULD_BE_POSITIVE.getMessage()));
    }

    public static void validateNotNull(Object value, ErrorCode errorCode) {
        Optional.ofNullable(value)
                .orElseThrow(() -> new IllegalArgumentException(errorCode.getMessage()));
    }

    public static void validateNotBlank(String value, ErrorCode errorCode) {
        Optional.ofNullable(value)
                .filter(v -> !v.trim().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(errorCode.getMessage()));
    }

    public static void validateTotalStock(int requestedQuantity, int promotionalStock, int normalStock) {
        Optional.of(requestedQuantity)
                .filter(qty -> qty <= (promotionalStock + normalStock))
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));
    }

    public static void validatePromotionalStock(int requestedQuantity, int availableStock) {
        Optional.of(requestedQuantity)
                .filter(qty -> qty <= availableStock)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));
    }

    public static void validateNormalStock(int requestedQuantity, int availableStock) {
        Optional.of(requestedQuantity)
                .filter(qty -> qty <= availableStock)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));
    }
}