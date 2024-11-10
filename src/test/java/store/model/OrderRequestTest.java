package store.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrderRequestTest {

    @Test
    void 주문요청_생성_성공() {
        OrderRequest request = new OrderRequest("물", 2);

        assertThat(request.productName()).isEqualTo("물");
        assertThat(request.quantity()).isEqualTo(2);
    }

    @Test
    void 주문요청_수량이_0이하면_실패() {
        assertThatThrownBy(() -> new OrderRequest("물", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
