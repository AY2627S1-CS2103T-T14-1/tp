package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class MembershipAssignmentTest {

    private static MembershipPlan goldPlan() {
        return new MembershipPlan(new MembershipName("Gold"), new BigDecimal("150"), 30);
    }

    @Test
    public void constructor_fromPlan_copiesKeyNameAndStartingSessions() {
        MembershipAssignment assignment =
                new MembershipAssignment(goldPlan(), Optional.empty());
        assertEquals("gold", assignment.getPlanKey());
        assertEquals("Gold", assignment.getPlanName());
        assertEquals(30, assignment.getRemainingSessions());
        assertTrue(assignment.getExpiryDate().isEmpty());
    }

    @Test
    public void constructor_fromPlan_withExpiry() {
        LocalDate expiry = LocalDate.of(2026, 12, 30);
        MembershipAssignment assignment =
                new MembershipAssignment(goldPlan(), Optional.of(expiry));
        assertEquals(Optional.of(expiry), assignment.getExpiryDate());
    }

    @Test
    public void constructor_pastExpiryAllowed() {
        LocalDate past = LocalDate.of(2020, 1, 1);
        MembershipAssignment assignment =
                new MembershipAssignment(goldPlan(), Optional.of(past));
        assertEquals(Optional.of(past), assignment.getExpiryDate());
    }

    @Test
    public void constructor_normalizesCopiedName() {
        MembershipAssignment assignment = new MembershipAssignment("gold", "  Gold   Plus  ", 10,
                Optional.empty());
        assertEquals("Gold Plus", assignment.getPlanName());
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () ->
                new MembershipAssignment(null, "Gold", 10, Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new MembershipAssignment("gold", null, 10, Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new MembershipAssignment("gold", "Gold", 10, null));
    }

    @Test
    public void constructor_invalidArgs_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipAssignment("  ", "Gold", 10, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipAssignment("gold", "...", 10, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipAssignment("gold", "Gold", -1, Optional.empty()));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipAssignment("gold", "Gold", 1001, Optional.empty()));
    }

    @Test
    public void isValidRemainingSessions_zeroToThousand() {
        assertTrue(MembershipAssignment.isValidRemainingSessions(0));
        assertTrue(MembershipAssignment.isValidRemainingSessions(1000));
        assertFalse(MembershipAssignment.isValidRemainingSessions(-1));
        assertFalse(MembershipAssignment.isValidRemainingSessions(1001));
    }

    @Test
    public void withRemainingSessions_retainsKeyNameExpiry() {
        LocalDate expiry = LocalDate.of(2026, 12, 30);
        MembershipAssignment original = new MembershipAssignment(goldPlan(), Optional.of(expiry));
        MembershipAssignment updated = original.withRemainingSessions(5);
        assertEquals("gold", updated.getPlanKey());
        assertEquals("Gold", updated.getPlanName());
        assertEquals(5, updated.getRemainingSessions());
        assertEquals(Optional.of(expiry), updated.getExpiryDate());
        assertThrows(IllegalArgumentException.class, () -> original.withRemainingSessions(-1));
        assertThrows(IllegalArgumentException.class, () -> original.withRemainingSessions(1001));
    }

    @Test
    public void equals_includesAllFields() {
        MembershipAssignment first = new MembershipAssignment(goldPlan(), Optional.empty());
        MembershipAssignment same = new MembershipAssignment("gold", "Gold", 30, Optional.empty());
        assertTrue(first.equals(same));
        assertEquals(first.hashCode(), same.hashCode());
        assertFalse(first.equals(new MembershipAssignment("silver", "Silver", 30, Optional.empty())));
        assertFalse(first.equals(new MembershipAssignment("gold", "Gold", 29, Optional.empty())));
        assertFalse(first.equals(new MembershipAssignment(goldPlan(),
                Optional.of(LocalDate.of(2026, 12, 30)))));
        assertTrue(first.equals(first));
        assertFalse(first.equals(null));
    }

    @Test
    public void toString_withAndWithoutExpiry() {
        assertEquals("Membership plan: Gold; Sessions remaining: 30",
                new MembershipAssignment(goldPlan(), Optional.empty()).toString());
        assertEquals("Membership plan: Gold; Sessions remaining: 30; Expiry: 2026-12-30",
                new MembershipAssignment(goldPlan(),
                        Optional.of(LocalDate.of(2026, 12, 30))).toString());
    }
}
