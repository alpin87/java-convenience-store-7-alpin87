package store.model;

import camp.nextstep.edu.missionutils.DateTimes;
import java.util.Optional;
import store.exception.ErrorCode;
import store.validator.Validator;
import java.time.LocalDateTime;

public class Promotion {
    private final PromotionType type;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;

    public Promotion(String name, int buyQuantity, int freeQuantity,
                     LocalDateTime startDate, LocalDateTime endDate) {
        validatePromotionDates(startDate, endDate);
        this.type = PromotionType.fromName(name);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public PromotionType getType() {
        return type;
    }

    public String getName() {
        return type.getName();
    }

    public int getBuyQuantity() {
        return type.getBuyQuantity();
    }

    public int getFreeQuantity() {
        return type.getFreeQuantity();
    }

    public boolean isValid() {
        return Optional.of(DateTimes.now())
                .map(this::isDateInPromotionPeriod)
                .orElse(false);
    }

    private boolean isDateInPromotionPeriod(LocalDateTime currentDate) {
        return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
    }

    private void validatePromotionDates(LocalDateTime startDate, LocalDateTime endDate) {
        validateDatesNotNull(startDate, endDate);
        validateDateRange(startDate, endDate);
    }

    private void validateDatesNotNull(LocalDateTime startDate, LocalDateTime endDate) {
        Validator.validateNotNull(startDate, ErrorCode.PROMOTION_DATE_INVALID);
        Validator.validateNotNull(endDate, ErrorCode.PROMOTION_DATE_INVALID);
    }

    private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(ErrorCode.START_DATE_SHOULD_BE_BEFORE_END_DATE.getMessage());
        }
    }
}