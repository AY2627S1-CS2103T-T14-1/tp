package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class GoalContainsKeywordsPredicateTest {

    private static final Person STRENGTH_CLIENT = new PersonBuilder().withGoal("Build strength").build();
    private static final Person CLIENT_WITHOUT_GOAL = new PersonBuilder().build();

    @Test
    public void test_goalContainsKeywords_returnsTrue() {
        assertTrue(new GoalContainsKeywordsPredicate(List.of("strength")).test(STRENGTH_CLIENT));
        assertTrue(new GoalContainsKeywordsPredicate(List.of("BUILD")).test(STRENGTH_CLIENT));
        assertTrue(new GoalContainsKeywordsPredicate(List.of("marathon", "build")).test(STRENGTH_CLIENT));
    }

    @Test
    public void test_goalDoesNotContainKeywords_returnsFalse() {
        assertFalse(new GoalContainsKeywordsPredicate(List.of("marathon")).test(STRENGTH_CLIENT));
        assertFalse(new GoalContainsKeywordsPredicate(List.of("strength")).test(CLIENT_WITHOUT_GOAL));
    }

    @Test
    public void equals() {
        GoalContainsKeywordsPredicate firstPredicate = new GoalContainsKeywordsPredicate(List.of("strength"));
        GoalContainsKeywordsPredicate secondPredicate = new GoalContainsKeywordsPredicate(List.of("marathon"));

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new GoalContainsKeywordsPredicate(List.of("strength"))));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(1));
    }

    @Test
    public void hashCodeMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        GoalContainsKeywordsPredicate predicate = new GoalContainsKeywordsPredicate(keywords);

        assertEquals(keywords.hashCode(), predicate.hashCode());
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        GoalContainsKeywordsPredicate predicate = new GoalContainsKeywordsPredicate(keywords);

        String expected = GoalContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }
}
