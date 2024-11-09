package store.view;

import camp.nextstep.edu.missionutils.Console;
import java.util.Optional;

public class InputView {
    private static final String ORDER_PROMPT = "구매하실 상품명과 수량을 입력해 주세요. (예: [사이다-2],[감자칩-1])";
    private static final String MEMBERSHIP_PROMPT = "멤버십 할인을 받으시겠습니까? (Y/N)";
    private static final String CONTINUE_PROMPT = "감사합니다. 구매하고 싶은 다른 상품이 있나요? (Y/N)";
    private static final String PROMOTION_NOTICE = "현재 %s %d개는 프로모션 할인이 적용되지 않습니다. 그래도 구매하시겠습니까? (Y/N)";
    private static final String FREE_ITEM_NOTICE = "현재 %s은(는) 1개를 무료로 더 받을 수 있습니다. 추가하시겠습니까? (Y/N)";

    public String readFirstOrder() {
        return readInput(ORDER_PROMPT);
    }

    public String readMembershipOption() {
        return readInput(MEMBERSHIP_PROMPT);
    }

    public String readContinueOrder() {
        return readInput(CONTINUE_PROMPT);
    }

    public String readPromotionOption(String productName, int quantity) {
        return readFormattedInput(PROMOTION_NOTICE, productName, quantity);
    }

    public String readAdditionalOption(String productName) {
        return readFormattedInput(FREE_ITEM_NOTICE, productName);
    }

    private String readInput(String prompt) {
        System.out.println("\n" + prompt);
        return Console.readLine();
    }

    private String readFormattedInput(String format, Object... args) {
        return Optional.of(format)
                .map(f -> String.format("\n" + f, args))
                .map(this::readInput)
                .orElseThrow();
    }
}