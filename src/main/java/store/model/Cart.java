package store.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<Order> orders;

    public Cart() {
        this.orders = new ArrayList<>();
    }

    public void addOrder(Order order) {
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
}
