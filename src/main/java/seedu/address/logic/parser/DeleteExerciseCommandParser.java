package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DeleteExerciseCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new DeleteExerciseCommand object.
 */
public class DeleteExerciseCommandParser implements Parser<DeleteExerciseCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteExerciseCommand
     * and returns a DeleteExerciseCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteExerciseCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteExerciseCommand.MESSAGE_USAGE));
        }
        String[] indices = trimmedArgs.split("\\s+");
        if (indices.length != 2) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteExerciseCommand.MESSAGE_USAGE));
        }
        try {
            Index personIndex = ParserUtil.parseIndex(indices[0]);
            Index exerciseIndex = ParserUtil.parseIndex(indices[1]);
            return new DeleteExerciseCommand(personIndex, exerciseIndex);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteExerciseCommand.MESSAGE_USAGE), pe);
        }
    }
}
