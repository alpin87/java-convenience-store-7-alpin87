package store.model;

import org.junit.jupiter.api.Test;
import store.model.YesNo;

import static org.assertj.core.api.Assertions.*;

class YesNoTest {

    @Test
    void YES_입력변환() {
        assertThat(YesNo.from("Y").isYes()).isTrue();
    }

    @Test
    void NO_입력변환() {
        assertThat(YesNo.from("N").isYes()).isFalse();
    }

    @Test
    void 잘못된_입력처리() {
        assertThatThrownBy(() -> YesNo.from("X"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}