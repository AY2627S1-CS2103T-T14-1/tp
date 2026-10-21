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
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.TrainingSession.Kind;

/** Parses the common index, date, time and location arguments for an appointment. */
final class AppointmentParser {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm")
            .withResolverStyle(ResolverStyle.STRICT);

    private AppointmentParser() {
    }

    static Appointment parse(String args, String usage, Kind kind) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_DATE, PREFIX_TIME, PREFIX_LOCATION);
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_DATE, PREFIX_TIME, PREFIX_LOCATION);
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        String date = arguments.getValue(PREFIX_DATE).orElse("");
        String time = arguments.getValue(PREFIX_TIME).orElse("");
        String location = arguments.getValue(PREFIX_LOCATION).orElse("");
        try {
            LocalDateTime dateTime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time, TIME_FORMAT));
            if (!dateTime.isAfter(LocalDateTime.now())) {
                String type = kind == Kind.TRAINING ? "Training sessions" : "Meetings";
                throw new ParseException(type + " must be in the future.");
            }
            return new Appointment(index, new TrainingSession(dateTime, location, kind));
        } catch (DateTimeParseException | IllegalArgumentException exception) {
            throw new ParseException("Use a valid date (YYYY-MM-DD), time (HH:mm) and non-empty location.");
        }
    }

    record Appointment(Index index, TrainingSession session) {
    }
}
