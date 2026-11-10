package playground;

import java.util.Objects;

interface Thermometer {
    double celsius();
}

interface Siren {
    void sound(String reason);
}

class SensorFault extends RuntimeException {}

public class FrostAlarm {
    private final Thermometer thermometer;
    private final Siren siren;

    public FrostAlarm(Thermometer thermometer, Siren siren) {
        this.thermometer = Objects.requireNonNull(thermometer, "Thermometer cannot be null");
        this.siren = Objects.requireNonNull(siren, "Siren cannot be null");
    }

    public void check() {
        try {
            double temp = thermometer.celsius();
            if (temp < 0.0) {
                siren.sound("frost");
            }
        } catch (SensorFault e) {
            siren.sound("sensor fault");
        }
    }
}