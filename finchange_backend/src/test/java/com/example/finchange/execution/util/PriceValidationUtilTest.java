package com.example.finchange.execution.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PriceValidationUtil.isPriceTickValid tests")
class PriceValidationUtilTest {

    @Test
    @DisplayName("Null, zero ve negatif fiyatlar geçersizdir")
    void nullZeroNegative_areInvalid() {
        assertThat(PriceValidationUtil.isPriceTickValid(null)).isFalse();
        assertThat(PriceValidationUtil.isPriceTickValid(BigDecimal.ZERO)).isFalse();
        assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("-1"))).isFalse();
    }

    @Nested
    @DisplayName("<= 19.999 tick=0.010")
    class Range_0_19_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("0.010"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("19.990"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("1.005"))).isFalse();
        }
    }

    @Nested
    @DisplayName("<= 49.999 tick=0.020")
    class Range_20_49_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("20.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("49.980"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("49.990"))).isFalse();
        }
    }

    @Nested
    @DisplayName("<= 99.999 tick=0.050")
    class Range_50_99_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("50.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("99.950"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("50.020"))).isFalse();
        }
    }

    @Nested
    @DisplayName("<= 249.999 tick=0.100")
    class Range_100_249_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("100.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("249.900"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("100.050"))).isFalse();
        }
    }

    @Nested
    @DisplayName("<= 499.999 tick=0.250")
    class Range_250_499_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("250.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("499.750"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("250.100"))).isFalse();
        }
    }

    @Nested
    @DisplayName("<= 999.999 tick=0.500")
    class Range_500_999_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("500.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("999.500"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("500.250"))).isFalse();
        }
    }

    @Nested
    @DisplayName("<= 2499.999 tick=1.000")
    class Range_1000_2499_999 {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("1000.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("2499.000"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("1000.500"))).isFalse();
        }
    }

    @Nested
    @DisplayName("> 2499.999 tick=2.500")
    class Range_2500_up {
        @Test void valid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("2500.000"))).isTrue();
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("2502.500"))).isTrue();
        }
        @Test void invalid_ticks() {
            assertThat(PriceValidationUtil.isPriceTickValid(new BigDecimal("2501.000"))).isFalse();
        }
    }
}
