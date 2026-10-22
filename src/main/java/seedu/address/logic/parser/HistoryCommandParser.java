package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a request to show every logged session for one client. */
public class HistoryCommandParser implements Parser<HistoryCommand> {
    @Override
    public HistoryCommand parse(String args) throws ParseException {
        try {
            return new HistoryCommand(ParserUtil.parseIndex(args));
        } catch (ParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HistoryCommand.MESSAGE_USAGE),
                    exception);
        }
    }
}
