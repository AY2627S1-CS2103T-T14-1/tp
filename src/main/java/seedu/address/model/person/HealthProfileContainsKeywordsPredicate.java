package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

/** Tests whether a client's health profile contains any supplied keyword. */
public class HealthProfileContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public HealthProfileContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        return person.getHealthProfile()
                .map(profile -> profile.value.toLowerCase(Locale.ROOT))
                .map(profile -> keywords.stream()
                        .anyMatch(keyword -> profile.contains(keyword.toLowerCase(Locale.ROOT))))
                .orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof HealthProfileContainsKeywordsPredicate predicate
                && keywords.equals(predicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }
}
