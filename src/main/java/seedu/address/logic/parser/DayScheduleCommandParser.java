package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import seedu.address.logic.commands.DayScheduleCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses a date for the daily client schedule. */
public class DayScheduleCommandParser implements Parser<DayScheduleCommand> {
    @Override
    public DayScheduleCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_DATE);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE);
        if (!arguments.getPreamble().isBlank() || arguments.getValue(PREFIX_DATE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, DayScheduleCommand.MESSAGE_USAGE));
        }
        try {
            return new DayScheduleCommand(LocalDate.parse(arguments.getValue(PREFIX_DATE).get()));
        } catch (DateTimeParseException exception) {
            throw new ParseException("Use a valid date (YYYY-MM-DD).");
        }
    }
}
