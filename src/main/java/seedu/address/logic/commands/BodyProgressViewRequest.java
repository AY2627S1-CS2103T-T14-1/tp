package seedu.address.logic.commands;

import java.util.Objects;

import seedu.address.model.person.Person;

/** A resolved client and section to reveal only after successful command execution and saving. */
public record BodyProgressViewRequest(Person person, Section section) {
    /** Creates a validated BodyProgressViewRequest instance. */
    public BodyProgressViewRequest {
        Objects.requireNonNull(person);
        Objects.requireNonNull(section);
    }

    /** The body-progress section requested by a query or mutation. */
    public enum Section {
        HISTORY, COMPARISON
    }
}
