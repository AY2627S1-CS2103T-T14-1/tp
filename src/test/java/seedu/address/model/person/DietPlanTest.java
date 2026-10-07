package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;

public class DietPlanTest {
    @Test
    public void constructor_trimsTextAndPreservesInternalFormatting() {
        DietPlan plan = new DietPlan("  1800 kcal/day,  120 g protein  ", "  Halal meals; avoid shellfish.  ");
        assertEquals("1800 kcal/day,  120 g protein", plan.getTarget());
        assertEquals(Optional.of("Halal meals; avoid shellfish."), plan.getNotes());
        assertTrue(new DietPlan("2000 kcal per day").getNotes().isEmpty());
    }

    @Test
    public void constructor_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new DietPlan(null));
        assertThrows(NullPointerException.class, () -> new DietPlan("1800 kcal", (String) null));
        assertThrows(NullPointerException.class, () -> DietPlan.isValidTarget(null));
        assertThrows(NullPointerException.class, () -> DietPlan.isValidNotes(null));
    }

    @Test
    public void constructor_invalidText_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, DietPlan.MESSAGE_CONSTRAINTS_TARGET, () -> new DietPlan(" "));
        assertThrows(IllegalArgumentException.class, DietPlan.MESSAGE_CONSTRAINTS_TARGET, () -> new DietPlan("!!!"));
        assertThrows(IllegalArgumentException.class, DietPlan.MESSAGE_CONSTRAINTS_NOTES, () ->
                new DietPlan("1800 kcal", " "));
        assertThrows(IllegalArgumentException.class, () -> new DietPlan("1800 kcal\n"));
        assertThrows(IllegalArgumentException.class, () -> new DietPlan("1800 kcal", "\nAvoid shellfish"));
    }

    @Test
    public void validation_checksNormalizedLengthBoundaries() {
        assertTrue(DietPlan.isValidTarget("  a  "));
        assertTrue(DietPlan.isValidTarget("a".repeat(200)));
        assertTrue(DietPlan.isValidTarget("  " + "a".repeat(200) + "  "));
        assertFalse(DietPlan.isValidTarget("a".repeat(201)));
        assertFalse(DietPlan.isValidTarget(""));
        assertTrue(DietPlan.isValidNotes("!"));
        assertTrue(DietPlan.isValidNotes("x".repeat(300)));
        assertFalse(DietPlan.isValidNotes("x".repeat(301)));
        assertFalse(DietPlan.isValidNotes(""));
        assertFalse(DietPlan.isValidNotes("   "));
    }

    @Test
    public void validation_acceptsUnicodeAndCountsCodePoints() {
        assertTrue(DietPlan.isValidTarget("目标 1800 千卡"));
        assertTrue(DietPlan.isValidTarget("1"));
        assertFalse(DietPlan.isValidTarget("---"));
        assertTrue(DietPlan.isValidNotes("🥗".repeat(300)));
        assertFalse(DietPlan.isValidNotes("🥗".repeat(301)));
    }

    @Test
    public void validation_rejectsNonPrintableCharactersEvenAtEdges() {
        String[] invalidCharacters = {"\n", "\r", "\t", "\u0000", "\u007f", "\u0085",
            "\u2028", "\u2029", "\u200b", "\ud800", "\ue000", "\u0378"};
        for (String invalid : invalidCharacters) {
            assertFalse(DietPlan.isValidTarget(invalid + "1800 kcal"));
            assertFalse(DietPlan.isValidNotes("Avoid shellfish" + invalid));
        }
    }

    @Test
    public void isSameDietPlan_comparesNormalizedTextIgnoringCase() {
        DietPlan plan = new DietPlan("1800 kcal", "Lean protein");
        assertTrue(plan.isSameDietPlan(plan));
        assertTrue(plan.isSameDietPlan(new DietPlan(" 1800 KCAL ", " lean PROTEIN ")));
        assertFalse(plan.isSameDietPlan(new DietPlan("2000 kcal", "Lean protein")));
        assertFalse(plan.isSameDietPlan(new DietPlan("1800 kcal", "More vegetables")));
        assertFalse(plan.isSameDietPlan(new DietPlan("1800 kcal")));
        assertFalse(new DietPlan("1800 kcal").isSameDietPlan(plan));
        assertTrue(new DietPlan("1800 kcal").isSameDietPlan(new DietPlan("1800 KCAL")));
        assertFalse(plan.isSameDietPlan(null));
    }

    @Test
    public void equalsAndHashCode_compareStoredValues() {
        DietPlan plan = new DietPlan("1800 kcal", "Lean protein");
        DietPlan equal = new DietPlan(" 1800 kcal ", " Lean protein ");
        assertEquals(plan, equal);
        assertEquals(plan.hashCode(), equal.hashCode());
        assertTrue(plan.equals(plan));
        assertFalse(plan.equals(null));
        assertFalse(plan.equals("1800 kcal"));
        assertFalse(plan.equals(new DietPlan("1800 KCAL", "Lean protein")));
        assertFalse(plan.equals(new DietPlan("1800 kcal")));
        assertFalse(plan.equals(new DietPlan("2000 kcal", "Lean protein")));
        assertFalse(plan.equals(new DietPlan("1800 kcal", "More vegetables")));
        assertEquals(new DietPlan("1800 kcal"), new DietPlan("1800 kcal"));
    }

    @Test
    public void toString_includesStoredText() {
        DietPlan plan = new DietPlan("1800 kcal", "Lean protein");
        assertEquals(DietPlan.class.getCanonicalName()
                + "{target=1800 kcal, notes=Optional[Lean protein]}", plan.toString());
    }
}
