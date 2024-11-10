package store.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OrderTest {
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("콜라", 1000, 10, "탄산2+1");
    }

    @Test
    void 주문생성_성공() {
        Order order = new Order(product, 2, true);

        assertThat(order.getQuantity()).isEqualTo(2);
        assertThat(order.calculateTotalPrice()).isEqualTo(2000);
        assertThat(order.isPromotional()).isTrue();
    }

    @Test
    void 주문생성_수량이_0이하면_실패() {
        assertThatThrownBy(() -> new Order(product, 0, false))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
