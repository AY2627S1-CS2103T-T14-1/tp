package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.HealthCommand;
import seedu.address.model.person.HealthProfile;

public class HealthCommandParserTest {
    private final HealthCommandParser parser = new HealthCommandParser();

    @Test
    public void parse_recordAndClear_success() {
        assertParseSuccess(parser, "1 h/Avoid jumping", new HealthCommand(
                seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON, new HealthProfile("Avoid jumping")));
        assertParseSuccess(parser, "1 h/", new HealthCommand(
                seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON, null));
    }

    @Test
    public void parse_invalidInput_failure() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HealthCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1", usage);
        assertParseFailure(parser, "1 h/Avoid jumping h/No running", usage);
        assertParseFailure(parser, "0 h/Avoid jumping", usage);
        assertParseFailure(parser, "1 h/!!!", HealthProfile.MESSAGE_CONSTRAINTS);
    }
}
