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
            processSingleOrder();
        } while (shouldContinueOrder());
    }
    private void processSingleOrder() {
        try {
            Optional.of(inputView.readFirstOrder())
                    .map(OrderParser::parseOrders)
                    .ifPresent(this::processOrders);
            completeOrder();
        } catch (IllegalArgumentException e) {
            handleOrderError(e);
        }
    }
    private void processOrders(List<OrderRequest> requests) {
        Optional.of(requests)
                .map(this::validateAndProcessOrders)
                .ifPresent(this::applyProcessedOrders);
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
                .filter(req -> productService.checkStock(req.productName(), req.quantity()))
                .orElseThrow(() -> new IllegalArgumentException(ErrorCode.INVENTORY_QUANTITY_EXCEEDED.getMessage()));
    }
    private ProcessedOrder createProcessedOrder(OrderRequest request) {
        OrderProcessingResult result = processOrderRequest(request);
        return new ProcessedOrder(request, result);
    }
    private OrderProcessingResult processOrderRequest(OrderRequest request) {
        OrderProcessingResult result = productService.processOrder(request.productName(), request.quantity());
        return Optional.of(request)
                .filter(req -> productService.isMDRecommendationPromotion(req.productName()))
                .filter(req -> handleMDPromotion(req))
                .map(req -> processOrder(req))
                .orElse(result);
    }
    private boolean handleMDPromotion(OrderRequest request) {
        return Optional.of(inputView.readAdditionalOption(request.productName()))
                .map(YesNo::from)
                .map(answer -> answer == YesNo.YES)
                .orElse(false);
    }
    private OrderProcessingResult processOrder(OrderRequest request) {
        int additionalQuantity = productService.getPromotionalFreeQuantity(request.productName());
        return productService.processOrder(request.productName(), request.quantity() + additionalQuantity);
    }
    private record ProcessedOrder(OrderRequest request, OrderProcessingResult result) {}
    private void completeOrder() {
        boolean useMembership = readMembershipOption();
        printOrderSummary(useMembership);
        orderService.applyPendingOrders();
    }
    private boolean readMembershipOption() {
        return Optional.of(inputView.readMembershipOption())
                .map(YesNo::from)
                .map(answer -> answer == YesNo.YES)
                .orElse(false);
    }
    private void printOrderSummary(boolean useMembership) {
        int totalPrice = orderService.calculateTotalPrice();
        int promotionDiscount = orderService.calculatePromotionDiscount();
        int membershipDiscount = calculateMembershipDiscount(useMembership, totalPrice, promotionDiscount);
        outputView.printReceipt(
                orderService.getCart(),
                totalPrice,
                promotionDiscount,
                membershipDiscount
        );
    }
    private int calculateMembershipDiscount(boolean useMembership, int totalPrice, int promotionDiscount) {
        return Optional.of(useMembership)
                .filter(use -> use)
                .map(use -> orderService.calculateMembershipDiscount(totalPrice, promotionDiscount))
                .orElse(0);
    }
    private void applyProcessedOrders(List<ProcessedOrder> processedOrders) {
        Optional.of(processedOrders)
                .ifPresent(orders -> orders.forEach(this::applyOrder));
    }
    private void applyOrder(ProcessedOrder processed) {
        orderService.processOrder(
                processed.request().productName(),
                processed.result().getTotalQuantity()
        );
    }
    private boolean shouldContinueOrder() {
        return Optional.of(inputView.readContinueOrder())
                .map(YesNo::from)
                .map(answer -> answer == YesNo.YES)
                .map(this::handleContinueOrder)
                .orElse(false);
    }
    private boolean handleContinueOrder(boolean shouldContinue) {
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