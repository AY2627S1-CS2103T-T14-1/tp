package seedu.address.logic.parser;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Weekday;

/** Shared parsing rules for exercise weekday values. */
public final class DayParserUtil {
    private DayParserUtil() {
    }

    /** Resolves a weekday name or prefix to its canonical weekday name. */
    public static String parseDay(String input) throws ParseException {
        return Weekday.fromPrefix(input).map(Weekday::getDisplayName)
                .orElseThrow(() -> new ParseException("Invalid day"));
    }
}
