package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.InactiveCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a positive number of calendar days. */
public class InactiveCommandParser implements Parser<InactiveCommand> {
    @Override
    public InactiveCommand parse(String args) throws ParseException {
        String value = args.trim();
        if (!value.matches("[1-9][0-9]*")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, InactiveCommand.MESSAGE_USAGE));
        }
        try {
            return new InactiveCommand(Integer.parseInt(value));
        } catch (NumberFormatException exception) {
            throw new ParseException("Number of days is too large.");
        }
    }
}
