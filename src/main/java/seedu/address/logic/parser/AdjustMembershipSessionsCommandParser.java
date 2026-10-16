package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AdjustMembershipSessionsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input arguments for the {@code imem} and {@code dmem} commands.
 * Both commands take exactly {@code PERSON_INDEX NUMBER}, where the amount is a
 * whole number from 1 to 1000. Indexes follow the displayed client list.
 */
public class AdjustMembershipSessionsCommandParser implements Parser<AdjustMembershipSessionsCommand> {

    private final boolean isIncrement;

    /**
     * Creates a parser for the increase form when {@code isIncrement} is true,
     * otherwise for the decrease form.
     */
    public AdjustMembershipSessionsCommandParser(boolean isIncrement) {
        this.isIncrement = isIncrement;
    }

    /**
     * Parses the given {@code String} of arguments and returns an
     * AdjustMembershipSessionsCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AdjustMembershipSessionsCommand parse(String args) throws ParseException {
        List<String> tokens = new ArrayList<>();
        for (String token : args.split("\\s+")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        if (tokens.size() != 2 || tokens.stream().anyMatch(token -> token.contains("/"))) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, getUsage()));
        }

        Index targetIndex = ParserUtil.parseIndex(tokens.get(0));
        int amount = ParserUtil.parseMembershipSessions(tokens.get(1));
        return new AdjustMembershipSessionsCommand(targetIndex, amount, isIncrement);
    }

    private String getUsage() {
        return isIncrement
                ? AdjustMembershipSessionsCommand.MESSAGE_USAGE_INCREASE
                : AdjustMembershipSessionsCommand.MESSAGE_USAGE_DECREASE;
    }
}
