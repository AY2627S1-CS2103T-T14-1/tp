package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DeleteExerciseCommand;

/**
 * Tests for {@code DeleteExerciseCommandParser}.
 */
public class DeleteExerciseCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteExerciseCommand.MESSAGE_USAGE);

    private DeleteExerciseCommandParser parser = new DeleteExerciseCommandParser();

    @Test
    public void parse_validArgs_returnsDeleteExerciseCommand() {
        DeleteExerciseCommand expectedCommand =
                new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_SECOND_PERSON);

        // standard args
        assertParseSuccess(parser, "1 2", expectedCommand);

        // extra whitespace is accepted
        assertParseSuccess(parser, "  1   2  ", expectedCommand);
    }

    @Test
    public void parse_emptyArgs_throwsParseException() {
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_wrongNumberOfArgs_throwsParseException() {
        // only person index
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);

        // too many indices
        assertParseFailure(parser, "1 2 3", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        // non-numeric indices
        assertParseFailure(parser, "a 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 a", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "a b", MESSAGE_INVALID_FORMAT);

        // zero indices
        assertParseFailure(parser, "0 1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 0", MESSAGE_INVALID_FORMAT);

        // negative indices
        assertParseFailure(parser, "-1 2", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 -2", MESSAGE_INVALID_FORMAT);

        // prefixed values are not indices
        assertParseFailure(parser, "1 n/foo", MESSAGE_INVALID_FORMAT);
    }
}
