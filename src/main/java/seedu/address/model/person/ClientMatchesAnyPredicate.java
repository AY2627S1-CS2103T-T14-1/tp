package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person} matches at least one of the supplied criteria.
 */
public class ClientMatchesAnyPredicate implements Predicate<Person> {
    private final List<Predicate<Person>> predicates;

    /**
     * Constructs a predicate from the supplied client-matching criteria.
     *
     * @param predicates The predicates to combine with OR.
     */
    public ClientMatchesAnyPredicate(List<Predicate<Person>> predicates) {
        requireNonNull(predicates);
        this.predicates = List.copyOf(predicates);
    }

    @Override
    public boolean test(Person person) {
        return predicates.stream().anyMatch(predicate -> predicate.test(person));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof ClientMatchesAnyPredicate otherPredicate)) {
            return false;
        }

        return predicates.equals(otherPredicate.predicates);
    }

    @Override
    public int hashCode() {
        return predicates.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicates", predicates).toString();
    }
}
