package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClearPlanCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments and creates a new ClearPlanCommand object.
 */
public class ClearPlanCommandParser implements Parser<ClearPlanCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the ClearPlanCommand
     * and returns a ClearPlanCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public ClearPlanCommand parse(String args) throws ParseException {
        requireNonNull(args);
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
        }
        String[] tokens = trimmedArgs.split("\\s+");
        if (tokens.length != 1) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE));
        }
        try {
            Index index = ParserUtil.parseIndex(tokens[0]);
            return new ClearPlanCommand(index);
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClearPlanCommand.MESSAGE_USAGE), pe);
        }
    }
}
