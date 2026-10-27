package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GOAL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new EditCommand object
 */
public class EditCommandParser implements Parser<EditCommand> {

    private static final List<Prefix> EDIT_PREFIXES = List.of(
            PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_GOAL, PREFIX_TAG);

    private static final Map<Prefix, String> PREFIX_DESCRIPTIONS = Map.of(
            PREFIX_NAME, "name",
            PREFIX_PHONE, "phone",
            PREFIX_EMAIL, "email",
            PREFIX_ADDRESS, "address",
            PREFIX_GOAL, "goal",
            PREFIX_TAG, "tag");

    private static final int MAX_SUGGESTION_DISTANCE = 1;

    /**
     * Parses the given {@code String} of arguments in the context of the EditCommand
     * and returns an EditCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public EditCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG,
                        PREFIX_GOAL);

        String preamble = argMultimap.getPreamble();
        if (preamble.isEmpty()) {
            throw new ParseException(EditCommand.MESSAGE_INDEX_REQUIRED);
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(preamble);
        } catch (ParseException pe) {
            throw new ParseException(EditCommand.MESSAGE_INVALID_INDEX, pe);
        }

        if (hasRepeatedPrefix(argMultimap)) {
            throw new ParseException(EditCommand.MESSAGE_REPEATED_PREFIX);
        }

        rejectUnknownPrefix(argMultimap);

        EditPersonDescriptor editPersonDescriptor = new EditPersonDescriptor();

        if (argMultimap.getValue(PREFIX_NAME).isPresent()) {
            editPersonDescriptor.setName(ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get()));
        }
        if (argMultimap.getValue(PREFIX_PHONE).isPresent()) {
            editPersonDescriptor.setPhone(ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get()));
        }
        if (argMultimap.getValue(PREFIX_EMAIL).isPresent()) {
            editPersonDescriptor.setEmail(ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get()));
        }
        if (argMultimap.getValue(PREFIX_ADDRESS).isPresent()) {
            editPersonDescriptor.setAddress(ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get()));
        }
        if (argMultimap.getValue(PREFIX_GOAL).isPresent()) {
            String goal = argMultimap.getValue(PREFIX_GOAL).get();
            if (goal.trim().isEmpty()) {
                editPersonDescriptor.clearGoal();
            } else {
                editPersonDescriptor.setGoal(ParserUtil.parseGoal(goal));
            }
        }
        parseTagsForEdit(argMultimap.getAllValues(PREFIX_TAG)).ifPresent(editPersonDescriptor::setTags);

        if (!editPersonDescriptor.isAnyFieldEdited()) {
            throw new ParseException(EditCommand.MESSAGE_NOT_EDITED);
        }

        return new EditCommand(index, editPersonDescriptor);
    }

    private static boolean hasRepeatedPrefix(ArgumentMultimap argMultimap) {
        return argMultimap.getAllValues(PREFIX_NAME).size() > 1
                || argMultimap.getAllValues(PREFIX_PHONE).size() > 1
                || argMultimap.getAllValues(PREFIX_EMAIL).size() > 1
                || argMultimap.getAllValues(PREFIX_ADDRESS).size() > 1
                || argMultimap.getAllValues(PREFIX_GOAL).size() > 1;
    }

    /**
     * Rejects a token that looks like a parameter prefix (that is, {@code <letters>/}) but whose
     * marker is not one of the recognised {@code edit} prefixes.
     *
     * <p>Because the argument tokenizer only recognises the {@code edit} prefixes, such a mistyped
     * token (for example {@code ad/}) is otherwise silently absorbed into the value of the nearest
     * recognised prefix (for example the fitness goal) instead of being reported.
     *
     * @throws ParseException if an unrecognised prefix-shaped token is found inside any recognised
     *                        prefix's value
     */
    private static void rejectUnknownPrefix(ArgumentMultimap argMultimap) throws ParseException {
        for (Prefix prefix : EDIT_PREFIXES) {
            for (String value : argMultimap.getAllValues(prefix)) {
                Prefix unknown = findUnrecognizedPrefixToken(value);
                if (unknown != null) {
                    throw new ParseException(buildUnrecognizedMessage(unknown));
                }
            }
        }
    }

    /**
     * Returns the first token in {@code value} that resembles a parameter prefix yet is not one of
     * the recognised {@code edit} prefixes, or {@code null} if there is none.
     */
    private static Prefix findUnrecognizedPrefixToken(String value) {
        for (String token : value.trim().split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            int slashIndex = token.indexOf('/');
            if (slashIndex <= 0) {
                continue;
            }
            String letters = token.substring(0, slashIndex);
            if (!letters.matches("[A-Za-z]+")) {
                continue;
            }
            Prefix candidate = new Prefix(letters + "/");
            if (!EDIT_PREFIXES.contains(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Builds the message for an unrecognised prefix-shaped token, appending a closest-prefix
     * suggestion when one is close enough to be useful.
     */
    private static String buildUnrecognizedMessage(Prefix unknown) {
        String message = String.format(EditCommand.MESSAGE_UNRECOGNIZED_PREFIX, unknown.getPrefix());
        String suggestion = suggestPrefix(unknown.getPrefix());
        if (suggestion != null) {
            message += " " + String.format(EditCommand.MESSAGE_DID_YOU_MEAN, suggestion);
        }
        return message;
    }

    /**
     * Returns a suggestion for the closest recognised prefix formatted as {@code "n/ (name)"},
     * or {@code null} if no recognised prefix is within {@value #MAX_SUGGESTION_DISTANCE} edits.
     */
    private static String suggestPrefix(String letters) {
        if (letters.endsWith("/")) {
            letters = letters.substring(0, letters.length() - 1);
        }
        Prefix bestPrefix = null;
        int bestDistance = Integer.MAX_VALUE;
        for (Prefix prefix : EDIT_PREFIXES) {
            String marker = prefix.getPrefix();
            marker = marker.substring(0, marker.length() - 1);
            int distance = levenshteinDistance(letters, marker);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPrefix = prefix;
            }
        }
        if (bestPrefix != null && bestDistance <= MAX_SUGGESTION_DISTANCE) {
            return bestPrefix.getPrefix() + " (" + PREFIX_DESCRIPTIONS.get(bestPrefix) + ")";
        }
        return null;
    }

    /**
     * Computes the Levenshtein edit distance between {@code a} and {@code b}.
     */
    private static int levenshteinDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];
        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }
        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }
            int[] temp = previous;
            previous = current;
            current = temp;
        }
        return previous[b.length()];
    }




    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>} if {@code tags} is non-empty.
     * If {@code tags} contains only one element which is an empty string, it will be parsed into a
     * {@code Set<Tag>} containing zero tags.
     */
    private Optional<Set<Tag>> parseTagsForEdit(Collection<String> tags) throws ParseException {
        assert tags != null;

        if (tags.isEmpty()) {
            return Optional.empty();
        }
        Collection<String> tagSet = tags.size() == 1 && tags.contains("") ? Collections.emptySet() : tags;
        return Optional.of(ParserUtil.parseTags(tagSet));
    }

}
