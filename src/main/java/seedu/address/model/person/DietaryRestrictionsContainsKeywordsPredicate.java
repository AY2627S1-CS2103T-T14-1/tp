package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/** Tests whether any current dietary restriction contains any supplied keyword. */
public class DietaryRestrictionsContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public DietaryRestrictionsContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        return person.getDietaryRestrictions().getValues().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> keywords.stream()
                        .anyMatch(keyword -> value.contains(keyword.toLowerCase(Locale.ROOT))));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DietaryRestrictionsContainsKeywordsPredicate predicate
                && keywords.equals(predicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }
}
