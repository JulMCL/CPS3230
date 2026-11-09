package mt.edu.um.cps3230.ferry.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Target B Design: Price Calculator, Group Rate Boundaries & Vehicles
 * - Decision Table / Interactions: Passenger age, residency, and fare rules.
 * - Boundaries: Group rate threshold at exactly 10 fare-paying passengers 
 *   (verifying that infants do not count toward the 10).
 * - Equivalence Classes: Vehicles add a fixed €15.70 and are never discounted.
 */
class PriceCalculatorTargetTest {

    private final PriceCalculator calculator = new PriceCalculator();
    private final LocalDate sampleSailing = Fixtures.SAILING_DATE;

    @ParameterizedTest(name = "Group rate boundary: {0} adults -> expected total cents {1}")
    @CsvSource(textBlock = """
        9,  4185
        10, 3953
        """)
    void shouldApplyGroupRateFromTenFarePayers(int adultCount, long expectedCents) {
        var passengers = Fixtures.adults(adultCount);
        var price = calculator.priceFor(sampleSailing, passengers, false);
        assertEquals(Money.ofCents(expectedCents), price);
    }

    @Test
    void shouldNotCountInfantsTowardsGroupRate() {
        // 9 adults + 1 infant (aged 1 year, for instance) -> should NOT trigger group rate (stays at 9 fare-payers price)
        var passengers = new ArrayList<>(Fixtures.adults(9));
        passengers.add(Fixtures.aged(1)); // Infant under 3
        
        var price = calculator.priceFor(sampleSailing, passengers, false);
        assertEquals(Money.ofCents(4185), price);
    }

    @ParameterizedTest(name = "Vehicle pricing: hasVehicle={0} -> expected price {1} cents")
    @CsvSource(textBlock = """
        false, 465
        true,  2035
        """)
    void shouldAddVehicleFeeWithoutDiscount(boolean hasVehicle, long expectedCents) {
        var passengers = List.of(Fixtures.adult());
        var price = calculator.priceFor(sampleSailing, passengers, hasVehicle);
        assertEquals(Money.ofCents(expectedCents), price);
    }
}