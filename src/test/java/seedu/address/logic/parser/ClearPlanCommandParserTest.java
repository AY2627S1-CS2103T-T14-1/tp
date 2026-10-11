package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ClearPlanCommand;

/**
 * Tests for {@code ClearPlanCommandParser}.
 */
public class ClearPlanCommandParserTest {

    private ClearPlanCommandParser parser = new ClearPlanCommandParser();

    @Test
    public void parse_validIndex_returnsClearPlanCommand() {
        assertParseSuccess(parser, "1", new ClearPlanCommand(INDEX_FIRST_PERSON));
        assertParseSuccess(parser, "  1  ", new ClearPlanCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        assertParseFailure(parser, "",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "   ",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_tooManyArgs_throwsParseException() {
        assertParseFailure(parser, "1 2",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "1 n/foo",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "0",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
        assertParseFailure(parser, "-1",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
    }
}
