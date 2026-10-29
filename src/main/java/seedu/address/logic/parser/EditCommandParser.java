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
            String goal = argMultimap.getRawValue(PREFIX_GOAL).get();
            if (goal.isEmpty() || goal.chars().allMatch(character -> character == ' ')) {
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
     * Rejects a token that is a near miss of a recognized {@code edit} prefix: a
     * {@code <letters>/} token within {@value #MAX_SUGGESTION_DISTANCE} edits of exactly one
     * recognized prefix (for example {@code ad/} for {@code a/}).
     *
     * <p>Because the argument tokenizer only recognizes the {@code edit} prefixes, such a mistyped
     * token is otherwise silently absorbed into the value of the nearest recognized prefix (for
     * example the fitness goal) instead of being reported. Only unambiguous near misses are
     * rejected, so ordinary text that merely contains a slash (for example {@code c/o},
     * {@code and/or}, {@code km/h} or {@code w/}) is left untouched.
     *
     * @throws ParseException if a near-miss prefix-shaped token is found inside any recognized
     *                        prefix's value
     */
    private static void rejectUnknownPrefix(ArgumentMultimap argMultimap) throws ParseException {
        for (Prefix prefix : EDIT_PREFIXES) {
            for (String value : argMultimap.getAllValues(prefix)) {
                String unknown = findUnrecognizedPrefixToken(value);
                if (unknown != null) {
                    throw new ParseException(buildUnrecognizedMessage(unknown));
                }
            }
        }
    }

    /**
     * Returns the letters of the first token in {@code value} that is a near miss of a recognized
     * {@code edit} prefix, or {@code null} if there is none.
     */
    private static String findUnrecognizedPrefixToken(String value) {
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
            if (EDIT_PREFIXES.contains(new Prefix(letters + "/"))) {
                continue;
            }
            if (uniqueClosestPrefix(letters) != null) {
                return letters;
            }
        }
        return null;
    }

    /**
     * Builds the message for a near-miss prefix-shaped token, naming the intended prefix.
     */
    private static String buildUnrecognizedMessage(String letters) {
        String message = String.format(EditCommand.MESSAGE_UNRECOGNIZED_PREFIX, letters + "/");
        Prefix closest = uniqueClosestPrefix(letters);
        if (closest != null) {
            message += " " + String.format(EditCommand.MESSAGE_DID_YOU_MEAN,
                    closest.getPrefix() + " (" + PREFIX_DESCRIPTIONS.get(closest) + ")");
        }
        return message;
    }

    /**
     * Returns the unique closest recognized prefix to {@code letters} when it is within
     * {@value #MAX_SUGGESTION_DISTANCE} edits and no other recognized prefix ties that distance,
     * or {@code null} otherwise. Requiring a unique winner keeps the suggestion deterministic and
     * avoids flagging tokens (such as a single letter) that sit equally close to several prefixes.
     */
    private static Prefix uniqueClosestPrefix(String letters) {
        Prefix bestPrefix = null;
        int bestDistance = Integer.MAX_VALUE;
        int bestCount = 0;
        for (Prefix prefix : EDIT_PREFIXES) {
            String marker = prefix.getPrefix();
            marker = marker.substring(0, marker.length() - 1);
            int distance = levenshteinDistance(letters, marker);
            if (distance < bestDistance) {
                bestDistance = distance;
                bestPrefix = prefix;
                bestCount = 1;
            } else if (distance == bestDistance) {
                bestCount++;
            }
        }
        return bestDistance <= MAX_SUGGESTION_DISTANCE && bestCount == 1 ? bestPrefix : null;
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
