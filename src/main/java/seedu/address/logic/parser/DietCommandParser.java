package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DietCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.DietPlan;

/**
 * Parses diet plans without treating slashes inside words, such as kcal/day, as prefixes.
 * Prefix tokens are recognised only at argument boundaries.
 */
public class DietCommandParser implements Parser<DietCommand> {
    private static final Pattern PREFIX_PATTERN =
            Pattern.compile("(?:^|[\\p{Zs}\\t]+)(cal|pro|carb|fat|[A-Za-z])/");
    private static final Pattern OUTER_HORIZONTAL_SPACE =
            Pattern.compile("^[\\p{Zs}\\t]+|[\\p{Zs}\\t]+$");

    @Override
    public DietCommand parse(String args) throws ParseException {
        requireNonNull(args);
        List<MatchResult> prefixes = PREFIX_PATTERN.matcher(args).results().toList();
        String preamble = args.substring(0, prefixes.isEmpty() ? args.length() : prefixes.getFirst().start()).strip();
        if (preamble.isEmpty()) {
            boolean legacyPrefix = prefixes.isEmpty()
                    || List.of("t", "n").contains(prefixes.getFirst().group(1));
            throw new ParseException(legacyPrefix ? DietCommand.MESSAGE_INDEX_REQUIRED
                    : DietCommand.MESSAGE_INVALID_FORMAT);
        }
        if (preamble.codePoints().anyMatch(Character::isWhitespace)) {
            throw new ParseException(DietCommand.MESSAGE_INVALID_FORMAT);
        }
        Index index;
        try {
            index = ParserUtil.parseIndex(preamble);
        } catch (ParseException exception) {
            throw new ParseException(DietCommand.MESSAGE_INVALID_INDEX, exception);
        }
        Map<String, String> values = new HashMap<>();
        boolean structured = prefixes.stream().anyMatch(prefix ->
                List.of("cal", "pro", "carb", "fat").contains(prefix.group(1)));
        for (int i = 0; i < prefixes.size(); i++) {
            MatchResult prefix = prefixes.get(i);
            String name = prefix.group(1);
            if (structured ? !List.of("cal", "pro", "carb", "fat", "n").contains(name)
                    : !List.of("t", "n").contains(name)) {
                throw new ParseException(DietCommand.MESSAGE_INVALID_FORMAT);
            }
            if (!structured && values.containsKey(name)) {
                throw new ParseException(DietCommand.MESSAGE_REPEATED_PREFIX);
            }
            int end = i + 1 < prefixes.size() ? prefixes.get(i + 1).start() : args.length();
            values.put(name, OUTER_HORIZONTAL_SPACE.matcher(args.substring(prefix.end(), end))
                    .replaceAll(""));
        }
        if (structured) {
            return DietCommand.structured(index, values);
        }
        if (!values.containsKey("t")) {
            throw new ParseException(DietCommand.MESSAGE_TARGET_REQUIRED);
        }
        String target = values.get("t");
        if (!DietPlan.isValidTarget(target)) {
            throw new ParseException(DietPlan.MESSAGE_CONSTRAINTS_TARGET);
        }
        if (values.containsKey("n") && !DietPlan.isValidNotes(values.get("n"))) {
            throw new ParseException(DietPlan.MESSAGE_CONSTRAINTS_NOTES);
        }
        DietPlan plan = values.containsKey("n") ? new DietPlan(target, values.get("n")) : new DietPlan(target);
        return new DietCommand(index, plan);
    }
}
