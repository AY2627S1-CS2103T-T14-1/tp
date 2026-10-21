package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses an {@code undo} command, which accepts no arguments. */
public class UndoCommandParser implements Parser<UndoCommand> {

    @Override
    public UndoCommand parse(String args) throws ParseException {
        if (!args.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UndoCommand.MESSAGE_USAGE));
        }
        return new UndoCommand();
    }
}
