package store.service;

import store.exception.ErrorCode;
import store.model.Product;
import store.model.Promotion;
import store.model.PromotionType;
import store.util.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProductService {
    private static final int MEMBERSHIP_DISCOUNT_RATE = 30;
    private static final int MAX_MEMBERSHIP_DISCOUNT = 8000;

    private final List<Product> products;
    private final Map<String, Promotion> promotions;

    public ProductService(FileReader fileReader) {
        this.products = fileReader.getProducts();
        this.promotions = fileReader.getPromotions().stream()
                .collect(Collectors.toMap(Promotion::getName, promotion -> promotion));
    }

    public List<Product> getProducts() {
        return products;
    }

    public Product findProduct(String name) {
        List<Product> availableProducts = getAvailableProducts(name);
        return findPromotionalProduct(availableProducts)
                .orElseGet(() -> findNormalProductFromList(availableProducts));
    }

    private List<Product> getAvailableProducts(String name) {
        return products.stream()
                .filter(product -> product.getName().equals(name))
                .filter(product -> product.getTotalStock() > 0)
                .toList();
    }

    private Optional<Product> findPromotionalProduct(List<Product> availableProducts) {
        return availableProducts.stream()
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .findFirst();
    }

    private Product findNormalProductFromList(List<Product> availableProducts) {
        return availableProducts.stream()
                .filter(p -> !p.hasPromotion())
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NON_EXISTENT_PRODUCT.getMessage()));
    }

    public Product findOriginalProduct(String name) {
        return products.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NON_EXISTENT_PRODUCT.getMessage()));
    }

    public boolean checkStock(String productName, int quantity) {
        return Optional.of(findProduct(productName))
                .map(product -> product.getTotalStock() >= quantity)
                .orElse(false);
    }

    public OrderProcessingResult processOrder(String productName, int requestedQuantity) {
        Product product = findProduct(productName);
        validateStock(product, requestedQuantity);
        return calculateOrderQuantities(product, requestedQuantity);
    }

    private void validateStock(Product product, int requestedQuantity) {
        Optional.of(product)
                .filter(p -> p.canFulfillOrder(requestedQuantity))
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));
    }

    private OrderProcessingResult calculateOrderQuantities(Product product, int requestedQuantity) {
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .map(p -> calculatePromotionalOrder(p, requestedQuantity))
                .orElseGet(() -> new OrderProcessingResult(0, requestedQuantity, 0));
    }

    private OrderProcessingResult calculatePromotionalOrder(Product product, int requestedQuantity) {
        return Optional.ofNullable(promotions.get(product.getPromotion()))
                .filter(Promotion::isValid)
                .map(promotion -> processValidPromotion(product, requestedQuantity, promotion))
                .orElseGet(() -> new OrderProcessingResult(0, requestedQuantity, 0));
    }

    private OrderProcessingResult processValidPromotion(Product product, int requestedQuantity, Promotion promotion) {
        PromotionSetsInfo setsInfo = calculatePromotionSets(product, requestedQuantity, promotion);
        return createPromotionResult(product, setsInfo);
    }

    private PromotionSetsInfo calculatePromotionSets(Product product, int requestedQuantity, Promotion promotion) {
        int promotionSetSize = promotion.getBuyQuantity() + promotion.getFreeQuantity();
        int availablePromotionSets = product.getPromotionalStock() / promotionSetSize;
        int requestedSets = requestedQuantity / promotionSetSize;
        int usedSets = Math.min(availablePromotionSets, requestedSets);

        return new PromotionSetsInfo(
                usedSets * promotion.getBuyQuantity(),
                usedSets * promotion.getFreeQuantity(),
                requestedQuantity - (usedSets * promotionSetSize)
        );
    }

    private OrderProcessingResult createPromotionResult(Product product, PromotionSetsInfo setsInfo) {
        return Optional.of(setsInfo)
                .filter(info -> info.remainingQuantity() > 0)
                .map(info -> handleRemainingQuantity(product, info))
                .orElseGet(() -> new OrderProcessingResult(
                        setsInfo.promotionQuantity(),
                        0,
                        setsInfo.freeQuantity()
                ));
    }

    private OrderProcessingResult handleRemainingQuantity(Product product, PromotionSetsInfo setsInfo) {
        return Optional.ofNullable(findNormalProduct(product.getName()))
                .filter(normalProduct -> normalProduct.getNormalStock() >= setsInfo.remainingQuantity())
                .map(normalProduct -> new OrderProcessingResult(
                        setsInfo.promotionQuantity(),
                        setsInfo.remainingQuantity(),
                        setsInfo.freeQuantity()
                ))
                .orElseGet(() -> new OrderProcessingResult(
                        setsInfo.promotionQuantity(),
                        0,
                        setsInfo.freeQuantity()
                ));
    }

    public void applyOrder(String productName, OrderProcessingResult result) {
        applyPromotionOrder(productName, result);
        applyNormalOrder(productName, result);
    }

    private void applyPromotionOrder(String productName, OrderProcessingResult result) {
        Optional.ofNullable(findPromotionProduct(productName))
                .ifPresent(product -> product.decreasePromotionStock(result.promotionQuantity() + result.freeItems()));
    }

    private void applyNormalOrder(String productName, OrderProcessingResult result) {
        Optional.ofNullable(findNormalProduct(productName))
                .ifPresent(product -> product.decreaseNormalStock(result.normalQuantity()));
    }

    public int calculatePromotionDiscount(String productName, int quantity) {
        return Optional.of(findProduct(productName))
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .map(p -> calculateProductDiscount(p, quantity))
                .orElse(0);
    }

    private int calculateProductDiscount(Product product, int quantity) {
        Promotion promotion = promotions.get(product.getPromotion());
        int availablePromotionSets = Math.min(
                product.getPromotionalStock() / promotion.getBuyQuantity(),
                quantity / promotion.getBuyQuantity()
        );
        return availablePromotionSets * promotion.getFreeQuantity() * product.getPrice();
    }

    public int calculateMembershipDiscount(int totalPrice, int promotionDiscount) {
        return Optional.of(totalPrice - promotionDiscount)
                .map(this::calculateRawDiscount)
                .map(this::applyDiscountLimit)
                .orElse(0);
    }

    private int calculateRawDiscount(int amount) {
        return (amount * MEMBERSHIP_DISCOUNT_RATE) / 100;
    }

    private int applyDiscountLimit(int discount) {
        return Math.min(discount, MAX_MEMBERSHIP_DISCOUNT);
    }

    private Product findPromotionProduct(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .filter(Product::hasPromotion)
                .findFirst()
                .orElse(null);
    }

    private Product findNormalProduct(String name) {
        return products.stream()
                .filter(p -> p.getName().equals(name))
                .filter(p -> !p.hasPromotion())
                .findFirst()
                .orElse(null);
    }

    public boolean isMDRecommendationPromotion(String productName) {
        return Optional.ofNullable(findProduct(productName))
                .filter(Product::hasPromotion)
                .map(product -> promotions.get(product.getPromotion()))
                .map(Promotion::getType)
                .filter(type -> type == PromotionType.MD_RECOMMENDATION)
                .isPresent();
    }

    public record OrderProcessingResult(int promotionQuantity, int normalQuantity, int freeItems) {
        public int getTotalQuantity() {
            return promotionQuantity + normalQuantity + freeItems;
        }
    }

    private record PromotionSetsInfo(int promotionQuantity, int freeQuantity, int remainingQuantity) {}

    public int getPromotionalFreeQuantity(String productName) {
        return Optional.of(findProduct(productName))
                .filter(Product::hasPromotion)
                .map(p -> promotions.get(p.getPromotion()))
                .filter(Promotion::isValid)
                .map(Promotion::getFreeQuantity)
                .orElse(0);
    }
}