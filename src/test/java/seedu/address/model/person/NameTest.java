package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class NameTest {

    @Test
    public void constructor_documentedCharacters_preservesName() {
        for (String name : List.of("Anne-Marie Tan", "D'Angelo Lim", "Dr. Ahmad Rahman", "Jos\u00e9 Tan",
                "\u674e\u5c0f\u9f99", "\uD801\uDC00 Tan", "'Ali", ".Anne", "-Anne", "Anne-Marie  Tan ")) {
            assertTrue(Name.isValidName(name));
            assertEquals(name, new Name(name).fullName);
        }
    }

    @Test
    public void isValidName_punctuationOnlyUnsupportedSymbolsAndControls_rejectsInput() {
        for (String name : List.of("-", "...", "'", " - . '", "Anne@Marie", "Anne/Marie", "Anne_Marie",
                "Anne\tMarie", "Anne\nMarie", "Anne\u0000Marie", "Anne\u200bMarie", "\uD83C\uDFE0", " Anne")) {
            assertFalse(Name.isValidName(name), name);
            assertThrows(IllegalArgumentException.class, Name.MESSAGE_CONSTRAINTS, () -> new Name(name));
        }
    }

    @Test
    public void constructor_existingNumericAndLongNames_remainValid() {
        for (String name : List.of("12345", "peter the 2nd", "A".repeat(200))) {
            assertEquals(name, new Name(name).fullName);
        }
    }

    @Test
    public void equals_punctuatedNames_retainsCaseAndSpacingSensitivity() {
        Name name = new Name("Anne-Marie Tan");
        assertEquals(name, new Name("Anne-Marie Tan"));
        assertEquals(name.hashCode(), new Name("Anne-Marie Tan").hashCode());
        assertFalse(name.equals(new Name("anne-marie tan")));
        assertFalse(name.equals(new Name("Anne-Marie  Tan")));
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Name(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidName = "";
        assertThrows(IllegalArgumentException.class, () -> new Name(invalidName));
    }

    @Test
    public void isValidName() {
        // null name
        assertThrows(NullPointerException.class, () -> Name.isValidName(null));

        // invalid name
        assertFalse(Name.isValidName("")); // empty string
        assertFalse(Name.isValidName(" ")); // spaces only
        assertFalse(Name.isValidName("^")); // only non-alphanumeric characters
        assertFalse(Name.isValidName("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(Name.isValidName("peter jack")); // alphabets only
        assertTrue(Name.isValidName("12345")); // numbers only
        assertTrue(Name.isValidName("peter the 2nd")); // alphanumeric characters
        assertTrue(Name.isValidName("Capital Tan")); // with capital letters
        assertTrue(Name.isValidName("David Roger Jackson Ray Jr 2nd")); // long names
    }

    @Test
    public void equals() {
        Name name = new Name("Valid Name");

        // same values -> returns true
        assertTrue(name.equals(new Name("Valid Name")));

        // same object -> returns true
        assertTrue(name.equals(name));

        // null -> returns false
        assertFalse(name.equals(null));

        // different types -> returns false
        assertFalse(name.equals(5.0f));

        // different values -> returns false
        assertFalse(name.equals(new Name("Other Valid Name")));
    }
}
