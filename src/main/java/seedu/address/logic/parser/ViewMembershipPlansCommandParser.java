package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ViewMembershipPlansCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses the argument of a {@code view} command for the membership plan catalog.
 * Only the case-sensitive aliases {@code mem} and {@code membership} are accepted.
 */
public class ViewMembershipPlansCommandParser implements Parser<ViewMembershipPlansCommand> {

    public static final String MESSAGE_USAGE = ViewMembershipPlansCommand.MESSAGE_USAGE;

    /**
     * Parses the given {@code String} of arguments and returns a ViewMembershipPlansCommand
     * object for execution.
     * @throws ParseException if the argument is not {@code mem} or {@code membership}
     */
    public ViewMembershipPlansCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (!trimmedArgs.equals("mem") && !trimmedArgs.equals("membership")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, MESSAGE_USAGE));
        }
        return new ViewMembershipPlansCommand();
    }
}
