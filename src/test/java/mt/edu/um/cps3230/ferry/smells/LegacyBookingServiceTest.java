package mt.edu.um.cps3230.ferry.smells;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import mt.edu.um.cps3230.ferry.adapters.inmemory.CsvTimetableReader;
import mt.edu.um.cps3230.ferry.adapters.inmemory.InMemoryBookingRepository;
import mt.edu.um.cps3230.ferry.adapters.inmemory.InMemoryNotificationGateway;
import mt.edu.um.cps3230.ferry.adapters.inmemory.InMemorySailingRepository;
import mt.edu.um.cps3230.ferry.adapters.payment.FakePaymentGateway;
import mt.edu.um.cps3230.ferry.domain.Booking;
import mt.edu.um.cps3230.ferry.domain.BookingRequest;
import mt.edu.um.cps3230.ferry.domain.BookingService;
import mt.edu.um.cps3230.ferry.domain.BookingStatus;
import mt.edu.um.cps3230.ferry.domain.Passenger;
import mt.edu.um.cps3230.ferry.domain.PriceCalculator;
import mt.edu.um.cps3230.ferry.domain.Sailing;
import mt.edu.um.cps3230.ferry.domain.SailingFullException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BookingService}, kept from an earlier version of the
 * application. They all pass.
 */
class LegacyBookingServiceTest {

    private static final String CARD = "4111111111111111";
    private static final String EMAIL = "someone@example.com";

    private LocalDate tomorrow;
    private List<Sailing> sailings;
    private InMemoryBookingRepository bookings;
    private InMemoryNotificationGateway notifications;
    private BookingService service;

    @BeforeEach
    void setUp() {
        tomorrow = LocalDate.now().plusDays(1);
        sailings = CsvTimetableReader.fromClasspath().sailingsOn(tomorrow);
        bookings = new InMemoryBookingRepository();
        notifications = new InMemoryNotificationGateway();
        service = new BookingService(Clock.systemDefaultZone(),
                new InMemorySailingRepository(bookings, sailings), bookings, new FakePaymentGateway(),
                notifications, new PriceCalculator(), () -> "FF-" + System.currentTimeMillis());
    }

    @Test
    void shouldWorkWhenABookingIsMade() {
        Sailing sailing = sailings.get(2);
        List<Passenger> family = List.of(
                new Passenger("Anna", tomorrow.minusYears(40), false),
                new Passenger("Ben", tomorrow.minusYears(38), false),
                new Passenger("Carla", tomorrow.minusYears(7), false));
        BookingRequest request = BookingRequest.forPassengers(sailing.id(), family, EMAIL);

        assertEquals(1045, service.quote(request).cents());
        Booking reserved = service.reserve(request);
        assertEquals(BookingStatus.RESERVED, reserved.status());
        assertEquals(3, reserved.passengers().size());
        assertEquals("07:00", reserved.sailing().departsAt().toLocalTime().toString());
        Booking confirmed = service.confirm(reserved.reference(), CARD);
        assertEquals(BookingStatus.CONFIRMED, confirmed.status());
        assertTrue(confirmed.paymentReference().isPresent());
        assertEquals(1, notifications.sentCount());
        assertTrue(notifications.messagesTo(EMAIL).get(0).contains(reserved.reference()));
        Booking checkedIn = service.checkIn(reserved.reference());
        assertEquals(BookingStatus.CHECKED_IN, checkedIn.status());
        assertEquals(checkedIn, service.find(reserved.reference()));
        assertEquals(1, bookings.all().size());
    }

    @Test
    void shouldBeFullWhenTheSailingIsFull() {
        Sailing sailing = sailings.get(2);
        Passenger passenger = new Passenger("Anna", tomorrow.minusYears(40), false);

        service.reserve(BookingRequest.forPassengers(sailing.id(), Collections.nCopies(900, passenger), EMAIL));

        assertThrows(SailingFullException.class, () -> service.reserve(
                BookingRequest.forPassengers(sailing.id(), List.of(passenger), EMAIL)));
    }

    @Test
    void shouldChargeTheRightFareWhenPassengersAreDifferentAges() {
        Sailing sailing = sailings.get(0);
        for (int age = 0; age <= 70; age += 5) {
            Passenger passenger = new Passenger("Passenger " + age, tomorrow.minusYears(age), false);
            BookingRequest request = BookingRequest.forPassengers(sailing.id(), List.of(passenger), EMAIL);
            long fare = service.quote(request).cents();
            if (age < 3) {
                assertEquals(0, fare);
            } else if (age < 12) {
                assertEquals(115, fare);
            } else {
                assertEquals(465, fare);
            }
        }
    }

    @Test
    void shouldGiveEachBookingItsOwnReferenceWhenTwoAreMade() throws InterruptedException {
        Sailing sailing = sailings.get(5);
        Passenger passenger = new Passenger("Anna", tomorrow.minusYears(40), false);
        BookingRequest request = BookingRequest.forPassengers(sailing.id(), List.of(passenger), EMAIL);

        Booking first = service.reserve(request);
        Thread.sleep(1000);
        Booking second = service.reserve(request);

        assertNotEquals(first.reference(), second.reference());
    }

    @Test
    void shouldCancelWhenAskedTo() {
        Sailing sailing = sailings.get(5);
        Passenger passenger = new Passenger("Anna", tomorrow.minusYears(40), false);
        Booking booking = service.reserve(BookingRequest.forPassengers(sailing.id(), List.of(passenger), EMAIL));

        Booking cancelled = service.cancel(booking.reference());

        assertEquals(BookingStatus.CANCELLED, cancelled.status());
    }
}
