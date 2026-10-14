package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class ClientMatchesAnyPredicateTest {

    private final Person alice = new PersonBuilder().withName("Alice").build();

    @Test
    public void test_onePredicate_returnsPredicateResult() {
        ClientMatchesAnyPredicate predicate = new ClientMatchesAnyPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("Alice"))));

        assertTrue(predicate.test(alice));
    }

    @Test
    public void test_multiplePredicates_returnsTrueWhenAnyPredicateMatches() {
        ClientMatchesAnyPredicate predicate = new ClientMatchesAnyPredicate(List.of(
                new NameContainsKeywordsPredicate(List.of("Bob")),
                new NameContainsKeywordsPredicate(List.of("Alice"))));

        assertTrue(predicate.test(alice));
    }

    @Test
    public void test_noMatchingPredicate_returnsFalse() {
        ClientMatchesAnyPredicate predicate = new ClientMatchesAnyPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("Bob"))));

        assertFalse(predicate.test(alice));
    }

    @Test
    public void equals_samePredicates_returnsTrue() {
        Predicate<Person> namePredicate = new NameContainsKeywordsPredicate(List.of("Alice"));
        ClientMatchesAnyPredicate predicate = new ClientMatchesAnyPredicate(List.of(namePredicate));

        assertTrue(predicate.equals(predicate));
        assertTrue(predicate.equals(new ClientMatchesAnyPredicate(List.of(namePredicate))));
        assertFalse(predicate.equals(new ClientMatchesAnyPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("Bob"))))));
    }
}
