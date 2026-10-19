package seedu.address.model.person;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/** Immutable dated weight and optional notes. */
public record BodyProgressEntry(LocalDate date, BigDecimal weightKg, Optional<String> notes) {
    /** Creates a validated BodyProgressEntry instance. */
    public BodyProgressEntry {
        date = BodyMeasurements.validateDate(date);
        weightKg = BodyMeasurements.validateDecimal(weightKg, false);
        notes = Objects.requireNonNull(notes).map(BodyMeasurements::normalizeNotes);
    }
}
