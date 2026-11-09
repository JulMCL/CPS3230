package mt.edu.um.cps3230.ferry.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Target C Design: Money Ranges and Rounding Boundaries
 * - Equivalence Classes for ofEuros cents: Valid (0-99), Invalid (<0, >99)
 * - Equivalence Classes for lessPercent: Valid (0-100), Invalid (<0, >100)
 * - Rounding Boundaries: Half-up rounding behavior at exact half-cent thresholds.
 */
class MoneyWeek3Test {

    @ParameterizedTest(name = "Valid cents {0} should succeed, invalid should throw exception")
    @CsvSource(textBlock = """
        0,    true
        99,   true
        -1,   false
        100,  false
        """)
    void shouldEnforceValidCentRange(int cents, boolean isValid) {
        if (isValid) {
            assertDoesNotThrow(() -> Money.ofEuros(10, cents));
        } else {
            assertThrows(IllegalArgumentException.class, () -> Money.ofEuros(10, cents));
        }
    }

    @ParameterizedTest(name = "Valid percentage {0} should succeed, invalid should throw exception")
    @CsvSource(textBlock = """
        0,   true
        100, true
        -1,  false
        101, false
        """)
    void shouldEnforceValidPercentageRange(int percent, boolean isValid) {
        var money = Money.ofEuros(10, 0);
        if (isValid) {
            assertDoesNotThrow(() -> money.lessPercent(percent));
        } else {
            assertThrows(IllegalArgumentException.class, () -> money.lessPercent(percent));
        }
    }

    @Test
    void shouldRoundHalfUpCorrectly() {
        // Example: Testing percentage reduction rounding behavior
        // e.g., an amount where 15% results in a half-cent boundary
        var money = Money.ofCents(15); // €0.15
        var reduced = money.lessPercent(50); // 50% of 15 cents is 7.5 cents -> rounds to 8 cents (half-up)
        assertEquals(Money.ofCents(8), reduced);
    }
}