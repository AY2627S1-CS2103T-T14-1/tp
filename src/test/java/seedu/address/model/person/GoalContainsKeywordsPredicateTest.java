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

    @Test
    public void test_greekGoalMatchesEverySigmaForm() {
        // Issue #279: "ΜΑΡΑΘΩΝΙΟΣ" ends with a capital sigma, which lower-cases to the final form at a word end,
        // while a lone query sigma lower-cases to the ordinary form.
        Person client = new PersonBuilder().withGoal("ΜΑΡΑΘΩΝΙΟΣ").build();
        for (String keyword : List.of("Σ", "σ", "ς", "ΙΟΣ", "ιος", "ιοσ", "μαραθωνιος", "ΜΑΡΑΘΩΝΙΟΣ")) {
            assertTrue(new GoalContainsKeywordsPredicate(List.of(keyword)).test(client), keyword);
        }
        assertTrue(new GoalContainsKeywordsPredicate(List.of("ΣΑ")).test(
                new PersonBuilder().withGoal("ΣΑΒΒΑΤΟ run").build()));
        assertFalse(new GoalContainsKeywordsPredicate(List.of("Ω")).test(
                new PersonBuilder().withGoal("ΜΑΡΑΘΟΝ").build()));
    }
}
