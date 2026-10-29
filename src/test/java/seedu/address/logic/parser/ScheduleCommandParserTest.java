package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.logic.commands.ViewScheduleCommand;
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
        assertParseFailure(parser, "1 d/2099-03-15 tm/14:30 l/\u00a0\u00a0\u00a0", message);
        assertParseFailure(parser, "1 d/2099-03-15 tm/14:30", message);
        assertParseFailure(parser, "1 d/2000-01-01 tm/14:30 l/Gym", "Training sessions must be in the future.");
    }

    @Test
    public void parse_invalidIndex_reportsUsage() {
        assertParseFailure(parser, "0 d/2099-03-15 tm/14:30 l/Gym",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ScheduleCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_viewSchedule_acceptsIndexAndRejectsInvalidIndex() {
        ViewScheduleCommandParser viewParser = new ViewScheduleCommandParser();
        assertParseSuccess(viewParser, "1", new ViewScheduleCommand(INDEX_FIRST_PERSON));
        assertParseFailure(viewParser, "0",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewScheduleCommand.MESSAGE_USAGE));
    }
}
