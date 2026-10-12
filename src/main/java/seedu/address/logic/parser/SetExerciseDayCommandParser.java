package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DAY;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for {@code dayex}. */
public class SetExerciseDayCommandParser implements Parser<SetExerciseDayCommand> {
    @Override
    public SetExerciseDayCommand parse(String args) throws ParseException {
        ArgumentMultimap map = ArgumentTokenizer.tokenize(args, PREFIX_DAY);
        try {
            String[] indices = map.getPreamble().trim().split("\\s+");
            if (indices.length != 2 || !map.getValue(PREFIX_DAY).isPresent()
                    || map.getValue(PREFIX_DAY).get().trim().isEmpty()) {
                throw new ParseException("Invalid arguments");
            }
            map.verifyNoDuplicatePrefixesFor(PREFIX_DAY);
            Index personIndex = ParserUtil.parseIndex(indices[0]);
            Index exerciseIndex = ParserUtil.parseIndex(indices[1]);
            String day = map.getValue(PREFIX_DAY).get().trim();
            if (day.isEmpty() || day.contains(" ")) {
                throw new ParseException("Day must be one word");
            }
            return new SetExerciseDayCommand(personIndex, exerciseIndex, day);
        } catch (ParseException e) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                    SetExerciseDayCommand.MESSAGE_USAGE), e);
        }
    }
}
