package store.view;

import store.model.Product;

public class OutputView {
    public void printFirstMessage() {
        System.out.println("안녕하세요. W편의점입니다.");
        System.out.println("현재 보유하고 있는 상품입니다.\n");
    }

    public void printProductList(Product product) {
        System.out.printf("- %s %,d원 %s%s%n",
                product.getName(),
                product.getPrice(),
                product.getStock() + "개",
                getPromotionText(product)
        );
    }

    private String getPromotionText(Product product) {
        if (product.hasPromotion()) {
            return " " + product.getPromotion();
        }
        return "";
    }

    public void printError(String message) {
        System.out.println(message);
    }
}
