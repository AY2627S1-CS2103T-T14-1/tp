package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_REPS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SETS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_WEIGHT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EXERCISE_DAY;

import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Exercise;

/**
 * Parses input arguments and creates a new AddExerciseCommand object.
 */
public class AddExerciseCommandParser implements Parser<AddExerciseCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddExerciseCommand
     * and returns an AddExerciseCommand object for execution.
     *
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddExerciseCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_SETS, PREFIX_REPS, PREFIX_WEIGHT,
                        PREFIX_EXERCISE_DAY);

        try {
            Index index = ParserUtil.parseIndex(argMultimap.getPreamble());
            if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_SETS, PREFIX_REPS, PREFIX_WEIGHT)) {
                throw new ParseException(
                        String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddExerciseCommand.MESSAGE_USAGE));
            }
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_SETS, PREFIX_REPS, PREFIX_WEIGHT,
                    PREFIX_EXERCISE_DAY);
            String name = ParserUtil.parseExerciseName(argMultimap.getValue(PREFIX_NAME).get());
            int sets = ParserUtil.parseExerciseSets(argMultimap.getValue(PREFIX_SETS).get());
            int reps = ParserUtil.parseExerciseReps(argMultimap.getValue(PREFIX_REPS).get());
            double weight = ParserUtil.parseExerciseWeight(argMultimap.getValue(PREFIX_WEIGHT).get());
            String day = argMultimap.getValue(PREFIX_EXERCISE_DAY).isPresent()
                    ? DayParserUtil.parseDay(argMultimap.getValue(PREFIX_EXERCISE_DAY).get()) : "";
            return new AddExerciseCommand(index, new Exercise(name, sets, reps, weight, day));
        } catch (ParseException pe) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddExerciseCommand.MESSAGE_USAGE), pe);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
