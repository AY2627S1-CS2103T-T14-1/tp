package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class DayParserUtilTest {
    @Test
    public void parseDay_acceptedPrefixes_returnFullWeekday() throws Exception {
        String[][] accepted = {
            {"m", "Monday"}, {"MONDAY", "Monday"},
            {"tu", "Tuesday"}, {"Tues", "Tuesday"},
            {"w", "Wednesday"}, {"WED", "Wednesday"},
            {"th", "Thursday"}, {"Thursday", "Thursday"},
            {"f", "Friday"}, {"FRI", "Friday"},
            {"s", "Saturday"}, {"Sat", "Saturday"},
            {"su", "Sunday"}, {"SUNDAY", "Sunday"}
        };
        for (String[] entry : accepted) {
            assertEquals(entry[1], DayParserUtil.parseDay(entry[0]), entry[0]);
        }
    }

    @Test
    public void parseDay_ambiguousOrInvalidPrefix_rejected() {
        for (String input : new String[] {"", " ", "t", "TuesdayX", "noday"}) {
            assertThrows(ParseException.class, () -> DayParserUtil.parseDay(input), input);
        }
    }
}
