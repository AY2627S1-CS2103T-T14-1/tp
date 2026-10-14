package seedu.address.logic.parser;

import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.ClientMatchesAnyPredicate;
import seedu.address.model.person.EmailContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.PhoneMatchesPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    private static final Prefix[] SUPPORTED_PREFIXES = {PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL};
    private static final String MESSAGE_KEYWORD_REQUIRED = "Search keyword is required. Use: find KEYWORD... or "
            + "find [n/NAME] [p/PHONE] [e/EMAIL]";
    private static final String MESSAGE_MIXED_PREAMBLE = "Invalid command format. Use: find KEYWORD... or "
            + "find [n/NAME] [p/PHONE] [e/EMAIL]";
    private static final String MESSAGE_REPEATED_PREFIX =
            "The n/, p/ and e/ prefixes may each be used only once.";
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

        return createFindCommand(argMultimap);
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
        List<Predicate<Person>> predicates = new ArrayList<>();
        predicates.add(new NameContainsKeywordsPredicate(List.of(nameQuery.split("\\s+"))));
        return new FindCommand(new ClientMatchesAnyPredicate(predicates));
    }

    private static FindCommand createFindCommand(ArgumentMultimap argMultimap) throws ParseException {
        List<Predicate<Person>> predicates = new ArrayList<>();
        argMultimap.getValue(PREFIX_NAME).ifPresent(value ->
                predicates.add(new NameContainsKeywordsPredicate(List.of(value.split("\\s+")))));
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            String phoneQuery = argMultimap.getValue(PREFIX_PHONE).get();
            if (!phoneQuery.matches(".*\\d.*")) {
                throw new ParseException("Phone search must contain at least one digit.");
            }
            predicates.add(new PhoneMatchesPredicate(phoneQuery));
        }
        argMultimap.getValue(PREFIX_EMAIL).ifPresent(value ->
                predicates.add(new EmailContainsKeywordsPredicate(List.of(value.split("\\s+")))));
        return new FindCommand(new ClientMatchesAnyPredicate(predicates));
    }

}
