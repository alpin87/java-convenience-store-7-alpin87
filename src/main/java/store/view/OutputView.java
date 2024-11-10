package store.view;

import store.model.Cart;
import store.model.Order;
import store.model.Product;
import java.util.List;
import java.util.Optional;

public class OutputView {
    private static final String RECEIPT_HEADER = "==============W 편의점================";
    private static final String RECEIPT_PROMOTION = "=============증\t정===============";
    private static final String RECEIPT_FOOTER = "====================================";
    private static final String OUT_OF_STOCK = "재고 없음";
    private static final String STOCK_SUFFIX = "개";

    public void printFirstMessage() {
        Optional.of("안녕하세요. W편의점입니다.\n현재 보유하고 있는 상품입니다.\n")
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
        ReceiptFormatter formatter = new ReceiptFormatter(cart, totalPrice, promotionDiscount, membershipDiscount);
        String formattedReceipt = formatter.format();
        System.out.println(formattedReceipt);
    }

    private class ReceiptFormatter {
        private final Cart cart;
        private final int totalPrice;
        private final int promotionDiscount;
        private final int membershipDiscount;
        private final StringBuilder builder;

        private ReceiptFormatter(Cart cart, int totalPrice, int promotionDiscount, int membershipDiscount) {
            this.cart = cart;
            this.totalPrice = totalPrice;
            this.promotionDiscount = promotionDiscount;
            this.membershipDiscount = membershipDiscount;
            this.builder = new StringBuilder();
        }

        public String format() {
            appendNewLine();
            appendHeader();
            appendOrderDetails();
            appendPromotionalItems();
            appendPriceDetails();
            return builder.toString();
        }

        private void appendHeader() {
            builder.append(RECEIPT_HEADER).append('\n');
            builder.append("상품명\t\t수량\t금액\n");
        }

        private void appendOrderDetails() {
            List<Order> orders = cart.getOrders();
            for (Order order : orders) {
                builder.append(String.format("%s\t\t%d\t%,d%n",
                        order.getProduct().getName(),
                        order.getQuantity(),
                        order.calculateTotalPrice()));
            }
        }

        private void appendPromotionalItems() {
            builder.append(RECEIPT_PROMOTION).append('\n');
            List<Order> promotionalOrders = cart.getPromotionalOrders();
            for (Order order : promotionalOrders) {
                builder.append(String.format("%s\t\t%d%n",
                        order.getProduct().getName(),
                        1));
            }
        }

        private void appendPriceDetails() {
            builder.append(RECEIPT_FOOTER).append('\n');
            appendPriceDetail("총구매액", "", totalPrice);
            appendPriceDetail("행사할인", "-", promotionDiscount);
            appendPriceDetail("멤버십할인", "-", membershipDiscount);
            appendFinalPrice();
        }

        private void appendPriceDetail(String label, String prefix, int amount) {
            builder.append(String.format("%s\t\t\t%s%,d%n", label, prefix, amount));
        }

        private void appendFinalPrice() {
            int finalPrice = totalPrice - promotionDiscount - membershipDiscount;
            appendPriceDetail("내실돈", "", finalPrice);
        }

        private void appendNewLine() {
            builder.append('\n');
        }
    }
}