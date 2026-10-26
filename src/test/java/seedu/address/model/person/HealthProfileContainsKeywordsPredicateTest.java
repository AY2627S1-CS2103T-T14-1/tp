package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class HealthProfileContainsKeywordsPredicateTest {
    @Test
    public void test_matchesProfileCaseInsensitively() {
        Person client = new PersonBuilder().withHealthProfile("Avoid jumping after ankle sprain").build();
        assertTrue(new HealthProfileContainsKeywordsPredicate(List.of("ANKLE")).test(client));
        assertTrue(new HealthProfileContainsKeywordsPredicate(List.of("shoulder", "jump")).test(client));
        assertFalse(new HealthProfileContainsKeywordsPredicate(List.of("shoulder")).test(client));
        assertFalse(new HealthProfileContainsKeywordsPredicate(List.of("ankle"))
                .test(new PersonBuilder().build()));
    }
    @Test
    public void test_greekSubstringAndUnrelatedKeyword() {
        Person client = new PersonBuilder().withHealthProfile("ΠΟΝΟΣ").build();
        assertTrue(new HealthProfileContainsKeywordsPredicate(List.of("Σ")).test(client));
        assertTrue(new HealthProfileContainsKeywordsPredicate(List.of("σ")).test(client));
        assertTrue(new HealthProfileContainsKeywordsPredicate(List.of("ς")).test(client));
        assertTrue(new HealthProfileContainsKeywordsPredicate(List.of("ΠΟΝΟΣ")).test(client));
        assertFalse(new HealthProfileContainsKeywordsPredicate(List.of("Χ")).test(client));
    }

}
