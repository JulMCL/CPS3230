package playground;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FrostAlarmMockitoTest {

    @Mock Thermometer thermometer;
    @Mock Siren siren;

    @Test
    void shouldSoundSirenWhenFreezingWithMockito() {
        // Stubbing
        when(thermometer.celsius()).thenReturn(-1.5);

        var alarm = new FrostAlarm(thermometer, siren);
        alarm.check();

        // Verification
        verify(siren).sound("frost");
    }

    @Test
    void shouldStaySilentAtZeroOrAbove() {
        when(thermometer.celsius()).thenReturn(0.0);

        var alarm = new FrostAlarm(thermometer, siren);
        alarm.check();

        // Verify it never sounded
        verify(siren, never()).sound(any());
    }

    @Test
    void shouldHandleSensorFault() {
        when(thermometer.celsius()).thenThrow(new SensorFault());

        var alarm = new FrostAlarm(thermometer, siren);
        alarm.check();

        verify(siren).sound("sensor fault");
    }
}