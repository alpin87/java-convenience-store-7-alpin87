package store.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProductTest {
    private Product promotionalProduct;
    private Product normalProduct;

    @BeforeEach
    void setUp() {
        promotionalProduct = new Product("콜라", 1000, 10, "탄산2+1");
        normalProduct = new Product("물", 500, 10, null);
    }

    @Test
    void 프로모션상품_생성_검증() {
        assertThat(promotionalProduct.getName()).isEqualTo("콜라");
        assertThat(promotionalProduct.getPrice()).isEqualTo(1000);
        assertThat(promotionalProduct.getPromotionalStock()).isEqualTo(10);
        assertThat(promotionalProduct.hasPromotion()).isTrue();
    }

    @Test
    void 일반상품_생성_검증() {
        assertThat(normalProduct.getNormalStock()).isEqualTo(10);
        assertThat(normalProduct.getPromotionalStock()).isEqualTo(0);
        assertThat(normalProduct.hasPromotion()).isFalse();
    }

    @Test
    void 재고확인() {
        assertThat(normalProduct.canFulfillOrder(5)).isTrue();
        assertThat(normalProduct.canFulfillOrder(11)).isFalse();
    }
}

class PromotionTest {
    private LocalDateTime now;
    private LocalDateTime yesterday;
    private LocalDateTime tomorrow;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        yesterday = now.minusDays(1);
        tomorrow = now.plusDays(1);
    }

    @Test
    void 프로모션_유효기간_검증() {
        Promotion promotion = new Promotion("탄산2+1", 2, 1, yesterday, tomorrow);
        assertThat(promotion.isValid()).isTrue();
    }

    @Test
    void 프로모션_시작일이_종료일보다_늦으면_실패() {
        assertThatThrownBy(() ->
                new Promotion("탄산2+1", 2, 1, tomorrow, yesterday)
        ).isInstanceOf(IllegalArgumentException.class);
    }
}