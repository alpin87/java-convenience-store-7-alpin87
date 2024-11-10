package store.controller;

import java.util.List;
import java.util.Optional;
import store.exception.ErrorCode;
import store.model.OrderRequest;
import store.model.YesNo;
import store.service.OrderService;
import store.service.ProductService;
import store.service.ProductService.OrderProcessingResult;
import store.util.OrderParser;
import store.view.InputView;
import store.view.OutputView;

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
        Optional.of(productService)
                .map(ProductService::getProducts)
                .ifPresent(products -> products.forEach(outputView::printProductList));
    }

    private void processOrderCycle() {
        do {
            processOneOrder();
        } while (checkContinueOrder());
    }

    private void processOneOrder() {
        try {
            processOrderAndComplete();
        } catch (IllegalArgumentException e) {
            handleOrderError(e);
        }
    }

    private void processOrderAndComplete() {
        Optional.of(readAndParseOrder())
                .map(this::validateAndProcessOrders)
                .ifPresent(this::applyProcessedOrders);
        completeOrder();
    }

    private List<OrderRequest> readAndParseOrder() {
        return Optional.of(inputView.readFirstOrder())
                .map(OrderParser::parseOrders)
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVALID_ORDER_FORMAT.getMessage()));
    }

    private List<ProcessedOrder> validateAndProcessOrders(List<OrderRequest> requests) {
        return requests.stream()
                .map(this::processRequest)
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ProcessedOrder> processRequest(OrderRequest request) {
        return Optional.of(request)
                .map(this::validateRequest)
                .map(this::createProcessedOrder);
    }

    private OrderRequest validateRequest(OrderRequest request) {
        return Optional.of(request)
                .filter(req -> isStockAvailable(req))
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));
    }

    private boolean isStockAvailable(OrderRequest request) {
        return productService.checkStock(request.productName(), request.quantity());
    }

    private ProcessedOrder createProcessedOrder(OrderRequest request) {
        OrderProcessingResult result = processOrderWithPromotion(request);
        return new ProcessedOrder(request, result);
    }

    private OrderProcessingResult processOrderWithPromotion(OrderRequest request) {
        OrderProcessingResult initialResult = productService.processOrder(request.productName(), request.quantity());

        return Optional.of(request)
                .filter(this::isMDPromotionProduct)
                .filter(this::confirmAdditionalItem)
                .map(this::processWithAdditionalItem)
                .orElse(initialResult);
    }

    private boolean isMDPromotionProduct(OrderRequest request) {
        return productService.isMDRecommendationPromotion(request.productName());
    }

    private boolean confirmAdditionalItem(OrderRequest request) {
        return Optional.of(inputView.readAdditionalOption(request.productName()))
                .map(YesNo::from)
                .map(YesNo::isYes)
                .orElse(false);
    }

    private OrderProcessingResult processWithAdditionalItem(OrderRequest request) {
        int totalQuantity = calculateTotalQuantity(request);
        return productService.processOrder(request.productName(), totalQuantity);
    }

    private int calculateTotalQuantity(OrderRequest request) {
        int additionalQuantity = productService.getPromotionalFreeQuantity(request.productName());
        return request.quantity() + additionalQuantity;
    }

    private void applyProcessedOrders(List<ProcessedOrder> processedOrders) {
        processedOrders.forEach(this::applyOrder);
    }

    private void applyOrder(ProcessedOrder processed) {
        orderService.processOrder(
                processed.request().productName(),
                processed.result().getTotalQuantity()
        );
    }

    private void completeOrder() {
        boolean useMembership = confirmMembership();
        printOrderResult(useMembership);
        finalizePendingOrders();
    }

    private boolean confirmMembership() {
        return Optional.of(inputView.readMembershipOption())
                .map(YesNo::from)
                .map(YesNo::isYes)
                .orElse(false);
    }

    private void printOrderResult(boolean useMembership) {
        OrderSummary summary = calculateOrderSummary(useMembership);
        outputView.printReceipt(
                orderService.getCart(),
                summary.totalPrice(),
                summary.promotionDiscount(),
                summary.membershipDiscount()
        );
    }

    private OrderSummary calculateOrderSummary(boolean useMembership) {
        int totalPrice = orderService.calculateTotalPrice();
        int promotionDiscount = orderService.calculatePromotionDiscount();
        int membershipDiscount = calculateMembershipDiscount(useMembership, totalPrice, promotionDiscount);

        return new OrderSummary(totalPrice, promotionDiscount, membershipDiscount);
    }

    private int calculateMembershipDiscount(boolean useMembership, int totalPrice, int promotionDiscount) {
        return Optional.of(useMembership)
                .filter(use -> use)
                .map(use -> orderService.calculateMembershipDiscount(totalPrice, promotionDiscount))
                .orElse(0);
    }

    private void finalizePendingOrders() {
        orderService.applyPendingOrders();
    }

    private boolean checkContinueOrder() {
        return Optional.of(inputView.readContinueOrder())
                .map(YesNo::from)
                .map(YesNo::isYes)
                .map(this::prepareNextOrderIfNeeded)
                .orElse(false);
    }

    private boolean prepareNextOrderIfNeeded(boolean shouldContinue) {
        Optional.of(shouldContinue)
                .filter(should -> should)
                .ifPresent(should -> prepareNextOrder());
        return shouldContinue;
    }

    private void prepareNextOrder() {
        orderService.clearCart();
        System.out.println();
        displayInitialScreen();
    }

    private void handleOrderError(IllegalArgumentException e) {
        outputView.printError(e.getMessage());
        processOneOrder();
    }

    private record ProcessedOrder(OrderRequest request, OrderProcessingResult result) {}
    private record OrderSummary(int totalPrice, int promotionDiscount, int membershipDiscount) {}
}