package store.model;

import store.exception.ErrorCode;
import java.util.Arrays;
import java.util.Optional;

public enum YesNo {
    YES("Y"),
    NO("N");

    private final String value;

    YesNo(String value) {
        this.value = value;
    }

    public static YesNo from(String input) {
        validateInput(input);
        return findAnswer(input.toUpperCase());
    }

    private static void validateInput(String input) {
        Optional.ofNullable(input)
                .filter(str -> !str.trim().isEmpty())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.MEMBERSHIP_YES_OR_NO_CHECK.getMessage()));
    }

    private static YesNo findAnswer(String input) {
        return Arrays.stream(values())
                .filter(answer -> answer.value.equals(input))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.MEMBERSHIP_YES_OR_NO_CHECK.getMessage()));
    }

    public boolean isYes() {
        return this == YES;
    }
}