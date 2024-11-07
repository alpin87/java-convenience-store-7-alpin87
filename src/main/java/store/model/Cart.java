package store.model;

import java.util.ArrayList;
import java.util.List;
import store.exception.ErrorCode;

public class Cart {
    private final List<Order> orders;

    public Cart() {
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
        validateOrder(order);
        orders.add(order);
    }

    public List<Order> getOrders() {
        return new ArrayList<>(orders);
    }

    public int calculateTotalPrice() {
        return orders.stream()
                .mapToInt(Order::calculateTotalPrice)
                .sum();
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(ErrorCode.ORDER_DOES_NOT_EXIST.getMessage());
        }
    }
}
