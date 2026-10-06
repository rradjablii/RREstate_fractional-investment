package com.rrestate.common;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyRulesTest {
    @Test
    void percentUsesExplicitScaleAndHalfEven() {
        assertThat(MoneyRules.percentOf(new BigDecimal("1000"), new BigDecimal("10000"), 6)).isEqualByComparingTo("10.000000");
        assertThat(MoneyRules.percentOf(new BigDecimal("1"), new BigDecimal("3"), 2)).isEqualByComparingTo("33.33");
        assertThat(MoneyRules.percentOf(new BigDecimal("1"), BigDecimal.ZERO, 2)).isEqualByComparingTo("0");
    }

    @Test
    void sharesRoundDownSoAllocationsNeverExceedTotal() {
        BigDecimal total = new BigDecimal("100.00");
        BigDecimal whole = new BigDecimal("3");
        BigDecimal one = MoneyRules.share(total, BigDecimal.ONE, whole);
        assertThat(one).isEqualByComparingTo("33.3333");
        assertThat(one.multiply(whole)).isLessThanOrEqualTo(total);
    }

    @Test
    void inputScaleAllowsTwoDecimals() {
        assertThat(MoneyRules.hasAtMostInputScale(new BigDecimal("10.50"))).isTrue();
        assertThat(MoneyRules.hasAtMostInputScale(new BigDecimal("10.500"))).isTrue();
        assertThat(MoneyRules.hasAtMostInputScale(new BigDecimal("10.505"))).isFalse();
    }

    @Test
    void wireFormatIsPlainString() {
        assertThat(MoneyRules.str(new BigDecimal("1E+3"))).isEqualTo("1000");
    }
}
