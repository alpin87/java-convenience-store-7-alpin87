package store.util;

import store.model.OrderRequest;
import store.validator.Validator;
import store.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrderParser {
    private static final Pattern ORDER_PATTERN = Pattern.compile("\\[(.*?)-(\\d+)\\]");

    public static List<OrderRequest> parseOrders(String input) {
        validateInput(input);
        List<OrderRequest> requests = parseOrderRequests(input);
        validateRequests(requests);
        return requests;
    }

    private static void validateInput(String input) {
        Validator.validateNotBlank(input, ErrorCode.ORDER_DOES_NOT_EXIST);
    }

    private static List<OrderRequest> parseOrderRequests(String input) {
        Matcher matcher = ORDER_PATTERN.matcher(input);
        List<OrderRequest> requests = new ArrayList<>();

        while (matcher.find()) {
            requests.add(createOrderRequest(matcher));
        }
        return requests;
    }

    private static OrderRequest createOrderRequest(Matcher matcher) {
        String productName = matcher.group(1);
        int quantity = Integer.parseInt(matcher.group(2));
        return new OrderRequest(productName, quantity);
    }

    private static void validateRequests(List<OrderRequest> requests) {
        if (requests.isEmpty()) {
            throw new IllegalArgumentException(ErrorCode.INVALID_ORDER_FORMAT.getMessage());
        }
    }
}