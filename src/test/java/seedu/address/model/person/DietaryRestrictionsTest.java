package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class DietaryRestrictionsTest {
    @Test
    public void constructor_validValues_preservesOrderAndDisplayText() {
        DietaryRestrictions restrictions = new DietaryRestrictions(List.of("  Peanut allergy ", "Halal meals"));
        assertEquals(List.of("Peanut allergy", "Halal meals"), restrictions.getValues());
    }

    @Test
    public void constructor_invalidValues_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new DietaryRestrictions(List.of()));
        assertThrows(IllegalArgumentException.class, () -> new DietaryRestrictions(List.of("")));
        assertThrows(IllegalArgumentException.class, () -> new DietaryRestrictions(
                List.of("Peanut allergy", " peanut ALLERGY ")));
        assertThrows(IllegalArgumentException.class, () -> new DietaryRestrictions(List.of("a".repeat(101))));
    }

    @Test
    public void constructor_greekSigmaVariants_rejectedAsDuplicates() {
        assertThrows(IllegalArgumentException.class, () -> new DietaryRestrictions(
                List.of("No dairy \u03A3", "NO DAIRY \u03C2")));
        assertThrows(IllegalArgumentException.class, () -> new DietaryRestrictions(
                List.of("no dairy \u03C3", "NO DAIRY \u03C2")));
    }
}
