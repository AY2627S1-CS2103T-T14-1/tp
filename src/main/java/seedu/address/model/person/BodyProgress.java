package seedu.address.model.person;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import seedu.address.commons.util.ToStringBuilder;

/** Immutable client-owned baseline and complete, chronologically sorted progress history. */
public final class BodyProgress {
    public static final String BASELINE_ORDER_ERROR =
            "Baseline date must be earlier than every recorded progress date. No changes were made.";
    public static final String PROGRESS_ORDER_ERROR = "Progress date must be later than the baseline date.";
    private final Optional<BodyBaseline> baseline;
    private final List<BodyProgressEntry> entries;

    /** Creates a validated BodyProgress instance. */
    public BodyProgress() {
        this(Optional.empty(), List.of());
    }

    /** Checks all cross-entry invariants and defensively copies the collection. */
    public BodyProgress(Optional<BodyBaseline> baseline, List<BodyProgressEntry> entries) {
        this.baseline = Objects.requireNonNull(baseline);
        List<BodyProgressEntry> sorted = new ArrayList<>(List.copyOf(entries));
        sorted.sort(Comparator.comparing(BodyProgressEntry::date));
        if (baseline.isEmpty() && !sorted.isEmpty()) {
            throw new IllegalArgumentException("Progress entries require a body baseline.");
        }
        for (int i = 0; i < sorted.size(); i++) {
            if (!sorted.get(i).date().isAfter(baseline.orElseThrow().date())) {
                throw new IllegalArgumentException(BASELINE_ORDER_ERROR);
            }
            if (i > 0 && sorted.get(i).date().equals(sorted.get(i - 1).date())) {
                throw new IllegalArgumentException("Body progress entry dates must be unique.");
            }
        }
        this.entries = List.copyOf(sorted);
    }

    public Optional<BodyBaseline> getBaseline() {
        return baseline;
    }

    public List<BodyProgressEntry> getEntries() {
        return entries;
    }

    public Optional<BodyProgressEntry> getLatestEntry() {
        return entries.isEmpty() ? Optional.empty() : Optional.of(entries.getLast());
    }

    public Optional<BodyProgressEntry> findEntry(LocalDate date) {
        return entries.stream().filter(entry -> entry.date().equals(date)).findFirst();
    }

    /** Replaces the baseline while retaining and validating every entry. */
    public BodyProgress withBaseline(BodyBaseline replacement) {
        return new BodyProgress(Optional.of(replacement), entries);
    }

    /** Replaces the complete same-date entry, or adds a new entry. */
    public BodyProgress withEntry(BodyProgressEntry replacement) {
        Objects.requireNonNull(replacement);
        if (baseline.isEmpty()) {
            throw new IllegalArgumentException("Progress entries require a body baseline.");
        }
        if (!replacement.date().isAfter(baseline.orElseThrow().date())) {
            throw new IllegalArgumentException(PROGRESS_ORDER_ERROR);
        }
        List<BodyProgressEntry> updated = new ArrayList<>(entries);
        updated.removeIf(entry -> entry.date().equals(replacement.date()));
        updated.add(replacement);
        return new BodyProgress(baseline, updated);
    }

    /** Removes only the requested progress date; the baseline is retained. */
    public BodyProgress withoutEntry(LocalDate date) {
        return new BodyProgress(baseline, entries.stream().filter(entry -> !entry.date().equals(date)).toList());
    }

    /** Checks future-date restrictions at the external execution/loading boundary. */
    public void validateDates(Clock clock) {
        baseline.ifPresent(value -> BodyMeasurements.requireNotFuture(value.date(), clock));
        entries.forEach(value -> BodyMeasurements.requireNotFuture(value.date(), clock));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BodyProgress progress
                && baseline.equals(progress.baseline) && entries.equals(progress.entries);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("baseline", baseline).add("entries", entries).toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseline, entries);
    }
}
