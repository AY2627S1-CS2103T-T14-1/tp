package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_EXERCISE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_TIME;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/**
 * Parses arguments for recording a completed workout session.
 */
public class LogCommandParser implements Parser<LogCommand> {

    public static final String MESSAGE_INVALID_DATE =
            "Session date must be a valid calendar date in yyyy-MM-dd format.";

    public static final String MESSAGE_INVALID_EXERCISE_FORMAT =
            "Each exercise must use ex/NAME|WEIGHT|SETS|REPS, with all four values provided.";

    /**
     * Parses the given arguments into a command containing a validated session.
     *
     * @throws ParseException if any argument is missing or invalid.
     */
    @Override
    public LogCommand parse(String args) throws ParseException {
        requireNonNull(args);

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                " " + args, PREFIX_LOG_DATE, PREFIX_LOG_TIME, PREFIX_LOG_EXERCISE);

        if (argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE));
        }

        Index index = ParserUtil.parseIndex(argMultimap.getPreamble());
        return new LogCommand(index, parseSession(argMultimap, LogCommand.MESSAGE_USAGE));
    }

    /** Parses a replacement or new session using the same workout validation rules. */
    static WorkoutSession parseSession(ArgumentMultimap argMultimap, String usage) throws ParseException {
        if (argMultimap.getValue(PREFIX_LOG_DATE).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, usage));
        }
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_LOG_DATE, PREFIX_LOG_TIME);
        LocalDate date = parseDate(argMultimap.getValue(PREFIX_LOG_DATE).orElseThrow());

        Optional<LocalTime> time = Optional.empty();
        if (argMultimap.getValue(PREFIX_LOG_TIME).isPresent()) {
            time = Optional.of(parseTime(argMultimap.getValue(PREFIX_LOG_TIME).orElseThrow()));
        }

        List<String> exerciseValues = argMultimap.getAllValues(PREFIX_LOG_EXERCISE);
        if (exerciseValues.isEmpty()) {
            throw new ParseException(WorkoutSession.MESSAGE_MISSING_EXERCISES);
        }
        if (exerciseValues.size() > WorkoutSession.MAX_EXERCISE_ENTRIES) {
            throw new ParseException(WorkoutSession.MESSAGE_TOO_MANY_EXERCISES);
        }

        List<WorkoutLogEntry> entries = new ArrayList<>();
        for (String exerciseValue : exerciseValues) {
            entries.add(parseEntry(exerciseValue));
        }

        try {
            WorkoutSession session = new WorkoutSession(date, time, entries);
            return session;
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    private static LocalDate parseDate(String value) throws ParseException {
        if (!value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
            throw new ParseException(MESSAGE_INVALID_DATE);
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw new ParseException(MESSAGE_INVALID_DATE, exception);
        }
    }

    private static LocalTime parseTime(String value) throws ParseException {
        if (!value.matches("([01][0-9]|2[0-3]):[0-5][0-9]")) {
            throw new ParseException(WorkoutSession.MESSAGE_INVALID_TIME);
        }

        return LocalTime.parse(value);
    }

    private static WorkoutLogEntry parseEntry(String value) throws ParseException {
        String[] parts = value.split("\\|", -1);

        if (parts.length != 4
                || Arrays.stream(parts).anyMatch(part -> part.trim().isEmpty())) {
            throw new ParseException(MESSAGE_INVALID_EXERCISE_FORMAT);
        }

        String name = parts[0].trim();
        BigDecimal weight = parseWeight(parts[1].trim());
        int sets = parseInteger(parts[2].trim(), WorkoutLogEntry.MESSAGE_CONSTRAINTS_SETS);
        int reps = parseInteger(parts[3].trim(), WorkoutLogEntry.MESSAGE_CONSTRAINTS_REPS);

        try {
            return new WorkoutLogEntry(name, weight, sets, reps);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }

    private static BigDecimal parseWeight(String value) throws ParseException {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new ParseException(WorkoutLogEntry.MESSAGE_CONSTRAINTS_WEIGHT, exception);
        }
    }

    private static int parseInteger(String value, String errorMessage) throws ParseException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ParseException(errorMessage, exception);
        }
    }
}
