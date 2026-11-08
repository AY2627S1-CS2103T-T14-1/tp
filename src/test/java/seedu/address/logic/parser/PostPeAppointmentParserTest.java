package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.Command;
import seedu.address.logic.parser.exceptions.ParseException;

/** Regressions for appointment field errors and expanded ISO years. */
public class PostPeAppointmentParserTest {
    private final List<Parser<? extends Command>> parsers =
            List.of(new ScheduleCommandParser(), new MeetingCommandParser());

    @Test
    public void invalidFields_reportTheSpecificField() {
        for (Parser<? extends Command> parser : parsers) {
            failure(parser, "1 d/2099-02-30 tm/10:00 l/Gym",
                    "Invalid date: use a real date in YYYY-MM-DD format (year 0001 to 9999).");
            failure(parser, "1 d/2099-03-16 tm/9:00 l/Gym", "Invalid time: use HH:mm in the range 00:00 to 23:59.");
            failure(parser, "1 d/2099-03-16 tm/24:00 l/Gym", "Invalid time: use HH:mm in the range 00:00 to 23:59.");
            failure(parser, "1 d/2099-03-16 tm/10:00 l/", "Location must not be blank.");
            failure(parser, "1 d/2099-03-16 tm/10:00 l/\u00a0", "Location must not be blank.");
        }
    }

    @Test
    public void expandedSignedAndUnpaddedDates_rejected() {
        for (Parser<? extends Command> parser : parsers) {
            for (String date : List.of("+10000-01-01", "0000-01-01", "-0001-01-01",
                    "+2099-01-01", "2099-3-16", "2099-03-6")) {
                failure(parser, "1 d/" + date + " tm/10:00 l/Gym",
                    "Invalid date: use a real date in YYYY-MM-DD format (year 0001 to 9999).");
            }
        }
    }

    @Test
    public void validLeapDayAndUpperYearBoundary_accepted() throws Exception {
        for (Parser<? extends Command> parser : parsers) {
            parser.parse("1 d/2096-02-29 tm/23:59 l/Gym");
            parser.parse("1 d/9999-12-31 tm/00:00 l/Gym");
        }
    }

    private void failure(Parser<? extends Command> parser, String input, String expected) {
        assertEquals(expected, assertThrows(ParseException.class, () -> parser.parse(input)).getMessage());
    }
}
