package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class DietaryRestrictionsContainsKeywordsPredicateTest {
    @Test
    public void test_matchesAnyRestrictionCaseInsensitively() {
        Person client = new PersonBuilder().withDietaryRestrictions("Peanut allergy", "Halal meals").build();
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("HALAL")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("shellfish", "peanut")).test(client));
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of("shellfish")).test(client));
    }

    @Test
    public void test_greekSigmaQueryMatchesWordFinalSigma() {
        Person client = new PersonBuilder().withDietaryRestrictions("ΧΩΡΙΣ ΛΑΚΤΟΖΗ").build();
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("Σ")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("ΧΩΡΙΣ")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("χωρις")).test(client));
    }
}
