package store.exception;

public enum ErrorCode {
    INVENTORY_QUANTITY_EXCEEDED("[ERROR] 재고 수량을 초과하여 구매할 수 없습니다."),
    MEMBERSHIP_YES_OR_NO_CHECK("[ERROR] Y 또는 N만 입력 가능합니다."),
    FAILED_TO_READ_PRODUCT("[ERROR] 상품 정보를 불러오는데 실패했습니다."),
    FAILED_TO_READ_PROMOTION("[ERROR] 프로모션 정보를 불러오는데 실패했습니다."),
    NON_EXISTENT_PRODUCT("[ERROR] 존재하지 않는 상품입니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
