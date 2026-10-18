package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_NAME;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import seedu.address.logic.commands.DeleteMembershipPlanCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.membership.MembershipName;

/**
 * Parses the strict {@code delmem n/NAME} form and returns a
 * DeleteMembershipPlanCommand object for execution.
 * The {@code n/} prefix letter is case-insensitive; any other letter prefix is rejected.
 */
public class DeleteMembershipPlanCommandParser implements Parser<DeleteMembershipPlanCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the DeleteMembershipPlanCommand
     * and returns a DeleteMembershipPlanCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public DeleteMembershipPlanCommand parse(String args) throws ParseException {
        String normalizedArgs = normalizeNamePrefix(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_MEMBERSHIP_NAME);

        if (!arePrefixesPresent(argMultimap, PREFIX_MEMBERSHIP_NAME)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteMembershipPlanCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_MEMBERSHIP_NAME);

        MembershipName name =
                ParserUtil.parseMembershipName(argMultimap.getValue(PREFIX_MEMBERSHIP_NAME).get());
        return new DeleteMembershipPlanCommand(name);
    }

    /**
     * Folds the {@code n/} prefix marker to lowercase so parsing is case-insensitive.
     *
     * @throws ParseException if any token carries an unknown letter prefix.
     */
    private static String normalizeNamePrefix(String args) throws ParseException {
        List<String> tokens = new ArrayList<>();
        for (String token : args.split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            int slashIndex = token.indexOf('/');
            if (slashIndex > 0 && token.substring(0, slashIndex).matches("[A-Za-z]+")) {
                String letters = token.substring(0, slashIndex);
                if (letters.length() != 1 || "nN".indexOf(letters.charAt(0)) == -1) {
                    throw new ParseException(String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT, DeleteMembershipPlanCommand.MESSAGE_USAGE));
                }
                token = "n" + token.substring(slashIndex);
            }
            tokens.add(token);
        }
        return " " + String.join(" ", tokens);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
