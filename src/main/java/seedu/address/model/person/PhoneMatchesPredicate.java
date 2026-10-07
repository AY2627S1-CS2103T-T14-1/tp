package seedu.address.model.person;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Phone} contains the given query.
 */
public class PhoneMatchesPredicate implements Predicate<Person> {
    private final String normalisedQuery;

    /**
     * Constructs a predicate using a normalised phone query.
     *
     * @param query The raw phone query.
     */
    public PhoneMatchesPredicate(String query) {
        normalisedQuery = normalise(query);
    }

    private static String normalise(String value) {
        // TODO: replace with Phone normalisation once 1A is merged.
        return value.replace(" ", "").replace("-", "");
    }

    @Override
    public boolean test(Person person) {
        return normalise(person.getPhone().value).contains(normalisedQuery);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PhoneMatchesPredicate otherPhoneMatchesPredicate)) {
            return false;
        }

        return normalisedQuery.equals(otherPhoneMatchesPredicate.normalisedQuery);
    }

    @Override
    public int hashCode() {
        return normalisedQuery.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("normalisedQuery", normalisedQuery).toString();
    }
}
