package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ViewScheduleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a request to view a client's schedule. */
public class ViewScheduleCommandParser implements Parser<ViewScheduleCommand> {
    @Override
    public ViewScheduleCommand parse(String args) throws ParseException {
        try {
            return new ViewScheduleCommand(ParserUtil.parseIndex(args));
        } catch (ParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewScheduleCommand.MESSAGE_USAGE));
        }
    }
}
