package seedu.address.model.membership;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class MembershipNameTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new MembershipName(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new MembershipName(""));
        assertThrows(IllegalArgumentException.class, () -> new MembershipName("   "));
        assertThrows(IllegalArgumentException.class, () -> new MembershipName("Gold@"));
        assertThrows(IllegalArgumentException.class, () -> new MembershipName("..."));
        assertThrows(IllegalArgumentException.class, () -> new MembershipName("   ...   "));
    }

    @Test
    public void normalize_trimsAndCollapsesWhitespace() {
        assertEquals("Gold Plus", MembershipName.normalize("  Gold   Plus  "));
        assertEquals("Gold Plus", MembershipName.normalize("\tGold\nPlus "));
        assertEquals("Gold", MembershipName.normalize("  Gold  "));
    }

    @Test
    public void constructor_normalizesWhitespace() {
        assertEquals("Gold Plus", new MembershipName("  Gold   Plus  ").value);
    }

    @Test
    public void isValidMembershipName_boundaryLengths() {
        assertTrue(MembershipName.isValidMembershipName("A"));
        assertTrue(MembershipName.isValidMembershipName("1"));
        assertTrue(MembershipName.isValidMembershipName("A".repeat(50)));
        assertFalse(MembershipName.isValidMembershipName("A".repeat(51)));
        assertFalse(MembershipName.isValidMembershipName(""));
        assertFalse(MembershipName.isValidMembershipName("   "));
    }

    @Test
    public void isValidMembershipName_allowedCharset() {
        assertTrue(MembershipName.isValidMembershipName("Gold Plus 2"));
        assertTrue(MembershipName.isValidMembershipName("O'Brien"));
        assertTrue(MembershipName.isValidMembershipName("Gold-Plus"));
        assertTrue(MembershipName.isValidMembershipName("Gold.Plus"));
        assertFalse(MembershipName.isValidMembershipName("Gold@Plus"));
        assertFalse(MembershipName.isValidMembershipName("Gold/Plus"));
        assertFalse(MembershipName.isValidMembershipName("Gold_Plus"));
    }

    @Test
    public void isValidMembershipName_requiresAlphanumeric() {
        assertFalse(MembershipName.isValidMembershipName("..."));
        assertFalse(MembershipName.isValidMembershipName(" - ' . "));
        assertTrue(MembershipName.isValidMembershipName(" . A . "));
        assertTrue(MembershipName.isValidMembershipName("9"));
    }

    @Test
    public void toDisplayName_capitalizesFirstLetterOnly() {
        assertEquals("Gold", MembershipName.toDisplayName("gold"));
        assertEquals("Gold plus", MembershipName.toDisplayName("gold plus"));
        assertEquals("Gold Plus", MembershipName.toDisplayName("Gold Plus"));
        assertEquals("GOLD", MembershipName.toDisplayName("GOLD"));
        assertEquals("1-day", MembershipName.toDisplayName("1-day"));
        assertEquals("...", MembershipName.toDisplayName("..."));
        assertEquals("", MembershipName.toDisplayName(""));
    }

    @Test
    public void getKey_localeIndependentCaseFolding() {
        assertEquals("gold", new MembershipName("Gold").getKey());
        assertEquals("gold plus", new MembershipName("GOLD Plus").getKey());
        assertEquals(new MembershipName("Gold").getKey(), new MembershipName("GOLD").getKey());
    }

    @Test
    public void equals_caseInsensitiveDisplayCasePreserved() {
        MembershipName gold = new MembershipName("Gold");
        assertEquals("Gold", gold.value);
        assertEquals(new MembershipName("gold"), gold);
        assertEquals(new MembershipName("GOLD"), gold);
        assertEquals(new MembershipName("  gold  ").hashCode(), gold.hashCode());
        assertFalse(gold.equals(new MembershipName("Silver")));
        assertTrue(gold.equals(gold));
        assertFalse(gold.equals(null));
        assertFalse(gold.equals("Gold"));
    }

    @Test
    public void toString_returnsDisplayValue() {
        assertEquals("Gold Plus", new MembershipName("Gold Plus").toString());
    }
}
