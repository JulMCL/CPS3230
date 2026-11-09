package mt.edu.um.cps3230.ferry.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/*
 * Target A Design: Passenger Age & Boundaries on Sailing Dates
 *  - Equivalence Classes:
 *      - Infant: Age < 4
 *      - Child: Age 3 to 11 inclusive
 *      - Adult: Age 12+
 *  - Boundaries:
 *      - Infant/Child boundary (turning 3 on, day before, and day after sailing)
 *      - Child/Adult boundary (turning 12 on, day before, and day after sailing)
 *      - Leap year edge case (born on 29 February)
 */
public class PassengerAgeTest {
    
    @ParameterizedTest(name = "Born {0}, sailing on {1} -> expected age {2}, infant: {3}, child: {4}")
    @CsvSource(textBlock = """
        2023-10-14, 2026-10-13,  2, true,  false
        2023-10-13, 2026-10-13,  3, false, true
        2023-10-12, 2026-10-13,  3, false, true
        2014-10-14, 2026-10-13, 11, false, true
        2014-10-13, 2026-10-13, 12, false, false
        2014-10-12, 2026-10-13, 12, false, false
        """)
    void shouldCalculateAgeAndCategoriesCorrectlyOnSailingDate(LocalDate dob, 
        LocalDate sailingDate, int expectedAge, boolean expectedInfant, boolean expectedChild) {
            var passenger = new Passenger("Test Name", dob, false);

            assertEquals(expectedAge, passenger.ageOn(sailingDate));
            assertEquals(expectedInfant, passenger.isInfantOn(sailingDate));
            assertEquals(expectedChild, passenger.isChildOn(sailingDate));
    }

    @ParameterizedTest(name = "Leap year birth 2020-02-29 checked on non-leap year date {0} -> age {1}")
    @CsvSource(textBlock = """
        2025-02-28, 4
        2025-03-01, 5
        """)
    void shouldHandleLeapYearBirthdaysCorrectly(LocalDate sailingDate, int expectedAge) {
        var passenger = new Passenger("Leap Baby", LocalDate.of(2020, 2, 29), false);
        assertEquals(expectedAge, passenger.ageOn(sailingDate));
    }

}
