package playground;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class FrostAlarmTest {

    // 1. Stub & 2. Spy example combined
    @Test
    void shouldSoundSirenOnFrost() {
        // Stub: returns -0.5
        Thermometer stubThermometer = () -> -0.5;
        
        // Spy: records every reason given
        class RecordingSiren implements Siren {
            final List<String> reasons = new ArrayList<>();
            @Override public void sound(String reason) { reasons.add(reason); }
        }
        RecordingSiren spySiren = new RecordingSiren();

        var alarm = new FrostAlarm(stubThermometer, spySiren);
        alarm.check();

        assertEquals(List.of("frost"), spySiren.reasons);
    }

    // 4. Dummy example for null check
    @Test
    void shouldRejectNullThermometer() {
        Siren dummySiren = reason -> { throw new AssertionError("should not be called"); };
        assertThrows(NullPointerException.class, () -> new FrostAlarm(null, dummySiren));
    }
}