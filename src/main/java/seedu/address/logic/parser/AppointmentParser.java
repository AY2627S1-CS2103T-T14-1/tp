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
    private static final String MESSAGE_DATE =
            "Invalid date: use a real date in YYYY-MM-DD format (year 0001 to 9999).";
    private static final String MESSAGE_TIME = "Invalid time: use HH:mm in the range 00:00 to 23:59.";
    private static final String MESSAGE_LOCATION = "Location must not be blank.";
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
        LocalDate parsedDate = parseDate(date);
        LocalTime parsedTime;
        try {
            parsedTime = LocalTime.parse(time, TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            throw new ParseException(MESSAGE_TIME);
        }
        if (location.codePoints().allMatch(character -> Character.isWhitespace(character)
                || Character.isSpaceChar(character))) {
            throw new ParseException(MESSAGE_LOCATION);
        }
        LocalDateTime dateTime = LocalDateTime.of(parsedDate, parsedTime);
        if (!dateTime.isAfter(LocalDateTime.now())) {
            String type = kind == Kind.TRAINING ? "Training sessions" : "Meetings";
            throw new ParseException(type + " must be in the future.");
        }
        return new Appointment(index, new TrainingSession(dateTime, location, kind));
    }

    private static LocalDate parseDate(String date) throws ParseException {
        if (!date.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new ParseException(MESSAGE_DATE);
        }
        try {
            LocalDate result = LocalDate.parse(date);
            if (result.getYear() < 1) {
                throw new ParseException(MESSAGE_DATE);
            }
            return result;
        } catch (DateTimeParseException exception) {
            throw new ParseException(MESSAGE_DATE);
        }
    }

    record Appointment(Index index, TrainingSession session) {
    }
}
