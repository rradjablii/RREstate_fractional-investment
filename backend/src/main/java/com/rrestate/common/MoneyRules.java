package com.rrestate.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Explicit numeric policy. All money is BigDecimal; nothing is rounded implicitly.
 * Storage: NUMERIC(19,4). Client input: at most 2 decimals. Percentages shown with 2 decimals,
 * stored interest with 6. Rounding: HALF_EVEN for ratios, DOWN for distribution allocations
 * (so allocations never exceed the distributed total).
 */
public final class MoneyRules {
    public static final int STORAGE_SCALE = 4;
    public static final int INPUT_SCALE = 2;
    public static final int PERCENT_SCALE = 2;
    public static final int INTEREST_SCALE = 6;
    public static final RoundingMode RATIO_ROUNDING = RoundingMode.HALF_EVEN;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private MoneyRules() {}

    /** part / whole * 100, or zero when whole is zero. */
    public static BigDecimal percentOf(BigDecimal part, BigDecimal whole, int scale) {
        if (whole.signum() == 0) return BigDecimal.ZERO.setScale(scale);
        return part.multiply(HUNDRED).divide(whole, scale, RATIO_ROUNDING);
    }

    /** Pro-rata share of total, rounded DOWN to storage scale. */
    public static BigDecimal share(BigDecimal total, BigDecimal part, BigDecimal whole) {
        if (whole.signum() == 0) return BigDecimal.ZERO.setScale(STORAGE_SCALE);
        return total.multiply(part).divide(whole, STORAGE_SCALE, RoundingMode.DOWN);
    }

    public static boolean hasAtMostInputScale(BigDecimal v) {
        return v.stripTrailingZeros().scale() <= INPUT_SCALE;
    }

    /** Wire format: plain decimal string (never scientific notation). */
    public static String str(BigDecimal v) {
        return v == null ? null : v.toPlainString();
    }
}
