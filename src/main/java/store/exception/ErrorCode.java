package store.exception;

public enum ErrorCode {
    NON_EXISTENT_PRODUCT("존재하지 않는 상품입니다. 다시 입력해 주세요."),
    QUANTITY_SHOULD_BE_POSITIVE("[ERROR] 수량은 0보다 커야 합니다."),
    ORDER_DOES_NOT_EXIST("[ERROR] 주문 정보가 존재하지 않습니다."),
    INVALID_ORDER_FORMAT("[ERROR] 잘못된 입력입니다. 다시 입력해 주세요."),
    INVENTORY_QUANTITY_EXCEEDED("[ERROR] 재고 수량을 초과하여 구매할 수 없습니다. 다시 입력해 주세요."),
    PROMOTION_DATE_INVALID("[ERROR] 프로모션 날짜가 유효하지 않습니다."),
    START_DATE_SHOULD_BE_BEFORE_END_DATE("[ERROR] 시작 날짜는 종료 날짜보다 이전이어야 합니다."),
    INVALID_PROMOTION_NAME("[ERROR] 유효하지 않은 프로모션 이름입니다."),
    MEMBERSHIP_YES_OR_NO_CHECK("[ERROR] Y 또는 N으로 입력해주세요."),
    FAILED_TO_READ_PRODUCT("[ERROR] 상품 정보를 읽어올 수 없습니다."),
    ORDER_NOT_FOUND("[ERROR] 주문 정보를 찾을 수 없습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}