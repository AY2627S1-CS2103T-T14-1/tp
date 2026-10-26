package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_RESTRICTION;

import java.util.List;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DietaryRestrictionsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.DietaryRestrictions;

/** Parses a request to replace or clear a client's dietary restrictions. */
public class DietaryRestrictionsCommandParser implements Parser<DietaryRestrictionsCommand> {
    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|\\s)([A-Za-z])/");

    @Override
    public DietaryRestrictionsCommand parse(String args) throws ParseException {
        if (PREFIX_PATTERN.matcher(args).results().anyMatch(match -> !match.group(1).equals("r"))) {
            throw invalidFormat();
        }
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_RESTRICTION);
        List<String> values = arguments.getAllValues(PREFIX_RESTRICTION);
        if (values.isEmpty()) {
            throw invalidFormat();
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException exception) {
            throw invalidFormat();
        }
        if (values.size() == 1 && values.getFirst().isEmpty()) {
            return new DietaryRestrictionsCommand(index, new DietaryRestrictions());
        }
        if (values.stream().anyMatch(String::isEmpty)) {
            throw invalidFormat();
        }
        try {
            return new DietaryRestrictionsCommand(index, new DietaryRestrictions(values));
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new ParseException(DietaryRestrictions.MESSAGE_CONSTRAINTS);
        }
    }

    private static ParseException invalidFormat() {
        return new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                DietaryRestrictionsCommand.MESSAGE_USAGE));
    }
}
