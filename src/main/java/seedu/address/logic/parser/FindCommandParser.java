package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientMatchesAnyPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    private static final Prefix[] SUPPORTED_PREFIXES = {PREFIX_NAME};
    private static final String MESSAGE_KEYWORD_REQUIRED = "Search keyword is required. Use: find KEYWORD... or "
            + "find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]";
    private static final String MESSAGE_MIXED_PREAMBLE = "Invalid command format. Use: find KEYWORD... or "
            + "find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]";
    private static final String MESSAGE_REPEATED_PREFIX =
            "The n/, p/, e/ and g/ prefixes may each be used only once.";
    private static final String MESSAGE_EMPTY_VALUE = "The %s search value cannot be empty.";

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(MESSAGE_KEYWORD_REQUIRED);
        }

        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(args, SUPPORTED_PREFIXES);
        boolean hasPrefix = hasAnySupportedPrefix(argMultimap);
        if (!hasPrefix) {
            return createFindCommand(trimmedArgs);
        }

        if (!argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(MESSAGE_MIXED_PREAMBLE);
        }

        for (Prefix prefix : SUPPORTED_PREFIXES) {
            if (argMultimap.getAllValues(prefix).size() > 1) {
                throw new ParseException(MESSAGE_REPEATED_PREFIX);
            }
            if (argMultimap.getValue(prefix).isPresent() && argMultimap.getValue(prefix).get().isEmpty()) {
                throw new ParseException(String.format(MESSAGE_EMPTY_VALUE, prefix));
            }
        }

        return createFindCommand(argMultimap.getValue(PREFIX_NAME).orElseThrow());
    }

    private static boolean hasAnySupportedPrefix(ArgumentMultimap argMultimap) {
        for (Prefix prefix : SUPPORTED_PREFIXES) {
            if (!argMultimap.getAllValues(prefix).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static FindCommand createFindCommand(String nameQuery) {
        String[] nameKeywords = nameQuery.split("\\s+");
        List<Predicate<Person>> predicates = new ArrayList<>();
        predicates.add(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
        return new FindCommand(new ClientMatchesAnyPredicate(predicates));
    }

}
