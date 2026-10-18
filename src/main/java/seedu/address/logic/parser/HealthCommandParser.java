package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_HEALTH;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.HealthCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.HealthProfile;

/** Parses a request to update a client's health considerations. */
public class HealthCommandParser implements Parser<HealthCommand> {
    @Override
    public HealthCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_HEALTH);
        if (arguments.getAllValues(PREFIX_HEALTH).size() != 1) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HealthCommand.MESSAGE_USAGE));
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(arguments.getPreamble());
        } catch (ParseException exception) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, HealthCommand.MESSAGE_USAGE));
        }
        String value = arguments.getValue(PREFIX_HEALTH).orElseThrow();
        if (value.isEmpty()) {
            return new HealthCommand(index, null);
        }
        try {
            return new HealthCommand(index, new HealthProfile(value));
        } catch (IllegalArgumentException exception) {
            throw new ParseException(HealthProfile.MESSAGE_CONSTRAINTS);
        }
    }
}
