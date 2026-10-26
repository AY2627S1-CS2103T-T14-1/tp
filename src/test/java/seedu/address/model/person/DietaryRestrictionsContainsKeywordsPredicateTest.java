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
    public void test_greekSigma_matchesExactSubstringAndBothLowercaseForms() {
        Person client = new PersonBuilder().withDietaryRestrictions("ΧΩΡΙΣ ΛΑΚΤΟΖΗ").build();
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("Σ")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("σ")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("ς")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("χωρις")).test(client));
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of("Ψ")).test(client));
    }

    @Test
    public void test_unicodeCase_matchesAccentsAndSupplementaryLetters() {
        Person client = new PersonBuilder().withDietaryRestrictions("CAFÉ", "\uD801\uDC00 food").build();
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("café")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("\uD801\uDC28")).test(client));
    }

    @Test
    public void test_keywords_areLiteralSubstrings() {
        Person client = new PersonBuilder().withDietaryRestrictions("Milk (A+B) [dairy].").build();
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("(a+b)")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of("[DAIRY]")).test(client));
        assertTrue(new DietaryRestrictionsContainsKeywordsPredicate(List.of(".")).test(client));
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of(".*")).test(client));
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of("milk+")).test(client));
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of("unrelated")).test(client));
    }

    @Test
    public void test_emptyRestrictionsOrKeywords_doesNotMatch() {
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of("peanut"))
                .test(new PersonBuilder().build()));
        assertFalse(new DietaryRestrictionsContainsKeywordsPredicate(List.of())
                .test(new PersonBuilder().withDietaryRestrictions("Peanut allergy").build()));
    }
}
