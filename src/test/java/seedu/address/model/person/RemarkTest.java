package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

public class RemarkTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new Remark(null));
    }

    @Test
    public void emptyRemark_isAllowed() {
        assertEquals("", new Remark("").value);
    }

    @Test
    public void equals_sameValue_returnsTrue() {
        assertEquals(new Remark("Likes to swim"), new Remark("Likes to swim"));
        assertEquals(new Remark("Likes to swim").hashCode(), new Remark("Likes to swim").hashCode());
        assertNotEquals(new Remark("Likes to swim"), new Remark("Likes baseball"));
    }
}
