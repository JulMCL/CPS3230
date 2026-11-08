import mt.edu.um.cps3230.ferry.domain.Money;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import org.junit.jupiter.api.Test;

public class MoneyTest {

    @Test
    void shouldReturnTheSameExactAmount() {
        Money money = new Money(66);
        assertThat(money.cents()).isEqualTo(66);
    }

    @Test
    void shouldRefuseNegativeAmountsOfMoney() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new Money(-30);
        });
        
        assertThat(exception.getMessage()).isNotNull();
    }

    @Test
    void shouldReturnAmountOfMoneyInEuros() {
        Money cash = Money.ofEuros(31, 00);
        assertThat(cash).isEqualTo(Money.ofEuros(31, 00));
    }

    @Test
    void shouldRoundUpWhenTheFractionalCentIsHalfOrGreater() {
        Money rounding = new Money(105);
        Money result = rounding.lessPercent(3);

        assertEquals(102L, result.cents());
    }

    @ParameterizedTest
    @CsvSource({
        "105, 3, 102",  // 105 cents minus 3 percent rounds/calculates to 102
        "200, 10, 180", // 200 cents minus 10 percent is 180
        "50, 0, 50"     // 50 cents minus 0 percent is 50
    })
    void shouldCalculatePercentageCorrectly(long initialCents, int percent, long expectedCents) {
        Money money = new Money(initialCents);
        Money result = money.lessPercent(percent);

        assertEquals(expectedCents, result.cents());
    }

}