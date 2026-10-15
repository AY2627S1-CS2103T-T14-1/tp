package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_TIME;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.LogCommand;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/**
 * Tests parsing workout sessions and routing the log command.
 */
public class LogCommandParserTest {

    private static final String VALID_EXERCISE = " ex/Back squat|60|3|10";
    private static final String VALID_ARGS = " 1 d/2020-01-15" + VALID_EXERCISE;

    private static final WorkoutLogEntry SQUAT =
            new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
    private static final WorkoutLogEntry BENCH =
            new WorkoutLogEntry("Bench press", new BigDecimal("40.25"), 3, 8);

    private final LogCommandParser parser = new LogCommandParser();

    @Test
    public void parse_withoutTime_success() {
        LogCommand expected = expectedCommand(Optional.empty(), List.of(SQUAT));

        assertParseSuccess(parser, VALID_ARGS, expected);
        assertParseSuccess(parser, VALID_ARGS.trim(), expected);
    }

    @Test
    public void parse_multipleExercisesWithTime_preservesOrder() {
        String input = " 1 d/2020-01-15 t/18:00"
                + VALID_EXERCISE
                + " ex/Bench press|40.25|3|8";

        LogCommand expected = expectedCommand(
                Optional.of(LocalTime.of(18, 0)), List.of(SQUAT, BENCH));

        assertParseSuccess(parser, input, expected);
    }

    @Test
    public void parse_differentPrefixOrder_success() {
        String input = " 1 ex/Bench press|40.25|3|8"
                + " t/18:00 d/2020-01-15"
                + VALID_EXERCISE;

        LogCommand expected = expectedCommand(
                Optional.of(LocalTime.of(18, 0)), List.of(BENCH, SQUAT));

        assertParseSuccess(parser, input, expected);
    }

    @Test
    public void parse_spacesAroundExerciseFields_success() {
        String input = " 1 d/2020-01-15 ex/ Back squat | 60 | 3 | 10 ";

        assertParseSuccess(
                parser, input, expectedCommand(Optional.empty(), List.of(SQUAT)));
    }

    @Test
    public void parse_missingRequiredArguments_failure() {
        String expectedMessage =
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, LogCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "   ", expectedMessage);
        assertParseFailure(parser, " 1" + VALID_EXERCISE, expectedMessage);
        assertParseFailure(parser, " d/2020-01-15" + VALID_EXERCISE, expectedMessage);
    }

    @Test
    public void parse_invalidIndex_failure() {
        for (String index : List.of("0", "-1", "1.5", "abc", "1 2", "2147483648")) {
            assertParseFailure(
                    parser, " " + index + " d/2020-01-15" + VALID_EXERCISE,
                    ParserUtil.MESSAGE_INVALID_INDEX);
        }
    }

    @Test
    public void parse_duplicateDateOrTime_failure() {
        assertParseFailure(
                parser, VALID_ARGS + " d/2020-01-16",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LOG_DATE));

        assertParseFailure(
                parser, VALID_ARGS + " t/18:00 t/19:00",
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_LOG_TIME));
    }

    @Test
    public void parse_invalidDate_failure() {
        for (String date : List.of(
                "", "15-01-2020", "2020-1-15", "2020-02-30", "2019-02-29")) {
            assertParseFailure(
                    parser, " 1 d/" + date + VALID_EXERCISE,
                    LogCommandParser.MESSAGE_INVALID_DATE);
        }
    }

    @Test
    public void parse_futureDate_failure() {
        String futureDate = LocalDate.now().plusDays(2).toString();

        assertParseFailure(
                parser, " 1 d/" + futureDate + VALID_EXERCISE,
                WorkoutSession.MESSAGE_FUTURE_DATE);
    }

    @Test
    public void parse_invalidTime_failure() {
        for (String time : List.of(
                "", "9:00", "24:00", "18:60", "18:00:00", "18:00:01")) {
            assertParseFailure(
                    parser, VALID_ARGS + " t/" + time,
                    WorkoutSession.MESSAGE_INVALID_TIME);
        }
    }

    @Test
    public void parse_missingExercises_failure() {
        assertParseFailure(
                parser, " 1 d/2020-01-15",
                WorkoutSession.MESSAGE_MISSING_EXERCISES);
    }

    @Test
    public void parse_incompleteExercise_failure() {
        for (String entry : List.of(
                "",
                "Back squat|60|3",
                "Back squat|60|3|10|extra",
                "|60|3|10",
                "Back squat||3|10",
                "Back squat|60||10",
                "Back squat|60|3|")) {
            assertParseFailure(
                    parser, " 1 d/2020-01-15 ex/" + entry,
                    LogCommandParser.MESSAGE_INVALID_EXERCISE_FORMAT);
        }
    }

    @Test
    public void parse_invalidExerciseName_failure() {
        for (String name : List.of("123", "Squat!", "A".repeat(51))) {
            assertParseFailure(
                    parser, " 1 d/2020-01-15 ex/" + name + "|60|3|10",
                    WorkoutLogEntry.MESSAGE_CONSTRAINTS_NAME);
        }
    }

    @Test
    public void parse_invalidWeight_failure() {
        for (String weight : List.of("-1", "1000.01", "40.123", "NaN", "Infinity", "abc")) {
            assertParseFailure(
                    parser, " 1 d/2020-01-15 ex/Back squat|" + weight + "|3|10",
                    WorkoutLogEntry.MESSAGE_CONSTRAINTS_WEIGHT);
        }
    }

    @Test
    public void parse_invalidSets_failure() {
        for (String sets : List.of("0", "-1", "101", "1.5", "abc", "2147483648")) {
            assertParseFailure(
                    parser, " 1 d/2020-01-15 ex/Back squat|60|" + sets + "|10",
                    WorkoutLogEntry.MESSAGE_CONSTRAINTS_SETS);
        }
    }

    @Test
    public void parse_invalidReps_failure() {
        for (String reps : List.of("0", "-1", "1001", "1.5", "abc", "2147483648")) {
            assertParseFailure(
                    parser, " 1 d/2020-01-15 ex/Back squat|60|3|" + reps,
                    WorkoutLogEntry.MESSAGE_CONSTRAINTS_REPS);
        }
    }

    @Test
    public void parse_invalidSecondExercise_rejectsWholeCommand() {
        assertParseFailure(
                parser, VALID_ARGS + " ex/Bench press|-1|3|8",
                WorkoutLogEntry.MESSAGE_CONSTRAINTS_WEIGHT);
    }

    @Test
    public void parse_thirtyExercises_success() {
        String input = " 1 d/2020-01-15" + VALID_EXERCISE.repeat(30);
        LogCommand expected = expectedCommand(
                Optional.empty(), Collections.nCopies(30, SQUAT));

        assertParseSuccess(parser, input, expected);
    }

    @Test
    public void parse_thirtyOneExercises_failure() {
        String input = " 1 d/2020-01-15" + VALID_EXERCISE.repeat(31);

        assertParseFailure(parser, input, WorkoutSession.MESSAGE_TOO_MANY_EXERCISES);
    }

    @Test
    public void parseCommand_log_routesToLogParser() throws Exception {
        LogCommand expected = expectedCommand(Optional.empty(), List.of(SQUAT));

        assertEquals(expected, new AddressBookParser().parseCommand("log" + VALID_ARGS));
    }

    private LogCommand expectedCommand(
            Optional<LocalTime> time, List<WorkoutLogEntry> entries) {
        WorkoutSession session = new WorkoutSession(
                LocalDate.of(2020, 1, 15), time, entries);

        return new LogCommand(Index.fromOneBased(1), session);
    }
}
