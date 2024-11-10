package store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import store.model.Cart;
import store.model.Product;
import store.model.Promotion;
import store.util.FileReader;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class ProductServiceTest {
    private ProductService productService;
    private FileReader fileReader;

    @BeforeEach
    void setUp() {
        fileReader = new TestFileReader();
        productService = new ProductService(fileReader);
    }

    @Test
    void 재고_확인() {
        assertThat(productService.checkStock("콜라", 5)).isTrue();
        assertThat(productService.checkStock("콜라", 15)).isFalse();
    }

    @Test
    void 상품_찾기_성공() {
        Product product = productService.findProduct("콜라");

        assertThat(product.getName()).isEqualTo("콜라");
        assertThat(product.hasPromotion()).isTrue();
    }

    @Test
    void 존재하지_않는_상품_찾기_실패() {
        assertThatThrownBy(() -> productService.findProduct("없는상품"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void MD추천상품_확인() {
        assertThat(productService.isMDRecommendationPromotion("오렌지주스")).isTrue();
        assertThat(productService.isMDRecommendationPromotion("콜라")).isFalse();
    }

    @Test
    void 프로모션_수량_계산() {
        String productName = "콜라";
        ProductService.OrderProcessingResult result = productService.processOrder(productName, 3);

        assertThat(result.getTotalQuantity()).isEqualTo(3);
    }

    private static class TestFileReader extends FileReader {
        @Override
        public List<Product> getProducts() {
            return List.of(
                    new Product("콜라", 1000, 10, "탄산2+1"),
                    new Product("콜라", 1000, 10, null),
                    new Product("오렌지주스", 1800, 9, "MD추천상품")
            );
        }

        @Override
        public List<Promotion> getPromotions() {
            LocalDateTime now = LocalDateTime.now();
            return List.of(
                    new Promotion("탄산2+1", 2, 1, now.minusDays(1), now.plusDays(1)),
                    new Promotion("MD추천상품", 1, 1, now.minusDays(1), now.plusDays(1))
            );
        }
    }
}

class OrderServiceTest {
    private OrderService orderService;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        FileReader fileReader = new TestFileReader();
        productService = new ProductService(fileReader);
        orderService = new OrderService(productService);
    }

    @Test
    void 장바구니_상품추가() {
        orderService.addToCart("콜라", 2, true);
        Cart cart = orderService.getCart();

        assertThat(cart.getOrders()).hasSize(1);
        assertThat(cart.getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void 멤버십_할인_계산() {
        orderService.addToCart("콜라", 10, false);
        int totalPrice = orderService.calculateTotalPrice();
        int promotionDiscount = 0;

        int membershipDiscount = orderService.calculateMembershipDiscount(totalPrice, promotionDiscount);

        assertThat(membershipDiscount).isLessThanOrEqualTo(8000);
    }

    @Test
    void 프로모션_할인_계산() {
        orderService.addToCart("콜라", 3, true);

        int discountAmount = orderService.calculatePromotionDiscount();
        assertThat(discountAmount).isEqualTo(1000);
    }

    @Test
    void 장바구니_초기화() {
        orderService.addToCart("콜라", 2, true);
        orderService.clearCart();

        assertThat(orderService.getCart().getOrders()).isEmpty();
    }

    @Test
    void 주문_처리_및_적용() {
        orderService.processOrder("콜라", 3);
        orderService.applyPendingOrders();

        assertThat(orderService.getCart().getTotalQuantity()).isEqualTo(3);
    }

    private static class TestFileReader extends FileReader {
        @Override
        public List<Product> getProducts() {
            return List.of(
                    new Product("콜라", 1000, 10, "탄산2+1"),
                    new Product("콜라", 1000, 10, null)
            );
        }

        @Override
        public List<Promotion> getPromotions() {
            LocalDateTime now = LocalDateTime.now();
            return List.of(
                    new Promotion("탄산2+1", 2, 1, now.minusDays(1), now.plusDays(1))
            );
        }
    }
}