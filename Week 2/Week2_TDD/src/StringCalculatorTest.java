import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class StringCalculatorTest {

    private final StringCalculator calculator = new StringCalculator();

    @Test
    void shouldReturnZeroWhenTheStringIsEmpty() {
        int result = calculator.add("");

        assertEquals(0, result);
    }

    @Test
    void shouldReturnSameNumberWhenTheStringHasOneNumber() {
        int result = calculator.add("1");

        assertEquals(1, result);
    }

    @Test
    void shouldReturnSumWhenTheStringHasTwoNumbers() {
        int result = calculator.add("1,2");

        assertEquals(3, result);
    }

    @Test
    void shouldReturnSumWhenTheStringHasAnyAmountOfNumbers() {
        int result = calculator.add("1,2,3,4,5");

        assertEquals(15, result);
    }

    @Test
    void shouldReturnSumWhenStringHasCommaOrNewLine() {
        int result = calculator.add("1\n2,3");

        assertEquals(6, result);
    }

    @Test
    @Disabled("Temporary blocking this test")
    void shouldThrowExceptionWhenDelimitersAreAdjacent() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.add("1,\n");
        });
    }

    @Test
    void shouldReturnSumBySupportingDifferentDelimiters() {
        int result = calculator.add("//;\n1;2;3");

        assertEquals(6, result);
    }

    @Test
    void shouldReturnSumBySupportingDifferentDelimiters2() {
        int result = calculator.add("//$\n1$2$3");

        assertEquals(6, result);
    }

    @Test
    void shouldThrowExceptionIfNegativeNumbersAreFound() {
        assertThrows(IllegalArgumentException.class, () -> {
            calculator.add("//$\n1$-2$3");
        });
    }

    @Test
    void shouldIgnoreNumbersBiggerThanOneThousand() {
        int result = calculator.add("//$\n2$1001");

        assertEquals(2, result);
    }

    @Test
    void shouldReturnSumWithDelimitersThatHaveDifferentLengths() {
        int result = calculator.add("//***\n1***2***3");

        assertEquals(6, result);
    }

    @Test
    void shouldSupportMultipleDelimiters() {
        int result = calculator.add("//[*][£]\n1*2£3£4");
    
        assertEquals(10, result);
    }

    @Test
    void shouldSupportMultipleDelimitersWithMoreThanOneCharacters() {
        int result = calculator.add("//[**][££]\n1**2££3££4");

        assertEquals(10, result);
    }
}