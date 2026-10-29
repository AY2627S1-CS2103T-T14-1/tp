package seedu.address.model.person;

import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s fitness goal contains any of the keywords given.
 */
public class GoalContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    /**
     * Constructs a predicate using fitness-goal keywords.
     *
     * @param keywords The keywords to match.
     */
    public GoalContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        return person.getGoal()
                .map(goal -> StringUtil.foldCaseForSearch(goal.value))
                .map(goal -> keywords.stream()
                        .anyMatch(keyword -> goal.contains(StringUtil.foldCaseForSearch(keyword))))
                .orElse(false);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof GoalContainsKeywordsPredicate otherGoalContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherGoalContainsKeywordsPredicate.keywords);
    }

    @Override
    public int hashCode() {
        return keywords.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
