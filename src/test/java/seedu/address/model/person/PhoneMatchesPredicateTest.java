package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class PhoneMatchesPredicateTest {

    @Test
    public void test_matchingPartialPhone_returnsTrue() {
        Person person = new PersonBuilder().withPhone("91234567").build();

        assertTrue(new PhoneMatchesPredicate("9123").test(person));
        assertTrue(new PhoneMatchesPredicate("9123 4567").test(person));
        assertTrue(new PhoneMatchesPredicate("9123-4567").test(person));
    }

    @Test
    public void test_matchingPhoneWithSpaces_returnsTrue() {
        Person person = new PersonBuilder().withPhone("6598765432").build();

        assertTrue(new PhoneMatchesPredicate("659 876").test(person));
    }

    @Test
    public void test_nonMatchingPhone_returnsFalse() {
        Person person = new PersonBuilder().withPhone("91234567").build();

        assertFalse(new PhoneMatchesPredicate("8888").test(person));
    }

    @Test
    public void equals() {
        PhoneMatchesPredicate firstPredicate = new PhoneMatchesPredicate("9123 4567");
        PhoneMatchesPredicate secondPredicate = new PhoneMatchesPredicate("8888");

        assertTrue(firstPredicate.equals(firstPredicate));
        assertTrue(firstPredicate.equals(new PhoneMatchesPredicate("9123-4567")));
        assertFalse(firstPredicate.equals(secondPredicate));
        assertFalse(firstPredicate.equals(null));
        assertFalse(firstPredicate.equals(1));
    }
}
