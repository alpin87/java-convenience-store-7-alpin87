package store.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import store.exception.ErrorCode;
import store.model.Cart;
import store.model.Order;
import store.model.Product;
import store.model.PromotionType;
import store.service.ProductService.OrderProcessingResult;
import java.util.function.Function;
import java.util.stream.Stream;

public class OrderService {
    private static final int MEMBERSHIP_DISCOUNT_RATE = 30;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;

    private final ProductService productService;
    private Cart cart;
    private List<OrderProcessingResult> pendingOrders;

    public OrderService(ProductService productService) {
        this.productService = productService;
        this.cart = new Cart();
        this.pendingOrders = new ArrayList<>();
    }

    public Cart getCart() {
        return cart;
    }

    public void addToCart(String productName, int quantity, boolean isPromotional) {
        Optional.ofNullable(productService.findOriginalProduct(productName))
                .map(product -> createOrder(product, quantity, isPromotional))
                .ifPresent(cart::addOrder);
    }

    private Order createOrder(Product product, int quantity, boolean isPromotional) {
        return new Order(product, quantity, isPromotional);
    }

    public void processOrder(String productName, int totalQuantity) {
        Optional.of(new OrderProcessingResult(totalQuantity, 0, 0))
                .map(result -> {
                    addToCart(productName, totalQuantity, true);
                    pendingOrders.add(result);
                    return result;
                });
    }

    public void applyPendingOrders() {
        Optional.of(pendingOrders)
                .map(List::stream)
                .ifPresent(stream ->
                        stream.forEach(result ->
                                productService.applyOrder(getProductName(result), result)));
        pendingOrders.clear();
    }

    private String getProductName(OrderProcessingResult result) {
        return Optional.of(cart)
                .map(Cart::getOrders)
                .map(List::stream)
                .flatMap(stream -> stream
                        .filter(order -> order.getQuantity() == result.getTotalQuantity())
                        .map(order -> order.getProduct().getName())
                        .findFirst())
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.ORDER_NOT_FOUND.getMessage()));
    }

    public int calculateTotalPrice() {
        return Optional.of(cart)
                .map(Cart::getOrders)
                .map(orders -> orders.stream()
                        .mapToInt(Order::calculateTotalPrice)
                        .sum())
                .orElse(0);
    }

    public int calculatePromotionDiscount() {
        return Optional.of(cart)
                .map(Cart::getOrders)
                .map(List::stream)
                .map(this::filterPromotionalOrders)
                .map(this::calculateTotalDiscount)
                .orElse(0);
    }

    private Stream<Order> filterPromotionalOrders(Stream<Order> orders) {
        return orders.filter(Order::isPromotional)
                .filter(order -> order.getProduct().hasPromotion());
    }

    private int calculateTotalDiscount(Stream<Order> orders) {
        return orders.mapToInt(this::calculateDiscountByOrder).sum();
    }

    private int calculateDiscountByOrder(Order order) {
        return getPromotionType(order.getProduct())
                .map(type -> calculateDiscountByType(order, type))
                .orElse(0);
    }

    private Optional<PromotionType> getPromotionType(Product product) {
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .map(Product::getPromotion)
                .map(PromotionType::fromName);
    }

    private int calculateDiscountByType(Order order, PromotionType type) {
        Map<PromotionType, Function<Order, Integer>> discountCalculators = Map.of(
                PromotionType.MD_RECOMMENDATION, this::calculateMDPromotionDiscount,
                PromotionType.BUY_2_GET_1, this::calculateBuy2Get1Discount,
                PromotionType.FLASH_SALE, this::calculateFlashSaleDiscount
        );

        return Optional.ofNullable(discountCalculators.get(type))
                .map(calculator -> calculator.apply(order))
                .orElse(0);
    }

    private int calculateMDPromotionDiscount(Order order) {
        return order.getProduct().getPrice();
    }

    private int calculateBuy2Get1Discount(Order order) {
        return (order.getQuantity() / 3) * order.getProduct().getPrice();
    }

    private int calculateFlashSaleDiscount(Order order) {
        return order.getProduct().getPrice();
    }

    public int calculateMembershipDiscount(int totalPrice, int promotionDiscount) {
        return Optional.of(calculateNormalItemsPrice())
                .map(this::calculateRawMembershipDiscount)
                .map(this::applyMembershipDiscountLimit)
                .orElse(0);
    }

    private int calculateNormalItemsPrice() {
        return Optional.of(cart)
                .map(Cart::getOrders)
                .map(List::stream)
                .map(stream -> stream
                        .filter(order -> !order.getProduct().hasPromotion())
                        .mapToInt(Order::calculateTotalPrice)
                        .sum())
                .orElse(0);
    }

    private int calculateRawMembershipDiscount(int price) {
        return (price * MEMBERSHIP_DISCOUNT_RATE) / 100;
    }

    private int applyMembershipDiscountLimit(int discount) {
        return Math.min(discount, MAX_MEMBERSHIP_DISCOUNT);
    }

    public void clearCart() {
        Optional.of(new Cart())
                .ifPresent(newCart -> {
                    this.cart = newCart;
                    this.pendingOrders = new ArrayList<>();
                });
    }
}