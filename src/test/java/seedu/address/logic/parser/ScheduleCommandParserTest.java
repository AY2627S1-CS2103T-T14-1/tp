package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.model.person.TrainingSession;

public class ScheduleCommandParserTest {
    private final ScheduleCommandParser parser = new ScheduleCommandParser();

    @Test
    public void parse_validDateTimeAndLocation_success() {
        assertParseSuccess(parser, "1 d/2099-03-15 tm/14:30 l/Main gym",
                new ScheduleCommand(INDEX_FIRST_PERSON,
                        new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym")));
    }

    @Test
    public void parse_invalidDateTimeAndLocation_failure() {
        String message = "Use a valid date (YYYY-MM-DD), time (HH:mm) and non-empty location.";
        assertParseFailure(parser, "1 d/2099-02-30 tm/14:30 l/Gym", message);
        assertParseFailure(parser, "1 d/2099-03-15 tm/25:30 l/Gym", message);
        assertParseFailure(parser, "1 d/2099-03-15 tm/14:30 l/", message);
        assertParseFailure(parser, "1 d/2099-03-15 tm/14:30", message);
        assertParseFailure(parser, "1 d/2000-01-01 tm/14:30 l/Gym", "Training sessions must be in the future.");
    }
}
