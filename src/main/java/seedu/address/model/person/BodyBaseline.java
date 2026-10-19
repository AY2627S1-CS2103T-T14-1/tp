package seedu.address.model.person;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Immutable starting measurements. Decimal values have canonical scale two. */
public record BodyBaseline(LocalDate date, BigDecimal weightKg, BigDecimal heightCm) {
    /** Creates a validated BodyBaseline instance. */
    public BodyBaseline {
        date = BodyMeasurements.validateDate(date);
        weightKg = BodyMeasurements.validateDecimal(weightKg, false);
        heightCm = BodyMeasurements.validateDecimal(heightCm, true);
    }
}
