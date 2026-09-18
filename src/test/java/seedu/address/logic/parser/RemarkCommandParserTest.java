package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.RemarkCommand;
import seedu.address.model.person.Remark;

/**
 * Tests remark parsing and usage feedback for invalid indices.
 */
public class RemarkCommandParserTest {

    private final RemarkCommandParser parser = new RemarkCommandParser();

    @Test
    public void parse_remarkText_success() {
        assertParseSuccess(parser, "1 r/Likes to swim.",
                new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim.")));
    }

    @Test
    public void parse_emptyOrMissingRemark_clearsRemark() {
        RemarkCommand expectedCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(""));
        assertParseSuccess(parser, "1 r/", expectedCommand);
        assertParseSuccess(parser, "1", expectedCommand);
    }

    @Test
    public void parse_invalidIndex_showsUsage() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemarkCommand.MESSAGE_USAGE);
        for (String input : new String[] {"", "r/Hello", "a r/Hello", "0 r/Hello", "-1 r/Hello"}) {
            assertParseFailure(parser, input, expectedMessage);
        }
    }

    @Test
    public void parseCommand_remark_routesToRemarkParser() throws Exception {
        assertEquals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark("Likes to swim.")),
                new AddressBookParser().parseCommand("remark 1 r/Likes to swim."));
    }
}
