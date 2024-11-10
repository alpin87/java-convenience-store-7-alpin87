package store.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CartTest {
    private Cart cart;
    private Product product;
    private Order order;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        product = new Product("콜라", 1000, 10, "탄산2+1");
        order = new Order(product, 2, true);
    }

    @Test
    void 장바구니_주문추가() {
        cart.addOrder(order);

        assertThat(cart.getOrders()).hasSize(1);
        assertThat(cart.getTotalQuantity()).isEqualTo(2);
    }

    @Test
    void 장바구니_총금액계산() {
        cart.addOrder(order);

        assertThat(cart.calculateTotalPrice()).isEqualTo(2000);
    }

    @Test
    void 프로모션_주문_필터링() {
        Product normalProduct = new Product("물", 500, 10, null);
        Order normalOrder = new Order(normalProduct, 1, false);

        cart.addOrder(order);
        cart.addOrder(normalOrder);

        assertThat(cart.getPromotionalOrders()).hasSize(1);
        assertThat(cart.getNormalOrders()).hasSize(1);
    }
}
