package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class GoalTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Goal(null));
    }

    @Test
    public void constructor_invalidGoal_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new Goal(""));
        assertThrows(IllegalArgumentException.class, () -> new Goal("   "));
        assertThrows(IllegalArgumentException.class, () -> new Goal("!!!"));
        assertThrows(IllegalArgumentException.class, () -> new Goal("Fat\nloss"));
        assertThrows(IllegalArgumentException.class, () -> new Goal("a".repeat(51)));
    }

    @Test
    public void constructor_validGoal_success() {
        new Goal("Fat loss");
        new Goal("5");
        new Goal("Run 5km in < 25 min");
        new Goal("Muscle gain (upper body)");
        new Goal("a".repeat(50));
        new Goal("  Fat loss  ");
    }

    @Test
    public void constructor_validGoal_trimsValue() {
        assertEquals("Fat loss", new Goal("  Fat loss ").value);
    }

    @Test
    public void equals() {
        Goal goal = new Goal("Fat loss");

        assertTrue(goal.equals(goal));
        assertTrue(goal.equals(new Goal("Fat loss")));
        assertFalse(goal.equals(new Goal("Muscle gain")));
        assertFalse(goal.equals(null));
        assertFalse(goal.equals("Fat loss"));
    }
}
