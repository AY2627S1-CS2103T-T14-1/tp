package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

class BodyProgressTest {
    private static final LocalDate BASE_DATE = LocalDate.of(2020, 1, 1);
    private static final BodyBaseline BASELINE = new BodyBaseline(BASE_DATE,
            new BigDecimal("75.5"), new BigDecimal("175"));

    @Test
    void measurements_strictBoundsAndSyntax() {
        assertEquals(new BigDecimal("75.50"), BodyMeasurements.parseDecimal(" 075.50 ", false));
        assertEquals(new BigDecimal("500.00"), BodyMeasurements.parseDecimal("500", false));
        assertEquals(new BigDecimal("300.00"), BodyMeasurements.parseDecimal("300", true));
        for (String value : List.of("0", "-1", "+75", ".5", "75.", "75.500", "1e2", "NaN",
                "Infinity", "75kg", "1,000", "500.01", "75\n")) {
            assertThrows(IllegalArgumentException.class, () -> BodyMeasurements.parseDecimal(value, false), value);
        }
        assertThrows(IllegalArgumentException.class, () -> BodyMeasurements.parseDecimal("300.01", true));
        assertThrows(IllegalArgumentException.class, () -> new BodyBaseline(BASE_DATE,
                new BigDecimal("1.001"), new BigDecimal("175")));
    }

    @Test
    void dates_strictCalendarRangeAndClock() {
        assertEquals(LocalDate.of(2020, 2, 29), BodyMeasurements.parseDate("2020-02-29"));
        for (String value : List.of("2020-02-30", "2019-02-29", "0000-01-01", "10000-01-01",
                "2020-1-01", "+2020-01-01", "2020-01-01\n")) {
            assertThrows(IllegalArgumentException.class, () -> BodyMeasurements.parseDate(value), value);
        }
        Clock clock = Clock.fixed(Instant.parse("2020-01-01T12:00:00Z"), ZoneOffset.UTC);
        BodyMeasurements.requireNotFuture(BASE_DATE, clock);
        assertThrows(IllegalArgumentException.class, () ->
                 BodyMeasurements.requireNotFuture(BASE_DATE.plusDays(1), clock));
    }

    @Test
    void notes_unicodeLimitsAndRawControlValidation() {
        String unicode = "\uD83D\uDCAA".repeat(300);
        assertEquals(unicode, BodyMeasurements.normalizeNotes(" " + unicode + " "));
        assertEquals("Walk  5 km/day", BodyMeasurements.normalizeNotes(" Walk  5 km/day "));
        for (String value : List.of("", "   ", unicode + "x", "note\n", "\tnote", "a\u0000b",
                "a\u200Bb", "a\u2028b", "a\uD800b")) {
            assertThrows(IllegalArgumentException.class, () -> BodyMeasurements.normalizeNotes(value));
        }
    }

    @Test
    void history_defensiveCopySortedUniqueAndImmutable() {
        BodyProgressEntry early = entry(8, "74.8", "Breakfast");
        BodyProgressEntry late = entry(15, "74.2", null);
        List<BodyProgressEntry> source = new ArrayList<>(List.of(late, early));
        BodyProgress progress = new BodyProgress(Optional.of(BASELINE), source);
        source.clear();
        assertEquals(List.of(early, late), progress.getEntries());
        assertEquals(late, progress.getLatestEntry().orElseThrow());
        assertThrows(UnsupportedOperationException.class, () -> progress.getEntries().clear());
        assertThrows(IllegalArgumentException.class, () ->
                 new BodyProgress(Optional.of(BASELINE), List.of(early, early)));
        assertThrows(IllegalArgumentException.class, () ->
                 new BodyProgress(Optional.empty(), List.of(early)));
        assertThrows(NullPointerException.class, () ->
                 new BodyProgress(Optional.of(BASELINE), java.util.Arrays.asList(early, null)));
    }

    @Test
    void corrections_preserveHistoryClearNotesAndDeriveLatest() {
        BodyProgress progress = new BodyProgress().withBaseline(BASELINE)
                .withEntry(entry(15, "74.2", null)).withEntry(entry(8, "74.8", "Breakfast"));
        BodyProgress replaced = progress.withEntry(entry(8, "74.6", null));
        assertEquals(2, replaced.getEntries().size());
        assertTrue(replaced.findEntry(BASE_DATE.withDayOfMonth(8)).orElseThrow().notes().isEmpty());
        assertEquals(15, replaced.getLatestEntry().orElseThrow().date().getDayOfMonth());
        assertThrows(IllegalArgumentException.class, () -> replaced.withBaseline(new BodyBaseline(
                BASE_DATE.withDayOfMonth(10), new BigDecimal("75"), new BigDecimal("175"))));
        assertThrows(IllegalArgumentException.class, () -> replaced.withEntry(entry(1, "74", null)));
        BodyProgress corrected = replaced.withBaseline(new BodyBaseline(BASE_DATE,
                new BigDecimal("75"), new BigDecimal("175")));
        assertEquals(replaced.getEntries(), corrected.getEntries());
        BodyProgress deleted = corrected.withoutEntry(BASE_DATE.withDayOfMonth(15));
        assertEquals(8, deleted.getLatestEntry().orElseThrow().date().getDayOfMonth());
        deleted = deleted.withoutEntry(BASE_DATE.withDayOfMonth(8));
        assertTrue(deleted.getBaseline().isPresent());
        assertTrue(deleted.getLatestEntry().isEmpty());
        assertEquals(2, progress.getEntries().size());
    }

    @Test
    void personCopies_preserveBodyAndIdentity() {
        Person original = new PersonBuilder().build();
        BodyProgress progress = new BodyProgress().withBaseline(BASELINE);
        Person updated = original.withBodyProgress(progress);
        assertTrue(original.isSamePerson(updated));
        assertFalse(original.equals(updated));
        assertEquals(updated, new PersonBuilder(updated).build());
        assertEquals(progress, updated.withHealthProfile(null).getBodyProgress());
        assertEquals(progress, updated.withDietaryRestrictions(new DietaryRestrictions()).getBodyProgress());
        assertEquals(progress, updated.withMembershipAssignment(Optional.empty()).getBodyProgress());
        assertEquals(new BodyBaseline(BASE_DATE, new BigDecimal("75.50"), new BigDecimal("175.00")), BASELINE);
        assertEquals(BASELINE.hashCode(), new BodyBaseline(BASE_DATE,
                new BigDecimal("75.50"), new BigDecimal("175.00")).hashCode());
    }

    private static BodyProgressEntry entry(int day, String weight, String notes) {
        return new BodyProgressEntry(BASE_DATE.withDayOfMonth(day), new BigDecimal(weight), Optional.ofNullable(notes));
    }
}
