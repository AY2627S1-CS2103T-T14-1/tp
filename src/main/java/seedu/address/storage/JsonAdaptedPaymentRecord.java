package seedu.address.storage;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.PaymentRecord;

/**
 * Jackson-friendly version of {@link PaymentRecord}.
 */
class JsonAdaptedPaymentRecord {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Payment record's %s field is missing!";

    private final String amount;
    private final String date;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final String remarks;

    /**
     * Constructs a {@code JsonAdaptedPaymentRecord} with the given stored values.
     */
    @JsonCreator
    public JsonAdaptedPaymentRecord(@JsonProperty("amount") String amount,
            @JsonProperty("date") String date,
            @JsonProperty("remarks") String remarks) {
        this.amount = amount;
        this.date = date;
        this.remarks = remarks;
    }

    /**
     * Converts a given {@code PaymentRecord} into this class for Jackson use.
     */
    public JsonAdaptedPaymentRecord(PaymentRecord source) {
        amount = source.getFormattedAmount();
        date = source.getDate().toString();
        remarks = source.getRemarks().orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted payment into the model's {@code PaymentRecord}.
     *
     * @throws IllegalValueException if stored payment data violates a constraint.
     */
    public PaymentRecord toModelType() throws IllegalValueException {
        if (amount == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "amount"));
        }
        if (date == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "date"));
        }
        try {
            return PaymentRecord.fromText(amount, date, Optional.ofNullable(remarks));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
