package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.storage.JsonAdaptedPaymentRecord.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.PaymentRecord;

public class JsonAdaptedPaymentRecordTest {

    private static final String VALID_AMOUNT = "150.5";
    private static final String VALID_DATE = "2026-01-15";
    private static final String VALID_REMARKS = "  Initial fee  ";

    @Test
    public void jsonRoundTrip_validRecord_preservesValuesAndTwoDecimalScale() throws Exception {
        PaymentRecord source = PaymentRecord.fromText(VALID_AMOUNT, VALID_DATE, Optional.of(VALID_REMARKS));
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(source);

        String json = JsonUtil.toJsonString(adapted);
        assertTrue(json.contains("\"150.50\""), "Stored amount must use two decimal places: " + json);
        assertEquals(source, JsonUtil.fromJsonString(json, JsonAdaptedPaymentRecord.class).toModelType());
    }

    @Test
    public void jsonRoundTrip_recordWithoutRemarks_omitsRemarksField() throws Exception {
        PaymentRecord source = PaymentRecord.fromText("75", "2026-02-01", Optional.empty());
        String json = JsonUtil.toJsonString(new JsonAdaptedPaymentRecord(source));

        assertFalse(json.contains("remarks"), "Absent remarks should not be persisted: " + json);
        PaymentRecord restored = JsonUtil.fromJsonString(json, JsonAdaptedPaymentRecord.class).toModelType();
        assertEquals(source, restored);
        assertEquals(Optional.empty(), restored.getRemarks());
    }

    @Test
    public void toModelType_remarksAreTrimmedOnLoad() throws Exception {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord("20.00", "2026-03-01", VALID_REMARKS);
        assertEquals(Optional.of("Initial fee"), adapted.toModelType().getRemarks());
    }

    @Test
    public void toModelType_missingAmount_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(null, VALID_DATE, null);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "amount"), adapted::toModelType);
    }

    @Test
    public void toModelType_missingDate_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(VALID_AMOUNT, null, null);
        assertThrows(IllegalValueException.class,
                String.format(MISSING_FIELD_MESSAGE_FORMAT, "date"), adapted::toModelType);
    }

    @Test
    public void toModelType_invalidAmount_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord("0", VALID_DATE, null);
        assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS, adapted::toModelType);
    }

    @Test
    public void toModelType_amountWithMoreThanTwoDecimals_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord("10.001", VALID_DATE, null);
        assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS, adapted::toModelType);
    }

    @Test
    public void toModelType_invalidDate_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(VALID_AMOUNT, "2026-02-30", null);
        assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_DATE_CONSTRAINTS, adapted::toModelType);
    }

    @Test
    public void toModelType_invalidRemarks_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord blank = new JsonAdaptedPaymentRecord(VALID_AMOUNT, VALID_DATE, "   ");
        assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS, blank::toModelType);

        JsonAdaptedPaymentRecord multiline = new JsonAdaptedPaymentRecord(VALID_AMOUNT, VALID_DATE, "fee\nnote");
        assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS,
                multiline::toModelType);
    }

    @Test
    public void jsonRoundTrip_leapDayAndLargeAmount_preservesTwoDecimalText() throws Exception {
        PaymentRecord source = PaymentRecord.fromText(
                "123456789012345678901234567890.12", "2024-02-29", Optional.empty());

        String json = JsonUtil.toJsonString(new JsonAdaptedPaymentRecord(source));

        assertTrue(json.contains("\"123456789012345678901234567890.12\""), json);
        assertTrue(json.contains("\"2024-02-29\""), json);
        assertEquals(source, JsonUtil.fromJsonString(json, JsonAdaptedPaymentRecord.class).toModelType());
    }

    @Test
    public void jsonRoundTrip_absentRemarksField_loadsEmptyRemarks() throws Exception {
        String json = "{\"amount\":\"20.00\",\"date\":\"2024-02-29\"}";

        PaymentRecord restored = JsonUtil.fromJsonString(json, JsonAdaptedPaymentRecord.class).toModelType();

        assertEquals(Optional.empty(), restored.getRemarks());
        assertEquals(LocalDate.of(2024, 2, 29), restored.getDate());
    }

    @Test
    public void toModelType_whitespaceOnlyRemarks_throwsIllegalValueException() {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(VALID_AMOUNT, VALID_DATE, "   ");
        assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS,
                adapted::toModelType);
    }

    @Test
    public void toModelType_controlCharacterRemarks_throwsIllegalValueException() {
        for (String remarks : new String[] {"fee\u0000", "fee\u2028", "fee\u2029"}) {
            JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(VALID_AMOUNT, VALID_DATE, remarks);
            assertThrows(IllegalValueException.class, PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS,
                    adapted::toModelType);
        }
    }

    @Test
    public void toModelType_validDate_keepsIsoDate() throws Exception {
        JsonAdaptedPaymentRecord adapted = new JsonAdaptedPaymentRecord(VALID_AMOUNT, VALID_DATE, null);
        assertEquals(LocalDate.of(2026, 1, 15), adapted.toModelType().getDate());
    }
}
