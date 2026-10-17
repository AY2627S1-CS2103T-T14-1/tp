package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DayScheduleCommand;
import seedu.address.logic.commands.InactiveCommand;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.TrainingSession.Kind;

public class AdvancedSchedulingCommandParserTest {
    @Test
    public void meeting_validAndInvalidInputs() {
        MeetingCommandParser parser = new MeetingCommandParser();
        assertParseSuccess(parser, "1 d/2099-03-15 tm/14:30 l/Cafe",
                new MeetingCommand(INDEX_FIRST_PERSON,
                        new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Cafe", Kind.MEETING)));
        assertParseFailure(parser, "1 d/2000-01-01 tm/14:30 l/Cafe", "Meetings must be in the future.");
        assertParseFailure(parser, "1 d/2099-03-15 tm/14:30 l/",
                "Use a valid date (YYYY-MM-DD), time (HH:mm) and non-empty location.");
    }

    @Test
    public void daySchedule_validAndInvalidDate() {
        DayScheduleCommandParser parser = new DayScheduleCommandParser();
        assertParseSuccess(parser, " d/2099-03-15", new DayScheduleCommand(LocalDate.of(2099, 3, 15)));
        assertParseFailure(parser, " d/2099-02-30", "Use a valid date (YYYY-MM-DD).");
        assertParseFailure(parser, "", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                DayScheduleCommand.MESSAGE_USAGE));
    }

    @Test
    public void inactive_requiresPositiveWholeDays() {
        InactiveCommandParser parser = new InactiveCommandParser();
        assertParseSuccess(parser, "14", new InactiveCommand(14));
        assertParseFailure(parser, "0", String.format(MESSAGE_INVALID_COMMAND_FORMAT, InactiveCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1.5", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                InactiveCommand.MESSAGE_USAGE));
    }
}
