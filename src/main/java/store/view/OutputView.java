package store.view;

import store.model.Cart;
import store.model.Order;
import store.model.Product;
import java.util.Optional;
import java.util.function.Consumer;

public class OutputView {
    private static final String RECEIPT_HEADER = "\n==============W 편의점================";
    private static final String RECEIPT_PROMOTION = "=============증\t정===============";
    private static final String RECEIPT_FOOTER = "====================================";
    private static final String OUT_OF_STOCK = "재고 없음";
    private static final String STOCK_SUFFIX = "개";

    public void printFirstMessage() {
        Optional.of("\n안녕하세요. W편의점입니다.\n현재 보유하고 있는 상품입니다.")
                .ifPresent(System.out::println);
    }

    public void printProductList(Product product) {
        Optional.of(product)
                .ifPresent(p -> System.out.printf("- %s %,d원 %s%s%n",
                        p.getName(),
                        p.getPrice(),
                        getStockText(p),
                        getPromotionText(p)));
    }

    private String getPromotionText(Product product) {
        return Optional.ofNullable(product)
                .filter(Product::hasPromotion)
                .map(p -> " " + p.getPromotion())
                .orElse("");
    }

    private String getStockText(Product product) {
        return Optional.of(product)
                .map(Product::getTotalStock)
                .filter(stock -> stock > 0)
                .map(stock -> stock + STOCK_SUFFIX)
                .orElse(OUT_OF_STOCK);
    }

    public void printError(String message) {
        Optional.ofNullable(message)
                .ifPresent(msg -> System.out.println("\n" + msg));
    }

    public void printReceipt(Cart cart, int totalPrice, int promotionDiscount, int membershipDiscount) {
        Optional.of(new ReceiptPrinter(cart, totalPrice, promotionDiscount, membershipDiscount))
                .ifPresent(ReceiptPrinter::print);
    }

    private class ReceiptPrinter {
        private final Cart cart;
        private final int totalPrice;
        private final int promotionDiscount;
        private final int membershipDiscount;

        private ReceiptPrinter(Cart cart, int totalPrice, int promotionDiscount, int membershipDiscount) {
            this.cart = cart;
            this.totalPrice = totalPrice;
            this.promotionDiscount = promotionDiscount;
            this.membershipDiscount = membershipDiscount;
        }

        public void print() {
            printSection(this::printHeader);
            printSection(this::printOrderDetails);
            printSection(this::printPromotionalItems);
            printSection(this::printPriceDetails);
        }

        private void printSection(Runnable printer) {
            Optional.of(printer).ifPresent(Runnable::run);
        }

        private void printHeader() {
            System.out.println(RECEIPT_HEADER);
            System.out.println("상품명\t\t수량\t금액");
        }

        private void printOrderDetails() {
            Optional.of(cart)
                    .map(Cart::getOrders)
                    .ifPresent(orders -> orders.forEach(this::printOrderLine));
        }

        private void printOrderLine(Order order) {
            System.out.printf("%s\t\t%d\t%,d%n",
                    order.getProduct().getName(),
                    order.getQuantity(),
                    order.calculateTotalPrice());
        }

        private void printPromotionalItems() {
            System.out.println(RECEIPT_PROMOTION);
            Optional.of(cart)
                    .map(Cart::getPromotionalOrders)
                    .ifPresent(orders -> orders.forEach(this::printPromotionLine));
        }

        private void printPromotionLine(Order order) {
            System.out.printf("%s\t\t%d%n",
                    order.getProduct().getName(),
                    1);
        }

        private void printPriceDetails() {
            System.out.println(RECEIPT_FOOTER);
            printPriceDetail("총구매액", "", totalPrice);
            printPriceDetail("행사할인", "-", promotionDiscount);
            printPriceDetail("멤버십할인", "-", membershipDiscount);
            printFinalPrice();
        }

        private void printPriceDetail(String label, String prefix, int amount) {
            System.out.printf("%s\t\t\t%s%,d%n", label, prefix, amount);
        }

        private void printFinalPrice() {
            int finalPrice = totalPrice - promotionDiscount - membershipDiscount;
            printPriceDetail("내실돈", "", finalPrice);
        }
    }
}