package store.model;

import camp.nextstep.edu.missionutils.DateTimes;
import store.exception.ErrorCode;
import java.time.LocalDateTime;

public class Promotion {
    private final String name;
    private final int buyQuantity;
    private final int getFreeQuantity;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public Promotion(String name, int buyQuantity, int getFreeQuantity,
                     LocalDateTime startDate, LocalDateTime endDate) {
        validateName(name);
        validateQuantities(buyQuantity, getFreeQuantity);
        validateDates(startDate, endDate);

        this.name = name;
        this.buyQuantity = buyQuantity;
        this.getFreeQuantity = getFreeQuantity;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public int getBuyQuantity() {
        return buyQuantity;
    }

    public int getFreeQuantity() {
        return getFreeQuantity;
    }

    public boolean isValid() {
        LocalDateTime now = DateTimes.now();
        return validateCurrentDate(now);
    }

    private boolean validateCurrentDate(LocalDateTime currentDate) {
        return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_PROMOTION_NAME.getMessage());
        }
    }

    private void validateQuantities(int buyQuantity, int getFreeQuantity) {
        if (buyQuantity <= 0 || getFreeQuantity <= 0) {
            throw new IllegalArgumentException(ErrorCode.INVALID_PROMOTION_QUANTITY.getMessage());
        }
    }

    private void validateDates(LocalDateTime startDate, LocalDateTime endDate) {
        validateDateNotNull(startDate, endDate);
        validateDateRange(startDate, endDate);
    }

    private void validateDateNotNull(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(ErrorCode.PROMOTION_DATE_INVALID.getMessage());
        }
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(ErrorCode.START_DATE_SHOULD_BE_BEFORE_END_DATE.getMessage());
        }
    }
}