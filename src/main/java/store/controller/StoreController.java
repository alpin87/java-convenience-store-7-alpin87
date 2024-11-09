package store.controller;

import java.util.ArrayList;
import store.model.OrderRequest;
import store.model.Product;
import store.model.YesNo;
import store.service.OrderService;
import store.service.ProductService;
import store.util.OrderParser;
import store.view.InputView;
import store.view.OutputView;
import java.util.List;
import java.util.Optional;

public class StoreController {
    private final ProductService productService;
    private final OrderService orderService;
    private final InputView inputView;
    private final OutputView outputView;

    public StoreController(
            ProductService productService,
            OrderService orderService,
            InputView inputView,
            OutputView outputView
    ) {
        this.productService = productService;
        this.orderService = orderService;
        this.inputView = inputView;
        this.outputView = outputView;
    }

    public void run() {
        displayInitialScreen();
        processOrderCycle();
    }

    private void displayInitialScreen() {
        outputView.printFirstMessage();
        displayProducts();
    }

    private void displayProducts() {
        productService.getProducts()
                .forEach(outputView::printProductList);
    }

    private void processOrderCycle() {
        do {
            processSingleOrder();
        } while (shouldContinueOrder());
    }

    private void processSingleOrder() {
        try {
            List<OrderRequest> requests = OrderParser.parseOrders(inputView.readFirstOrder());
            processOrders(requests);
            completeOrder();
        } catch (IllegalArgumentException e) {
            handleOrderError(e);
        }
    }

    private void processOrders(List<OrderRequest> requests) {
        List<ProcessedOrder> processedOrders = validateAndProcessOrders(requests);
        applyProcessedOrders(processedOrders);
    }

    private List<ProcessedOrder> validateAndProcessOrders(List<OrderRequest> requests) {
        List<ProcessedOrder> processedOrders = new ArrayList<>();

        for (OrderRequest request : requests) {
            validateOrder(request);
            ProductService.OrderProcessingResult result = productService.processOrder(request.productName(), request.quantity());

            if (productService.isMDRecommendationPromotion(request.productName())) {
                String response = inputView.readAdditionalOption(request.productName());
                if (YesNo.from(response) == YesNo.YES) {
                    int additionalQuantity = productService.getPromotionalFreeQuantity(request.productName());
                    result = productService.processOrder(request.productName(), request.quantity() + additionalQuantity);
                }
            }

            if (needsConfirmation(request, result)) {
                confirmAndAddOrder(request, result, processedOrders);
                continue;
            }

            processedOrders.add(new ProcessedOrder(request, result));
        }

        return processedOrders;
    }

    private void applyProcessedOrders(List<ProcessedOrder> processedOrders) {
        processedOrders.forEach(processed ->
                orderService.processOrder(processed.request().productName(), processed.result().getTotalQuantity())
        );
    }

    private boolean needsConfirmation(OrderRequest request, ProductService.OrderProcessingResult result) {
        return Optional.of(request)
                .map(req -> productService.findProduct(req.productName()))
                .filter(Product::hasPromotion)
                .filter(p -> result.normalQuantity() > 0)
                .isPresent();
    }

    private void confirmAndAddOrder(OrderRequest request, ProductService.OrderProcessingResult result, List<ProcessedOrder> orders) {
        String response = inputView.readPromotionOption(request.productName(), result.normalQuantity());

        if (YesNo.from(response) == YesNo.YES) {
            orders.add(new ProcessedOrder(request, result));
        }
    }

    private record ProcessedOrder(OrderRequest request, ProductService.OrderProcessingResult result) {}

    private void processOrder(OrderRequest request) {
        validateOrder(request);
        processPromotionOrder(request);
    }

    private void validateOrder(OrderRequest request) {
        Optional.of(request)
                .map(req -> productService.checkStock(req.productName(), req.quantity()))
                .filter(valid -> valid)
                .orElseThrow(() -> new IllegalArgumentException("[ERROR] 재고 수량을 초과하여 구매할 수 없습니다."));
    }

    private void processPromotionOrder(OrderRequest request) {
        ProductService.OrderProcessingResult result = productService.processOrder(
                request.productName(),
                request.quantity()
        );

        handleOrderResult(request, result);
    }

