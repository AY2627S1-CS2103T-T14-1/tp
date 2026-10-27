package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

public class PaymentRecordTest {

    private static final LocalDate DATE = LocalDate.of(2026, 10, 20);

    @Test
    public void constructor_normalizesAmountAndFormatsTwoDecimals() {
        PaymentRecord whole = new PaymentRecord(new BigDecimal("150"), DATE, Optional.empty());
        PaymentRecord fraction = new PaymentRecord(new BigDecimal("150.5"), DATE, Optional.empty());
        PaymentRecord padded = new PaymentRecord(new BigDecimal("1.2300"), DATE, Optional.empty());
        PaymentRecord decimal = new PaymentRecord(new BigDecimal("0.1"), DATE, Optional.empty());

        assertEquals(new BigDecimal("150.00"), whole.getAmount());
        assertEquals("150.00", whole.getFormattedAmount());
        assertEquals("150.50", fraction.getFormattedAmount());
        assertEquals("1.23", padded.getFormattedAmount());
        assertEquals("0.10", decimal.getFormattedAmount());
    }

    @Test
    public void parseAmount_normalizesTextAndRejectsInvalidValues() {
        assertEquals(new BigDecimal("150.00"), PaymentRecord.parseAmount("150"));
        assertEquals(new BigDecimal("0.10"), PaymentRecord.parseAmount("0.1"));
        assertEquals(new BigDecimal("123456789012345678901234567890.12"),
                PaymentRecord.parseAmount("123456789012345678901234567890.12"));

        for (String invalid : new String[] {null, "0", "-1", "1.234"}) {
            assertThrows(IllegalArgumentException.class, PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS, () ->
                    PaymentRecord.parseAmount(invalid));
        }
    }

    @Test
    public void constructor_rejectsInvalidAmount() {
        assertThrows(IllegalArgumentException.class, PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS, () ->
                new PaymentRecord(BigDecimal.ZERO, DATE, Optional.empty()));
        assertThrows(IllegalArgumentException.class, PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS, () ->
                new PaymentRecord(new BigDecimal("-1"), DATE, Optional.empty()));
        assertThrows(IllegalArgumentException.class, PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS, () ->
                new PaymentRecord(new BigDecimal("1.234"), DATE, Optional.empty()));
    }

    @Test
    public void constructor_rejectsNullArguments() {
        assertThrows(NullPointerException.class, () ->
                new PaymentRecord(null, DATE, Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new PaymentRecord(BigDecimal.ONE, null, Optional.empty()));
        assertThrows(NullPointerException.class, () ->
                new PaymentRecord(BigDecimal.ONE, DATE, null));
    }

    @Test
    public void constructor_trimsRemarksAndPreservesInternalText() {
        PaymentRecord record = new PaymentRecord(BigDecimal.ONE, DATE, Optional.of("  paid, and/or  "));

        assertEquals(Optional.of("paid, and/or"), record.getRemarks());
    }

    @Test
    public void dateValidation_acceptsLeapDayAndYearBoundaries() {
        assertTrue(PaymentRecord.isValidDate("2024-02-29"));
        assertTrue(PaymentRecord.isValidDate("0001-01-01"));
        assertTrue(PaymentRecord.isValidDate("9999-12-31"));
        assertEquals(LocalDate.of(2024, 2, 29), PaymentRecord.parseDate("2024-02-29"));

        assertFalse(PaymentRecord.isValidDate("2023-02-29"));
        assertFalse(PaymentRecord.isValidDate("0000-01-01"));
        assertFalse(PaymentRecord.isValidDate("10000-01-01"));
        assertThrows(IllegalArgumentException.class, PaymentRecord.MESSAGE_DATE_CONSTRAINTS, () ->
                PaymentRecord.parseDate("2023-02-29"));
    }

    @Test
    public void remarksValidation_acceptsTrimmingAndLengthBoundaries() {
        assertTrue(PaymentRecord.isValidRemarks("  Initial fee  "));
        assertEquals("Initial fee", PaymentRecord.parseRemarks("  Initial fee  "));
        assertTrue(PaymentRecord.isValidRemarks("a".repeat(PaymentRecord.MAX_REMARKS_LENGTH)));

        assertFalse(PaymentRecord.isValidRemarks("   "));
        assertFalse(PaymentRecord.isValidRemarks("a".repeat(PaymentRecord.MAX_REMARKS_LENGTH + 1)));
        assertThrows(IllegalArgumentException.class, PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS, () ->
                PaymentRecord.parseRemarks("   "));
    }

    @Test
    public void remarksValidation_rejectsLineBreaksAndControlCharacters() {
        String[] invalidRemarks = {"line1\nline2", "line1\rline2", "fee\u0000", "fee\u0007",
            "fee\u0085", "fee\u2028", "fee\u2029"};
        for (String remarks : invalidRemarks) {
            assertFalse(PaymentRecord.isValidRemarks(remarks), remarks);
        }
    }

    @Test
    public void remarksValidation_rejectsNonBreakingSpaceOnly() {
        assertFalse(PaymentRecord.isValidRemarks("\u00A0"));
        assertFalse(PaymentRecord.isValidRemarks("   \u00A0   "));
        assertFalse(PaymentRecord.isValidRemarks("fee\u00A0paid"));
        assertTrue(PaymentRecord.isValidRemarks("  Initial fee  "));
        assertTrue(PaymentRecord.isValidRemarks("paid, and/or"));
    }

    @Test
    public void equalsAndHashCode_useAllValues() {
        PaymentRecord record = new PaymentRecord(new BigDecimal("150"), DATE, Optional.of("Initial fee"));
        PaymentRecord sameValues = PaymentRecord.fromText("150.00", "2026-10-20", Optional.of("Initial fee"));
        PaymentRecord differentAmount =
                new PaymentRecord(new BigDecimal("151"), DATE, Optional.of("Initial fee"));
        PaymentRecord differentDate = new PaymentRecord(new BigDecimal("150"),
                LocalDate.of(2026, 10, 21), Optional.of("Initial fee"));
        PaymentRecord differentRemarks =
                new PaymentRecord(new BigDecimal("150"), DATE, Optional.of("Late fee"));

        assertEquals(record, sameValues);
        assertEquals(record.hashCode(), sameValues.hashCode());
        assertNotEquals(record, differentAmount);
        assertNotEquals(record, differentDate);
        assertNotEquals(record, differentRemarks);
        assertEquals(record, record);
        assertNotEquals(record, null);
        assertNotEquals(record, "150.00");
    }

    @Test
    public void toStringMethod() {
        PaymentRecord record = new PaymentRecord(new BigDecimal("150"), DATE, Optional.of("Initial fee"));
        String expected = PaymentRecord.class.getCanonicalName()
                + "{amount=150.00, date=2026-10-20, remarks=Optional[Initial fee]}";

        assertEquals(expected, record.toString());
    }
}
