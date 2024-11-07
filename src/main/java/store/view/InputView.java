package store.view;

import camp.nextstep.edu.missionutils.Console;
import store.exception.ErrorCode;

public class InputView {

    private static final String INPUT_PRODUCT_MESSAGE = "구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])";
    private static final String INPUT_MEMBERSHIP_MESSAGE = "멤버십 할인을 받으시겠습니까? (Y/N)";

    public String FirstOrder() {
        System.out.println(INPUT_PRODUCT_MESSAGE);
        return Console.readLine();
    }

    public boolean MembershipOption() {
        System.out.println(INPUT_MEMBERSHIP_MESSAGE);
        String input = Console.readLine();
        validateYesOrNo(input);
        return input.equalsIgnoreCase("Y");
    }

    private void validateYesOrNo(String input) {
        if (!input.equalsIgnoreCase("Y") && !input.equalsIgnoreCase("N")) {
            throw new IllegalArgumentException(ErrorCode.MEMBERSHIP_YES_OR_NO_CHECK.getMessage());
        }
    }
}
