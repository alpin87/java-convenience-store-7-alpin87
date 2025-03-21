package store.model;

import store.exception.ErrorCode;
import java.util.Arrays;
import java.util.Optional;

public enum PromotionType {
    BUY_2_GET_1("탄산2+1", 2, 1),
    MD_RECOMMENDATION("MD추천상품", 1, 1),
    FLASH_SALE("반짝할인", 1, 1);

    private final String name;
    private final int buyQuantity;
    private final int freeQuantity;

    PromotionType(String name, int buyQuantity, int freeQuantity) {
        this.name = name;
        this.buyQuantity = buyQuantity;
        this.freeQuantity = freeQuantity;
    }

    public String getName() {
        return name;
    }

    public int getBuyQuantity() {
        return buyQuantity;
    }

    public int getFreeQuantity() {
        return freeQuantity;
    }

    public static PromotionType fromName(String name) {
        return Arrays.stream(values())
                .filter(type -> type.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVALID_PROMOTION_NAME.getMessage()));
    }

    public boolean shouldAskForPromotion() {
        return this == MD_RECOMMENDATION;
    }
}