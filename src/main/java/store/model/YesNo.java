package store.model;

import store.exception.ErrorCode;

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
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.MEMBERSHIP_YES_OR_NO_CHECK.getMessage());
        }
    }

    private static YesNo findAnswer(String input) {
        for (YesNo answer : values()) {
            if (answer.value.equals(input)) {
                return answer;
            }
        }
        throw new IllegalArgumentException(ErrorCode.MEMBERSHIP_YES_OR_NO_CHECK.getMessage());
    }
}