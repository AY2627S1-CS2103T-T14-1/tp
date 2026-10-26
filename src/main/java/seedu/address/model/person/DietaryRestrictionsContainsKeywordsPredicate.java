package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Tests whether any current dietary restriction contains any supplied keyword. */
public class DietaryRestrictionsContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final List<Pattern> patterns;

    /** Creates a predicate that compares literal keywords using Unicode case matching. */
    public DietaryRestrictionsContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
        patterns = this.keywords.stream()
                .map(keyword -> Pattern.compile(Pattern.quote(keyword),
                        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                .toList();
    }

    @Override
    public boolean test(Person person) {
        return person.getDietaryRestrictions().getValues().stream()
                .anyMatch(value -> patterns.stream().anyMatch(pattern -> pattern.matcher(value).find()));
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
