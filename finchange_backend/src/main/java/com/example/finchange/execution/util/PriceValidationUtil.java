package com.example.finchange.execution.util;

import java.math.BigDecimal;

public class PriceValidationUtil {

    public static boolean isPriceTickValid(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }

        BigDecimal tick;
        if (price.compareTo(new BigDecimal("19.999")) <= 0) {
            tick = new BigDecimal("0.010");
        } else if (price.compareTo(new BigDecimal("49.999")) <= 0) {
            tick = new BigDecimal("0.020");
        } else if (price.compareTo(new BigDecimal("99.999")) <= 0) {
            tick = new BigDecimal("0.050");
        } else if (price.compareTo(new BigDecimal("249.999")) <= 0) {
            tick = new BigDecimal("0.100");
        } else if (price.compareTo(new BigDecimal("499.999")) <= 0) {
            tick = new BigDecimal("0.250");
        } else if (price.compareTo(new BigDecimal("999.999")) <= 0) {
            tick = new BigDecimal("0.500");
        } else if (price.compareTo(new BigDecimal("2499.999")) <= 0) {
            tick = new BigDecimal("1.000");
        } else {
            tick = new BigDecimal("2.500");
        }

        return price.remainder(tick).compareTo(BigDecimal.ZERO) == 0;
    }
}