    private void handleOrderResult(OrderRequest request, ProductService.OrderProcessingResult result) {
        Optional.of(result)
                .filter(r -> needsConfirmation(request, r))
                .map(r -> confirmNormalStockUsage(request, r))
                .orElseGet(() -> addToCartWithPromotion(request, result));
    }

    private boolean confirmNormalStockUsage(OrderRequest request, ProductService.OrderProcessingResult result) {
        String response = inputView.readPromotionOption(request.productName(), result.normalQuantity());

        return Optional.of(response)
                .map(YesNo::from)
                .filter(answer -> answer == YesNo.YES)
                .map(answer -> addToCartWithPromotion(request, result))
                .orElse(false);
    }

    private boolean addToCartWithPromotion(OrderRequest request, ProductService.OrderProcessingResult result) {
        productService.applyOrder(request.productName(), result);
        orderService.addToCart(request.productName(), result.getTotalQuantity(), true);  // 프로모션 적용 상품으로 추가
        return true;
    }

    private boolean handlePromotionOffer(OrderRequest request) {
        String response = inputView.readAdditionalOption(request.productName());
        return Optional.of(response)
                .map(YesNo::from)
                .filter(answer -> answer == YesNo.YES)
                .map(answer -> addToCartWithPromotion(request))
                .orElseGet(() -> addToCart(request));
    }

    private boolean handleNormalStockConfirmation(OrderRequest request) {
        return Optional.of(request)
                .filter(req -> productService.needsNormalStockConfirmation(req.productName(), req.quantity()))
                .map(this::confirmNormalStockUsage)
                .orElseGet(() -> addToCart(request));
    }

    private boolean confirmNormalStockUsage(OrderRequest request) {
        int nonPromotionalQuantity = productService.getNonPromotionalQuantity(request.productName(), request.quantity());
        String response = inputView.readPromotionOption(request.productName(), nonPromotionalQuantity);

        return Optional.of(response)
                .map(YesNo::from)
                .filter(answer -> answer == YesNo.YES)
                .map(answer -> addToCart(request))
                .orElse(false);
    }

    private boolean addToCartWithPromotion(OrderRequest request) {
        int additionalQuantity = productService.getPromotionalFreeQuantity(request.productName());
        OrderRequest updatedRequest = new OrderRequest(
                request.productName(),
                request.quantity() + additionalQuantity
        );
        return addToCart(updatedRequest);
    }

    private boolean addToCart(OrderRequest request) {
        orderService.addToCart(request.productName(), request.quantity(), false);  // 일반 상품으로 추가
        return true;
    }

    private void completeOrder() {
        boolean useMembership = readMembershipOption();
        printOrderReceipt(useMembership);
        orderService.applyPendingOrders();
    }

    private boolean readMembershipOption() {
        String response = inputView.readMembershipOption();
        return YesNo.from(response) == YesNo.YES;
    }

    private void printOrderReceipt(boolean useMembership) {
        OrderService.TotalPriceResult totalPriceResult = orderService.calculateTotalPrice();
        int totalPrice = totalPriceResult.getTotalPrice();
        int promotionDiscount = orderService.calculatePromotionDiscount();
        int membershipDiscount = calculateFinalMembershipDiscount(useMembership, totalPriceResult.normalPrice());

        outputView.printReceipt(
                orderService.getCart(),
                totalPrice,
                promotionDiscount,
                membershipDiscount
        );
    }

    private int calculateFinalMembershipDiscount(boolean useMembership, int normalPrice) {
        return getMembershipDiscount(useMembership, calculateMembershipDiscountAmount(normalPrice));
    }

    private int calculateMembershipDiscountAmount(int normalPrice) {
        return productService.calculateMembershipDiscount(normalPrice);
    }

    private int getMembershipDiscount(boolean useMembership, int discountAmount) {
        return Optional.of(useMembership)
                .filter(use -> use)
                .map(use -> discountAmount)
                .orElse(0);
    }

    private boolean shouldContinueOrder() {
        String response = inputView.readContinueOrder();
        boolean shouldContinue = YesNo.from(response) == YesNo.YES;

        if (shouldContinue) {
            prepareNextOrder();
        }

        return shouldContinue;
    }

    private void prepareNextOrder() {
        orderService.clearCart();
        resetDisplay();
    }

    private void resetDisplay() {
        outputView.printFirstMessage();
        displayProducts();
    }

    private void handleOrderError(IllegalArgumentException e) {
        outputView.printError(e.getMessage());
        processSingleOrder();
    }
}