package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Represents one immutable payment received from a client.
 * Guarantees: immutable; values are valid as declared by the supplied validation methods.
 */
public final class PaymentRecord {

    public static final String MESSAGE_AMOUNT_CONSTRAINTS =
            "Amount must be greater than zero and have at most two decimal places.";
    public static final String MESSAGE_DATE_CONSTRAINTS =
            "Payment date must be a valid date in YYYY-MM-DD format.";
    public static final String MESSAGE_REMARKS_CONSTRAINTS =
            "Remarks must contain 1 to 300 printable characters without line breaks when r/ is supplied.";
    public static final int AMOUNT_SCALE = 2;
    public static final int MAX_REMARKS_LENGTH = 300;

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d+(\\.\\d{1,2})?");
    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT);

    private final BigDecimal amount;
    private final LocalDate date;
    private final Optional<String> remarks;

    /**
     * Constructs a payment record from already-parsed values.
     * The amount is normalized to two decimal places; remarks are trimmed and Unicode spaces are
     * normalized to regular spaces.
     */
    public PaymentRecord(BigDecimal amount, LocalDate date, Optional<String> remarks) {
        requireNonNull(amount);
        requireNonNull(date);
        requireNonNull(remarks);
        checkArgument(isValidAmount(amount), MESSAGE_AMOUNT_CONSTRAINTS);
        checkArgument(isValidDate(date), MESSAGE_DATE_CONSTRAINTS);
        remarks.ifPresent(value -> checkArgument(isValidRemarks(value), MESSAGE_REMARKS_CONSTRAINTS));

        this.amount = amount.setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
        this.date = date;
        this.remarks = remarks.map(PaymentRecord::canonicalizeRemarks);
    }

    /**
     * Constructs a payment record from raw command or JSON text.
     *
     * @throws IllegalArgumentException if any supplied value is invalid.
     */
    public static PaymentRecord fromText(String amount, String date, Optional<String> remarks) {
        requireNonNull(remarks);
        return new PaymentRecord(parseAmount(amount), parseDate(date),
                remarks.map(PaymentRecord::parseRemarks));
    }

    /** Returns true if {@code test} is a plain positive decimal with at most two fractional digits. */
    public static boolean isValidAmount(String test) {
        if (test == null || !AMOUNT_PATTERN.matcher(test).matches()) {
            return false;
        }
        try {
            return isValidAmount(new BigDecimal(test));
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    /** Returns true if {@code amount} is positive and carries no precision beyond two decimal places. */
    public static boolean isValidAmount(BigDecimal amount) {
        return amount != null
                && amount.signum() > 0
                && amount.stripTrailingZeros().scale() <= AMOUNT_SCALE;
    }

    /** Returns true if {@code test} is exactly YYYY-MM-DD and names a real date in years 0001-9999. */
    public static boolean isValidDate(String test) {
        if (test == null || !DATE_PATTERN.matcher(test).matches()) {
            return false;
        }
        try {
            return isValidDate(LocalDate.parse(test, DATE_FORMATTER));
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    /** Returns true if {@code date} is a real date in years 0001-9999. */
    public static boolean isValidDate(LocalDate date) {
        return date != null && date.getYear() >= 1 && date.getYear() <= 9999;
    }

    /**
     * Returns true if {@code test}, after trimming and normalizing Unicode spaces to regular
     * spaces, has 1 to 300 printable characters. Internal spacing, punctuation and case are
     * preserved; a remark made up only of Unicode spaces (for example non-breaking spaces) is
     * blank and therefore invalid.
     */
    public static boolean isValidRemarks(String test) {
        if (test == null) {
            return false;
        }
        String canonical = canonicalizeRemarks(test);
        int length = canonical.codePointCount(0, canonical.length());
        return length >= 1
                && length <= MAX_REMARKS_LENGTH
                && test.codePoints().allMatch(PaymentRecord::isPrintable);
    }

    /** Parses a plain positive amount and normalizes it to two decimal places. */
    public static BigDecimal parseAmount(String test) {
        checkArgument(isValidAmount(test), MESSAGE_AMOUNT_CONSTRAINTS);
        return new BigDecimal(test).setScale(AMOUNT_SCALE, RoundingMode.UNNECESSARY);
    }

    /** Parses an exact YYYY-MM-DD date in years 0001-9999. */
    public static LocalDate parseDate(String test) {
        checkArgument(isValidDate(test), MESSAGE_DATE_CONSTRAINTS);
        return LocalDate.parse(test, DATE_FORMATTER);
    }

    /**
     * Validates the remarks and returns their canonical form: trimmed, with Unicode space
     * separators normalized to regular spaces.
     */
    public static String parseRemarks(String test) {
        checkArgument(isValidRemarks(test), MESSAGE_REMARKS_CONSTRAINTS);
        return canonicalizeRemarks(test);
    }

    /**
     * Returns the canonical form of the remarks: Unicode space separators (for example
     * non-breaking spaces) are replaced with regular spaces and outer whitespace is trimmed, so
     * pasted text such as {@code fee\u00A0paid} is treated the same as {@code fee paid}.
     */
    private static String canonicalizeRemarks(String remarks) {
        return normalizeSpaces(remarks).strip();
    }

    private static String normalizeSpaces(String remarks) {
        StringBuilder normalized = new StringBuilder(remarks.length());
        remarks.codePoints().forEach(codePoint -> normalized.appendCodePoint(
                Character.getType(codePoint) == Character.SPACE_SEPARATOR ? ' ' : codePoint));
        return normalized.toString();
    }

    private static boolean isPrintable(int codePoint) {
        int type = Character.getType(codePoint);
        return !Character.isISOControl(codePoint)
                && type != Character.FORMAT
                && type != Character.LINE_SEPARATOR
                && type != Character.PARAGRAPH_SEPARATOR
                && type != Character.SURROGATE
                && type != Character.PRIVATE_USE
                && type != Character.UNASSIGNED;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public Optional<String> getRemarks() {
        return remarks;
    }

    /** Returns the amount formatted with exactly two decimal places. */
    public String getFormattedAmount() {
        return amount.toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PaymentRecord otherRecord)) {
            return false;
        }

        return amount.equals(otherRecord.amount)
                && date.equals(otherRecord.date)
                && remarks.equals(otherRecord.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, date, remarks);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("amount", getFormattedAmount())
                .add("date", date)
                .add("remarks", remarks)
                .toString();
    }
}
