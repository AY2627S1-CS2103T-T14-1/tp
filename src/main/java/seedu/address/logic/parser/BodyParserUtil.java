package seedu.address.logic.parser;

import java.math.BigInteger;
import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.BodyMeasurements;

/** Parses raw body-command arguments without hiding invalid trailing characters. */
final class BodyParserUtil {
    private static final Pattern PREFIX = Pattern.compile("(?:^|(?<=\\h))(?:([A-Za-z])/|([A-Za-z]{2,})/(?=\\h|$))");
    private static final String INDEX_ERROR =
            "Client index must be a positive whole number shown in the client list.";

    private BodyParserUtil() {
    }

    /** Validates command structure first, then index syntax and field values in callers. */
    static Arguments parse(String raw, Set<String> required, Set<String> optional, String usage) throws ParseException {
        // Preserve raw notes for their more specific printable-text validation.
        if (raw.codePoints().anyMatch(codePoint -> codePoint == '\n' || codePoint == '\r'
                || codePoint == 0x2028 || codePoint == 0x2029)) {
            throw format(usage);
        }
        Matcher matcher = PREFIX.matcher(raw);
        Map<String, String> values = new HashMap<>();
        String previous = null;
        int valueStart = 0;
        String preamble = raw;
        while (matcher.find()) {
            if (previous == null) {
                preamble = raw.substring(0, matcher.start());
            } else {
                values.put(previous, raw.substring(valueStart, matcher.start()));
            }
            String prefix = matcher.group(1) == null ? matcher.group(2) : matcher.group(1);
            if ((!required.contains(prefix) && !optional.contains(prefix))
                    || values.containsKey(prefix) || prefix.equals(previous)) {
                throw format(usage);
            }
            previous = prefix;
            valueStart = matcher.end();
        }
        if (previous != null) {
            values.put(previous, raw.substring(valueStart));
        }
        if (!values.keySet().containsAll(required)
                || required.stream().anyMatch(key -> BodyMeasurements.trimHorizontal(values.get(key)).isEmpty())) {
            throw format(usage);
        }
        String indexText = BodyMeasurements.trimHorizontal(preamble);
        if (indexText.isEmpty() || indexText.matches(".*\\h+.*")) {
            throw format(usage);
        }
        return new Arguments(parseIndex(indexText), Map.copyOf(values));
    }

    private static Index parseIndex(String text) throws ParseException {
        if (!text.matches("[0-9]+")) {
            throw new ParseException(INDEX_ERROR);
        }
        BigInteger value = new BigInteger(text);
        if (value.signum() <= 0 || value.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) > 0) {
            throw new ParseException(INDEX_ERROR);
        }
        return Index.fromOneBased(value.intValue());
    }

    static LocalDate date(String value, Clock clock) {
        LocalDate date = BodyMeasurements.parseDate(value);
        BodyMeasurements.requireNotFuture(date, clock);
        return date;
    }

    private static ParseException format(String usage) {
        return new ParseException("Invalid command format. Use: " + usage);
    }

    /** Validated index and untrimmed field values. */
    record Arguments(Index index, Map<String, String> values) {
        String value(String prefix) {
            return values.get(prefix);
        }
    }
}
