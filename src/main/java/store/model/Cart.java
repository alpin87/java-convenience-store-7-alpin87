package store.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import store.exception.ErrorCode;
import store.validator.Validator;
import java.util.stream.Collectors;

public class Cart {
    private final List<Order> orders;

    public Cart() {
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        validateOrder(order);
        Optional.ofNullable(order)
                .ifPresent(orders::add);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public List<Order> getPromotionalOrders() {
        return Optional.of(orders)
                .map(List::stream)
                .map(stream -> stream
                        .filter(Order::isPromotional)
                        .filter(order -> order.getProduct().hasPromotion())
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    public List<Order> getNormalOrders() {
        return Optional.of(orders)
                .map(List::stream)
                .map(stream -> stream
                        .filter(order -> !order.getProduct().hasPromotion())
                        .collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    public int calculateTotalPrice() {
        return Optional.of(orders)
                .map(List::stream)
                .map(stream -> stream
                        .mapToInt(Order::calculateTotalPrice)
                        .sum())
                .orElse(0);
    }

    public int getTotalQuantity() {
        return Optional.of(orders)
                .map(List::stream)
                .map(stream -> stream
                        .mapToInt(Order::getQuantity)
                        .sum())
                .orElse(0);
    }

    private void validateOrder(Order order) {
        Optional.ofNullable(order)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.ORDER_DOES_NOT_EXIST.getMessage()));
    }
}