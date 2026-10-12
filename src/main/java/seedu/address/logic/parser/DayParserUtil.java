package seedu.address.logic.parser;

import seedu.address.logic.parser.exceptions.ParseException;

/** Shared parsing rules for exercise weekday values. */
public final class DayParserUtil {
    private static final String[] DAYS = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    private DayParserUtil() {
    }

    /** Resolves a weekday name or prefix to its canonical weekday name. */
    public static String parseDay(String input) throws ParseException {
        String candidate = input.trim().toLowerCase();
        for (String day : DAYS) {
            String lowerDay = day.toLowerCase();
            int minimumLength = (day.equals("Tuesday") || day.equals("Thursday")) ? 2 : 1;
            if (candidate.length() >= minimumLength && lowerDay.startsWith(candidate)) {
                return day;
            }
        }
        throw new ParseException("Invalid day");
    }
}
