package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REMARK;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PaymentRecord;

/**
 * Parses input arguments and creates a new {@link PayCommand} object.
 */
public class PayCommandParser implements Parser<PayCommand> {

    private static final String AMOUNT_PREFIX = PREFIX_ADDRESS.getPrefix();
    private static final String DATE_PREFIX = PREFIX_DATE.getPrefix();
    private static final String REMARKS_PREFIX = PREFIX_REMARK.getPrefix();

    @Override
    public PayCommand parse(String args) throws ParseException {
        if (containsLineBreak(args)) {
            throw invalidFormat();
        }

        String trimmedArgs = args.strip();
        if (trimmedArgs.isEmpty()) {
            throw invalidFormat();
        }

        int indexEnd = firstWhitespaceIndex(trimmedArgs);
        String indexText = indexEnd < 0 ? trimmedArgs : trimmedArgs.substring(0, indexEnd);
        String argumentArea = indexEnd < 0 ? "" : trimmedArgs.substring(indexEnd).strip();
        if (indexText.isEmpty() || argumentArea.isEmpty()) {
            throw invalidFormat();
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(indexText);
        } catch (ParseException exception) {
            throw invalidFormat();
        }

        Map<String, String> values = parseValues(argumentArea);
        String amount = values.get(AMOUNT_PREFIX);
        String date = values.get(DATE_PREFIX);
        if (amount == null || date == null) {
            throw invalidFormat();
        }

        try {
            PaymentRecord paymentRecord = PaymentRecord.fromText(amount, date,
                    Optional.ofNullable(values.get(REMARKS_PREFIX)));
            return new PayCommand(index, paymentRecord);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage());
        }
    }

    private static Map<String, String> parseValues(String argumentArea) throws ParseException {
        Map<String, String> values = new LinkedHashMap<>();
        int position = 0;
        while (position < argumentArea.length()) {
            String prefix = recognizedPrefixAt(argumentArea, position);
            if (prefix == null) {
                throw invalidFormat();
            }

            int valueStart = position + prefix.length();
            int nextPrefix = findNextPrefixStart(argumentArea, valueStart);
            String value = argumentArea.substring(valueStart, nextPrefix).strip();
            if (values.containsKey(prefix) || value.isEmpty() || ParserUtil.containsUnknownPrefixToken(value)) {
                throw invalidFormat();
            }
            if (!prefix.equals(REMARKS_PREFIX) && containsWhitespace(value)) {
                throw invalidFormat();
            }
            values.put(prefix, value);

            position = nextPrefix;
            while (position < argumentArea.length() && Character.isWhitespace(argumentArea.charAt(position))) {
                position++;
            }
        }
        return values;
    }

    private static String recognizedPrefixAt(String text, int start) {
        if (text.startsWith(AMOUNT_PREFIX, start)) {
            return AMOUNT_PREFIX;
        }
        if (text.startsWith(DATE_PREFIX, start)) {
            return DATE_PREFIX;
        }
        if (text.startsWith(REMARKS_PREFIX, start)) {
            return REMARKS_PREFIX;
        }
        return null;
    }

    private static int findNextPrefixStart(String text, int fromIndex) {
        for (int i = fromIndex; i < text.length() - 1; i++) {
            if (Character.isWhitespace(text.charAt(i)) && recognizedPrefixAt(text, i + 1) != null) {
                return i + 1;
            }
        }
        return text.length();
    }

    private static boolean containsWhitespace(String value) {
        return value.codePoints().anyMatch(Character::isWhitespace);
    }

    private static boolean containsLineBreak(String text) {
        return text.indexOf('\n') >= 0
                || text.indexOf('\r') >= 0
                || text.indexOf('\u0085') >= 0
                || text.indexOf('\u2028') >= 0
                || text.indexOf('\u2029') >= 0;
    }

    private static int firstWhitespaceIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PayCommand.MESSAGE_USAGE));
    }
}
