package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class HealthProfileTest {
    @Test
    public void constructor_invalidValue_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new HealthProfile(""));
        assertThrows(IllegalArgumentException.class, () -> new HealthProfile("  "));
        assertThrows(IllegalArgumentException.class, () -> new HealthProfile("!!!"));
        assertThrows(IllegalArgumentException.class, () -> new HealthProfile("Knee\ninjury"));
        assertThrows(IllegalArgumentException.class, () -> new HealthProfile("a".repeat(501)));
    }

    @Test
    public void constructor_validValue_trimsAndPreservesText() {
        HealthProfile profile = new HealthProfile("  Avoid jumping after ankle sprain  ");
        assertEquals("Avoid jumping after ankle sprain", profile.value);
        assertTrue(HealthProfile.isValidHealthProfile("a".repeat(500)));
        assertFalse(HealthProfile.isValidHealthProfile("a".repeat(501)));
    }
}
