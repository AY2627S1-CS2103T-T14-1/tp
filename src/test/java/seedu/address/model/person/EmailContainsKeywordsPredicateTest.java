package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class EmailContainsKeywordsPredicateTest {

    private static final Person LIM = new PersonBuilder().withEmail("lim@example.com").build();

    @Test
    public void test_emailContainsKeywords_returnsTrue() {
        assertTrue(new EmailContainsKeywordsPredicate(List.of("example.com")).test(LIM));
        assertTrue(new EmailContainsKeywordsPredicate(List.of("LIM")).test(LIM));
        assertTrue(new EmailContainsKeywordsPredicate(List.of("zzz", "lim")).test(LIM));
    }

    @Test
    public void test_emailDoesNotContainKeywords_returnsFalse() {
        assertFalse(new EmailContainsKeywordsPredicate(List.of("gmail")).test(LIM));
    }

    @Test
    public void equals() {
        EmailContainsKeywordsPredicate firstPredicate = new EmailContainsKeywordsPredicate(List.of("lim"));
        EmailContainsKeywordsPredicate secondPredicate = new EmailContainsKeywordsPredicate(List.of("example"));

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new EmailContainsKeywordsPredicate(List.of("lim"))));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(1));
    }
}
