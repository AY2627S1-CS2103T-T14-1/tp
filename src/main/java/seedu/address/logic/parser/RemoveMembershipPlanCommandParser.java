package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.RemoveMembershipPlanCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the strict {@code rmem PERSON_INDEX} form and returns a
 * RemoveMembershipPlanCommand object for execution.
 */
public class RemoveMembershipPlanCommandParser implements Parser<RemoveMembershipPlanCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the RemoveMembershipPlanCommand
     * and returns a RemoveMembershipPlanCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public RemoveMembershipPlanCommand parse(String args) throws ParseException {
        List<String> tokens = new ArrayList<>();
        for (String token : args.split("\\s+")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        if (tokens.size() != 1 || tokens.get(0).contains("/")) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, RemoveMembershipPlanCommand.MESSAGE_USAGE));
        }

        Index targetIndex = ParserUtil.parseIndex(tokens.get(0));
        return new RemoveMembershipPlanCommand(targetIndex);
    }
}
