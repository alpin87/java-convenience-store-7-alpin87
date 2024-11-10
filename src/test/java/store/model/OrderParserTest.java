package store.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import store.util.OrderParser;

class OrderParserTest {

    @Test
    void 정상적인_주문_파싱() {
        List<OrderRequest> requests = OrderParser.parseOrders("[콜라-2],[물-1]");

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).productName()).isEqualTo("콜라");
        assertThat(requests.get(0).quantity()).isEqualTo(2);
    }

    @Test
    void 잘못된_형식의_주문_파싱() {
        assertThatThrownBy(() -> OrderParser.parseOrders("콜라-2,물-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 빈_입력_파싱() {
        assertThatThrownBy(() -> OrderParser.parseOrders(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
