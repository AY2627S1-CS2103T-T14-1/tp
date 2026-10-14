package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TIME;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TrainingSession;

/** Parses a request to add a client's training session. */
public class ScheduleCommandParser implements Parser<ScheduleCommand> {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public ScheduleCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_DATE, PREFIX_TIME, PREFIX_LOCATION);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_TIME, PREFIX_LOCATION);
        String date = arguments.getValue(PREFIX_DATE).orElse("");
        String time = arguments.getValue(PREFIX_TIME).orElse("");
        String location = arguments.getValue(PREFIX_LOCATION).orElse("");
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ScheduleCommand.MESSAGE_USAGE));
        }
        try {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time, TIME_FORMAT));
            if (!dateTime.isAfter(LocalDateTime.now())) {
                throw new ParseException("Training sessions must be in the future.");
            }
            return new ScheduleCommand(index, new TrainingSession(dateTime, location));
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new ParseException("Use a valid date (YYYY-MM-DD), time (HH:mm) and non-empty location.");
        }
    }
}
