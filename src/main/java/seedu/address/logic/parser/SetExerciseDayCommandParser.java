package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@code dayex}. */
public class SetExerciseDayCommandParser implements Parser<SetExerciseDayCommand> {
    @Override
    public SetExerciseDayCommand parse(String args) throws ParseException {
        try {
            String[] parts = args.trim().split("\\s+");
            if (parts.length != 3) {
                throw new ParseException("Invalid arguments");
            }
            Index personIndex = ParserUtil.parseIndex(parts[0]);
            Index exerciseIndex = ParserUtil.parseIndex(parts[1]);
            String day = parts[2];
            return new SetExerciseDayCommand(personIndex, exerciseIndex, day);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    SetExerciseDayCommand.MESSAGE_USAGE), e);
        }
    }
}
