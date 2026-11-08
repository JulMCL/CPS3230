import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StringCalculatorAITest {

    private final StringCalculator calculator = new StringCalculator();

    @Nested
    @DisplayName("Empty and single-number input")
    class BasicInput {

        @Test
        @DisplayName("empty string returns 0")
        void emptyStringReturnsZero() {
            assertEquals(0, calculator.add(""));
        }

        @Test
        @DisplayName("single number returns its value")
        void singleNumberReturnsItself() {
            assertEquals(1, calculator.add("1"));
        }

        @Test
        @DisplayName("zero is handled")
        void zeroReturnsZero() {
            assertEquals(0, calculator.add("0"));
        }
    }

    @Nested
    @DisplayName("Default delimiters (comma and newline)")
    class DefaultDelimiters {

        @Test
        @DisplayName("two numbers separated by comma are summed")
        void twoNumbersWithComma() {
            assertEquals(3, calculator.add("1,2"));
        }

        @Test
        @DisplayName("numbers separated by newline are summed")
        void numbersWithNewline() {
            assertEquals(6, calculator.add("1\n2,3"));
        }

        @Test
        @DisplayName("only newlines as delimiters")
        void onlyNewlines() {
            assertEquals(6, calculator.add("1\n2\n3"));
        }

        @Test
        @DisplayName("many numbers are summed")
        void manyNumbers() {
            assertEquals(55, calculator.add("1,2,3,4,5,6,7,8,9,10"));
        }

        @Test
        @DisplayName("trailing delimiter is ignored")
        void trailingDelimiterIgnored() {
            assertEquals(3, calculator.add("1,2,"));
        }

        @ParameterizedTest(name = "\"{0}\" = {1}")
        @CsvSource(delimiter = '|', value = {
                "0,0 | 0",
                "10,20,30 | 60",
                "5\n5 | 10",
                "100,200,300,400 | 1000"
        })
        void variousValidInputs(String input, int expected) {
            assertEquals(expected, calculator.add(input.replace("\\n", "\n")));
        }
    }

    @Nested
    @DisplayName("Numbers greater than 1000")
    class LargeNumbers {

        @Test
        @DisplayName("1000 is included in the sum")
        void exactlyThousandIsIncluded() {
            assertEquals(1000, calculator.add("1000"));
        }

        @Test
        @DisplayName("1001 is treated as 0")
        void justAboveThousandIgnored() {
            assertEquals(0, calculator.add("1001"));
        }

        @Test
        @DisplayName("large numbers are ignored while others are summed")
        void largeNumbersIgnoredInSum() {
            assertEquals(2, calculator.add("2,1001"));
        }

        @Test
        @DisplayName("multiple large numbers are all ignored")
        void multipleLargeNumbers() {
            assertEquals(5, calculator.add("1001,5,2000,9999"));
        }

        @Test
        @DisplayName("sum of values up to 1000 is not capped")
        void sumOfValidValuesExceedsThousand() {
            assertEquals(2000, calculator.add("1000,1000"));
        }
    }

    @Nested
    @DisplayName("Single custom delimiter (//<delim>\\n)")
    class SingleCustomDelimiter {

        @Test
        @DisplayName("semicolon delimiter")
        void semicolonDelimiter() {
            assertEquals(3, calculator.add("//;\n1;2"));
        }

        @Test
        @DisplayName("asterisk delimiter (regex metacharacter is escaped)")
        void asteriskDelimiter() {
            assertEquals(6, calculator.add("//*\n1*2*3"));
        }

        @Test
        @DisplayName("period delimiter (regex metacharacter is escaped)")
        void periodDelimiter() {
            assertEquals(3, calculator.add("//.\n1.2"));
        }

        @Test
        @DisplayName("non-ASCII delimiter")
        void unicodeDelimiter() {
            assertEquals(6, calculator.add("//£\n1£2£3"));
        }

        @Test
        @DisplayName("default comma is no longer a delimiter when custom one is set")
        void commaNotDelimiterWhenCustomSet() {
            assertThrows(NumberFormatException.class, () -> calculator.add("//;\n1,2;3"));
        }

        @Test
        @DisplayName("custom delimiter with large numbers ignores values over 1000")
        void customDelimiterLargeNumbers() {
            assertEquals(3, calculator.add("//;\n1;1001;2"));
        }
    }

    @Nested
    @DisplayName("Bracketed custom delimiters (//[<delim>]\\n)")
    class BracketedCustomDelimiter {

        @Test
        @DisplayName("single-character bracketed delimiter")
        void singleCharBracketed() {
            assertEquals(6, calculator.add("//[*]\n1*2*3"));
        }

        @Test
        @DisplayName("multi-character bracketed delimiter")
        void multiCharBracketed() {
            assertEquals(6, calculator.add("//[***]\n1***2***3"));
        }

        @Test
        @DisplayName("multiple single-character delimiters")
        void multipleSingleCharDelimiters() {
            assertEquals(6, calculator.add("//[*][%]\n1*2%3"));
        }

        @Test
        @DisplayName("multiple multi-character delimiters")
        void multipleMultiCharDelimiters() {
            assertEquals(6, calculator.add("//[**][%%]\n1**2%%3"));
        }

        @Test
        @DisplayName("bracketed delimiter containing regex metacharacters")
        void bracketedRegexMetacharacters() {
            assertEquals(6, calculator.add("//[.][|]\n1.2|3"));
        }

        @Test
        @DisplayName("bracketed delimiter with a single number and no delimiters")
        void singleNumberWithBracketedHeader() {
            assertEquals(7, calculator.add("//[***]\n7"));
        }

        @Test
        @DisplayName("bracketed delimiter ignores numbers over 1000")
        void bracketedDelimiterLargeNumbers() {
            assertEquals(3, calculator.add("//[***]\n1***1001***2"));
        }
    }

    @Nested
    @DisplayName("Negative numbers")
    class NegativeNumbers {

        @Test
        @DisplayName("single negative number throws")
        void singleNegativeThrows() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculator.add("-1"));
            assertEquals("Negatives are not allowed: [-1]", ex.getMessage());
        }

        @Test
        @DisplayName("all negatives are reported in the message")
        void multipleNegativesReported() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculator.add("1,-2,3,-4"));
            assertEquals("Negatives are not allowed: [-2, -4]", ex.getMessage());
        }

        @Test
        @DisplayName("negatives are detected with custom delimiters")
        void negativesWithCustomDelimiter() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculator.add("//;\n-1;2;-3"));
            assertEquals("Negatives are not allowed: [-1, -3]", ex.getMessage());
        }

        @Test
        @DisplayName("negatives are detected with bracketed delimiters")
        void negativesWithBracketedDelimiter() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculator.add("//[***]\n-5***10"));
            assertEquals("Negatives are not allowed: [-5]", ex.getMessage());
        }

        @Test
        @DisplayName("negative numbers are rejected even alongside large numbers")
        void negativeAndLargeNumber() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> calculator.add("-5,2000"));
            assertEquals("Negatives are not allowed: [-5]", ex.getMessage());
        }

        @ParameterizedTest
        @ValueSource(strings = {"-1", "0,-1", "-1000", "-1001,1"})
        @DisplayName("various negative inputs throw")
        void variousNegativeInputs(String input) {
            assertThrows(IllegalArgumentException.class, () -> calculator.add(input));
        }
    }

    @Nested
    @DisplayName("Invalid input")
    class InvalidInput {

        @Test
        @DisplayName("null input throws NullPointerException")
        void nullInputThrows() {
            assertThrows(NullPointerException.class, () -> calculator.add(null));
        }

        @Test
        @DisplayName("non-numeric token throws NumberFormatException")
        void nonNumericThrows() {
            assertThrows(NumberFormatException.class, () -> calculator.add("1,abc"));
        }

        @Test
        @DisplayName("whitespace around numbers is not trimmed and throws")
        void whitespaceThrows() {
            assertThrows(NumberFormatException.class, () -> calculator.add("1, 2"));
        }
    }
}