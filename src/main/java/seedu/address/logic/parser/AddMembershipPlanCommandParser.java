package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_SESSIONS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddMembershipPlanCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;

/**
 * Parses input arguments and creates a new AddMembershipPlanCommand object.
 * Prefix letters are case-insensitive: {@code N/}, {@code P/} and {@code S/} are accepted.
 * Any other letter prefix is rejected as an unknown prefix.
 */
public class AddMembershipPlanCommandParser implements Parser<AddMembershipPlanCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddMembershipPlanCommand
     * and returns an AddMembershipPlanCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddMembershipPlanCommand parse(String args) throws ParseException {
        String normalizedArgs = normalizePrefixLetters(args);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(normalizedArgs,
                PREFIX_MEMBERSHIP_NAME, PREFIX_MEMBERSHIP_PRICE, PREFIX_MEMBERSHIP_SESSIONS);

        if (!arePrefixesPresent(argMultimap,
                PREFIX_MEMBERSHIP_NAME, PREFIX_MEMBERSHIP_PRICE, PREFIX_MEMBERSHIP_SESSIONS)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddMembershipPlanCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_MEMBERSHIP_NAME, PREFIX_MEMBERSHIP_PRICE, PREFIX_MEMBERSHIP_SESSIONS);

        String rawPrice = argMultimap.getValue(PREFIX_MEMBERSHIP_PRICE).get();
        String rawSessions = argMultimap.getValue(PREFIX_MEMBERSHIP_SESSIONS).get();
        if (containsInnerWhitespace(rawPrice) || containsInnerWhitespace(rawSessions)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddMembershipPlanCommand.MESSAGE_USAGE));
        }

        MembershipName name = ParserUtil.parseMembershipName(
                argMultimap.getValue(PREFIX_MEMBERSHIP_NAME).get());
        BigDecimal price = ParserUtil.parseMembershipPrice(rawPrice);
        int startingSessions = ParserUtil.parseMembershipSessions(rawSessions);

        return new AddMembershipPlanCommand(new MembershipPlan(name, price, startingSessions));
    }

    /**
     * Folds the single-letter {@code n/}, {@code p/} and {@code s/} prefix markers to lowercase
     * so tokenizing is case-insensitive.
     *
     * @throws ParseException if any token carries an unknown letter prefix.
     */
    private static String normalizePrefixLetters(String args) throws ParseException {
        List<String> tokens = new ArrayList<>();
        for (String token : args.split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            int slashIndex = token.indexOf('/');
            if (slashIndex > 0 && token.substring(0, slashIndex).matches("[A-Za-z]+")) {
                String letters = token.substring(0, slashIndex);
                if (letters.length() != 1 || "npsNPS".indexOf(letters.charAt(0)) == -1) {
                    throw new ParseException(String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT, AddMembershipPlanCommand.MESSAGE_USAGE));
                }
                token = Character.toLowerCase(letters.charAt(0)) + token.substring(slashIndex);
            }
            tokens.add(token);
        }
        return " " + String.join(" ", tokens);
    }

    /**
     * Returns true if the trimmed value holds inner whitespace, meaning extra text
     * was absorbed into a single-token field.
     */
    private static boolean containsInnerWhitespace(String value) {
        return value.matches(".*\\s+.*");
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }
}
