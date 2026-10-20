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

        assertEquals(new BigDecimal("150.00"), whole.getAmount());
        assertEquals("150.00", whole.getFormattedAmount());
        assertEquals("150.50", fraction.getFormattedAmount());
        assertEquals("1.23", padded.getFormattedAmount());
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
        for (String remarks : new String[] {"line1\nline2", "line1\rline2", "fee\u0000", "fee\u0007"}) {
            assertFalse(PaymentRecord.isValidRemarks(remarks), remarks);
        }
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
