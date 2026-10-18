package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

public class MembershipPlanTest {

    private static MembershipPlan goldPlan() {
        return new MembershipPlan(new MembershipName("Gold"), new BigDecimal("150"), 30);
    }

    @Test
    public void constructor_null_throwsNullPointerException() {
        MembershipName name = new MembershipName("Gold");
        assertThrows(NullPointerException.class, () -> new MembershipPlan(null,
                new BigDecimal("10"), 10));
        assertThrows(NullPointerException.class, () -> new MembershipPlan(name, null, 10));
    }

    @Test
    public void constructor_invalidPrice_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("0"), 10));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("-5"), 10));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("10.123"), 10));
    }

    @Test
    public void constructor_invalidSessions_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("10"), 0));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("10"), 1001));
        assertThrows(IllegalArgumentException.class, () ->
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("10"), -1));
    }

    @Test
    public void constructor_priceStoredAtScaleTwo() {
        assertEquals(new BigDecimal("150.00"), goldPlan().getPrice());
        assertEquals(new BigDecimal("150.50"),
                new MembershipPlan(new MembershipName("Gold"), new BigDecimal("150.5"), 10).getPrice());
        assertEquals("150.00", goldPlan().getPrice().toPlainString());
    }

    @Test
    public void isValidPrice_positiveAtMostTwoDecimals() {
        assertTrue(MembershipPlan.isValidPrice(new BigDecimal("0.01")));
        assertTrue(MembershipPlan.isValidPrice(new BigDecimal("150")));
        assertTrue(MembershipPlan.isValidPrice(new BigDecimal("150.5")));
        assertTrue(MembershipPlan.isValidPrice(new BigDecimal("150.50")));
        assertFalse(MembershipPlan.isValidPrice(new BigDecimal("0")));
        assertFalse(MembershipPlan.isValidPrice(new BigDecimal("-1")));
        assertFalse(MembershipPlan.isValidPrice(new BigDecimal("10.123")));
    }

    @Test
    public void isValidStartingSessions_oneToThousand() {
        assertTrue(MembershipPlan.isValidStartingSessions(1));
        assertTrue(MembershipPlan.isValidStartingSessions(1000));
        assertFalse(MembershipPlan.isValidStartingSessions(0));
        assertFalse(MembershipPlan.isValidStartingSessions(1001));
    }

    @Test
    public void getKey_delegatesToName() {
        assertEquals("gold", goldPlan().getKey());
    }

    @Test
    public void equals_includesAllFields() {
        assertTrue(goldPlan().equals(goldPlan()));
        assertTrue(goldPlan().equals(new MembershipPlan(new MembershipName("gold"),
                new BigDecimal("150.00"), 30)));
        assertFalse(goldPlan().equals(new MembershipPlan(new MembershipName("Silver"),
                new BigDecimal("150"), 30)));
        assertFalse(goldPlan().equals(new MembershipPlan(new MembershipName("Gold"),
                new BigDecimal("80"), 30)));
        assertFalse(goldPlan().equals(new MembershipPlan(new MembershipName("Gold"),
                new BigDecimal("150"), 12)));
        assertFalse(goldPlan().equals(null));
        assertFalse(goldPlan().equals("Gold"));
        assertEquals(goldPlan().hashCode(),
                new MembershipPlan(new MembershipName("GOLD"), new BigDecimal("150"), 30).hashCode());
    }

    @Test
    public void toString_format() {
        assertEquals("Gold; Price: 150.00; Sessions: 30", goldPlan().toString());
    }
}
