package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_EXPIRY;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AssignMembershipPlanCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.membership.MembershipName;

/**
 * Parses input arguments and creates a new AssignMembershipPlanCommand object.
 * Format: {@code amem MEMBERSHIP_NAME PERSON_INDEX [e/YYYY-MM-DD]}.
 * The last non-prefixed token before the optional {@code e/} is the client index;
 * all preceding text is the plan name. The {@code e/} prefix letter is case-insensitive;
 * any other letter prefix is rejected.
 */
public class AssignMembershipPlanCommandParser implements Parser<AssignMembershipPlanCommand> {

    private final Clock clock;

    /**
     * Creates an AssignMembershipPlanCommandParser using the system clock for expiry checks.
     */
    public AssignMembershipPlanCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /**
     * Creates an AssignMembershipPlanCommandParser using the given clock for expiry checks.
     */
    public AssignMembershipPlanCommandParser(Clock clock) {
        requireNonNull(clock);
        this.clock = clock;
    }

    /**
     * Parses the given {@code String} of arguments in the context of the AssignMembershipPlanCommand
     * and returns an AssignMembershipPlanCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AssignMembershipPlanCommand parse(String args) throws ParseException {
        String normalizedArgs = normalizeExpiryPrefix(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(normalizedArgs, PREFIX_MEMBERSHIP_EXPIRY);
        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_MEMBERSHIP_EXPIRY);

        Optional<String> rawExpiry = argMultimap.getValue(PREFIX_MEMBERSHIP_EXPIRY);
        if (rawExpiry.isPresent() && containsInnerWhitespace(rawExpiry.get())) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AssignMembershipPlanCommand.MESSAGE_USAGE));
        }

        List<String> tokens = splitPreamble(argMultimap.getPreamble());
        if (tokens.size() < 2) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, AssignMembershipPlanCommand.MESSAGE_USAGE));
        }

        Index targetIndex = ParserUtil.parseIndex(tokens.get(tokens.size() - 1));
        MembershipName planName =
                ParserUtil.parseMembershipName(String.join(" ", tokens.subList(0, tokens.size() - 1)));
        Optional<LocalDate> expiryDate = rawExpiry.isPresent()
                ? Optional.of(ParserUtil.parseMembershipExpiry(rawExpiry.get(), clock))
                : Optional.empty();

        return new AssignMembershipPlanCommand(planName, targetIndex, expiryDate);
    }

    /**
     * Folds the {@code e/} prefix marker to lowercase so parsing is case-insensitive.
     *
     * @throws ParseException if any token carries an unknown letter prefix.
     */
    private static String normalizeExpiryPrefix(String args) throws ParseException {
        List<String> tokens = new ArrayList<>();
        for (String token : args.split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            int slashIndex = token.indexOf('/');
            if (slashIndex > 0 && token.substring(0, slashIndex).matches("[A-Za-z]+")) {
                String letters = token.substring(0, slashIndex);
                if (letters.length() != 1 || "eE".indexOf(letters.charAt(0)) == -1) {
                    throw new ParseException(String.format(
                            MESSAGE_INVALID_COMMAND_FORMAT, AssignMembershipPlanCommand.MESSAGE_USAGE));
                }
                token = "e" + token.substring(slashIndex);
            }
            tokens.add(token);
        }
        return " " + String.join(" ", tokens);
    }

    private static List<String> splitPreamble(String preamble) {
        List<String> tokens = new ArrayList<>();
        for (String token : preamble.split("\\s+")) {
            if (!token.isEmpty()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    /**
     * Returns true if the trimmed value holds inner whitespace, meaning extra text
     * was absorbed into a single-token field.
     */
    private static boolean containsInnerWhitespace(String value) {
        return value.matches(".*\\s+.*");
    }
}
