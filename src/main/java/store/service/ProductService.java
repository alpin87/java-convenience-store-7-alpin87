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
        List<Product> availableProducts = products.stream()
                .filter(product -> product.getName().equals(name))
                .filter(product -> product.getTotalStock() > 0)
                .toList();

        return availableProducts.stream()
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .findFirst()
                .orElseGet(() -> availableProducts.stream()
                        .filter(p -> !p.hasPromotion())
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NON_EXISTENT_PRODUCT.getMessage())));
    }

    public Product findOriginalProduct(String name) {
        return products.stream()
                .filter(product -> product.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.NON_EXISTENT_PRODUCT.getMessage()));
    }

    public boolean checkStock(String productName, int quantity) {
        Product product = findProduct(productName);
        return product.getTotalStock() >= quantity;
    }

    public boolean isEligibleForPromotion(String productName, int quantity) {
        Product product = findProduct(productName);
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .map(p -> isQuantityEligibleForPromotion(p, quantity))
                .orElse(false);
    }

    private boolean isQuantityEligibleForPromotion(Product product, int quantity) {
        Promotion promotion = promotions.get(product.getPromotion());
        return quantity % promotion.getBuyQuantity() == 0 &&
                product.getPromotionalStock() >= quantity;
    }

    public int getPromotionalFreeQuantity(String productName) {
        Product product = findProduct(productName);
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .map(p -> promotions.get(p.getPromotion()))
                .filter(Promotion::isValid)
                .map(Promotion::getFreeQuantity)
                .orElse(0);
    }

    public int getNonPromotionalQuantity(String productName, int quantity) {
        Product product = findProduct(productName);
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .map(p -> calculateNonPromotionalQuantity(p, quantity))
                .orElse(0);
    }

    private int calculateNonPromotionalQuantity(Product product, int quantity) {
        Promotion promotion = promotions.get(product.getPromotion());
        int availablePromotionSets = product.getPromotionalStock() / promotion.getBuyQuantity();
        int maxPromotionQuantity = availablePromotionSets * promotion.getBuyQuantity();
        return quantity - maxPromotionQuantity;
    }

    public boolean needsNormalStockConfirmation(String productName, int quantity) {
        Product product = findProduct(productName);
        int nonPromotionalQuantity = getNonPromotionalQuantity(productName, quantity);
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .filter(p -> nonPromotionalQuantity > 0)
                .filter(p -> p.getNormalStock() >= nonPromotionalQuantity)
                .isPresent();
    }

    public void decreaseStock(String productName, int quantity) {
        Product product = findProduct(productName);
        OrderProcessingResult result = processOrder(productName, quantity);
        applyOrder(productName, result);
    }

    public int calculatePromotionDiscount(String productName, int quantity) {
        Product product = findProduct(productName);
        return Optional.of(product)
                .filter(Product::hasPromotion)
                .filter(p -> promotions.get(p.getPromotion()).isValid())
                .map(p -> calculateDiscountAmount(p, quantity))
                .orElse(0);
    }

    private int calculateDiscountAmount(Product product, int quantity) {
        Promotion promotion = promotions.get(product.getPromotion());
        int availablePromotionSets = Math.min(
                product.getPromotionalStock() / promotion.getBuyQuantity(),
                quantity / promotion.getBuyQuantity()
        );
        return availablePromotionSets * promotion.getFreeQuantity() * product.getPrice();
    }

    public int calculateMembershipDiscount(int totalPrice, int promotionDiscount) {
        int discountableAmount = calculateDiscountableAmount(totalPrice, promotionDiscount);
        return calculateMembershipDiscountByAmount(discountableAmount);
    }

    private int calculateDiscountableAmount(int totalPrice, int promotionDiscount) {
        int remainingPrice = totalPrice - promotionDiscount;
        return validateDiscountableAmount(remainingPrice);
    }

    private int validateDiscountableAmount(int amount) {
        return Math.max(amount, 0);
    }

    private int calculateMembershipDiscountByAmount(int amount) {
        int rawDiscount = calculateRawDiscount(amount);
        return applyDiscountLimit(rawDiscount);
    }

    private int calculateRawDiscount(int amount) {
        return (amount * MEMBERSHIP_DISCOUNT_RATE) / 100;
    }

    private int applyDiscountLimit(int discount) {
        return Math.min(discount, MAX_MEMBERSHIP_DISCOUNT);
    }

    public OrderProcessingResult processOrder(String productName, int requestedQuantity) {
        Product product = findProduct(productName);

        OrderProcessingResult result = Optional.of(product)
                .filter(p -> p.canFulfillOrder(requestedQuantity))
                .map(p -> calculateOrderQuantities(p, requestedQuantity))
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));

        transferPromotionStockToNormal(product, result);

        return result;
    }

    private void transferPromotionStockToNormal(Product product, OrderProcessingResult result) {
        int remainingQuantity = result.normalQuantity();

        if (remainingQuantity > 0) {
            int transferQuantity = Math.min(product.getPromotionalStock(), remainingQuantity);
            product.transferPromotionStockToNormal(transferQuantity);
        }
    }

    private OrderProcessingResult calculateOrderQuantities(Product product, int requestedQuantity) {
        OrderProcessingResult promotionResult = Optional.of(product)
                .filter(Product::hasPromotion)
                .map(Product::getPromotion)
                .map(promotions::get)
                .filter(Promotion::isValid)
                .map(promotion -> calculatePromotionOrder(product, requestedQuantity, promotion))
                .orElse(new OrderProcessingResult(0, requestedQuantity, 0));

        int remainingQuantity = requestedQuantity - promotionResult.getTotalQuantity();

        if (remainingQuantity > 0) {
            int normalQuantity = Math.min(remainingQuantity, product.getNormalStock() + product.getPromotionalStock());
            return new OrderProcessingResult(promotionResult.promotionQuantity(), normalQuantity, promotionResult.freeItems());
        }

        return promotionResult;
    }

    private OrderProcessingResult calculatePromotionOrder(Product product, int requestedQuantity, Promotion promotion) {
        int promotionSetSize = promotion.getBuyQuantity() + promotion.getFreeQuantity();
        int availablePromotionSets = product.getPromotionalStock() / promotionSetSize;
        int requestedSets = requestedQuantity / promotionSetSize;
        int usedPromotionSets = Math.min(availablePromotionSets, requestedSets);

        int promotionPurchaseQuantity = usedPromotionSets * promotion.getBuyQuantity();
        int promotionFreeQuantity = usedPromotionSets * promotion.getFreeQuantity();

        int remainingQuantity = requestedQuantity - (usedPromotionSets * promotionSetSize);

        return handleRemainingQuantity(product, remainingQuantity, promotionPurchaseQuantity, promotionFreeQuantity);
    }

    private OrderProcessingResult handleRemainingQuantity(Product product, int remainingQuantity,
                                                          int promotionQuantity, int freeItems) {
        return Optional.of(remainingQuantity)
                .filter(remaining -> remaining > 0)
                .map(remaining -> {
                    Optional<Product> normalProduct = products.stream()
                            .filter(p -> p.getName().equals(product.getName()))
                            .filter(p -> !p.hasPromotion())
                            .filter(p -> p.getNormalStock() >= remaining)
                            .findFirst();

                    return normalProduct
                            .map(p -> new OrderProcessingResult(promotionQuantity, remaining, freeItems))
                            .orElseGet(() -> new OrderProcessingResult(promotionQuantity, 0, freeItems));
                })
                .orElseGet(() -> new OrderProcessingResult(promotionQuantity, 0, freeItems));
    }


    private PromotionSets calculatePromotionSets(Product product, int requestedQuantity, Promotion promotion) {
        int possibleSets = requestedQuantity / promotion.getBuyQuantity();
        int maxSets = product.getPromotionalStock() / promotion.getBuyQuantity();
        int actualSets = Math.min(possibleSets, maxSets);
        return new PromotionSets(possibleSets, maxSets, actualSets);
    }

    private record PromotionSets(int possible, int max, int actual) {}

    private OrderProcessingResult processPromotionalOrder(Product product, int requestedQuantity, Promotion promotion) {
        int promotionSets = product.getPromotionalStock() / promotion.getBuyQuantity();
        int maxPromotionQuantity = promotionSets * promotion.getBuyQuantity();
        int promotionQuantity = Math.min(maxPromotionQuantity,
                (requestedQuantity / promotion.getBuyQuantity()) * promotion.getBuyQuantity());
        int remainingQuantity = requestedQuantity - promotionQuantity;
        int freeItems = (promotionQuantity / promotion.getBuyQuantity()) * promotion.getFreeQuantity();

        return new OrderProcessingResult(promotionQuantity, remainingQuantity, freeItems);
    }

    public void applyOrder(String productName, OrderProcessingResult result) {
        Product promotionProduct = findPromotionProduct(productName);
        Product normalProduct = findNormalProduct(productName);

        if (promotionProduct != null) {
            promotionProduct.decreasePromotionStock(result.promotionQuantity() + result.freeItems());
        }

        if (normalProduct != null) {
            normalProduct.decreaseNormalStock(result.normalQuantity());
        }
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

    private record StockDecreaseResult(int promotionDecrease, int normalDecrease) {}

    private StockDecreaseResult calculateStockDecrease(Product product, int totalQuantity) {
        int promotionDecrease = Math.min(product.getPromotionalStock(), totalQuantity);
        int normalDecrease = totalQuantity - promotionDecrease;
        return new StockDecreaseResult(promotionDecrease, normalDecrease);
    }

    public record OrderProcessingResult(int promotionQuantity, int normalQuantity, int freeItems) {
        public int getTotalQuantity() {
            return promotionQuantity + normalQuantity + freeItems;
        }
    }

    public boolean isMDRecommendationPromotion(String productName) {
        return Optional.ofNullable(findProduct(productName))
                .filter(Product::hasPromotion)
                .map(product -> promotions.get(product.getPromotion()))
                .map(Promotion::getType)
                .filter(type -> type == PromotionType.MD_RECOMMENDATION)
                .isPresent();
    }
}