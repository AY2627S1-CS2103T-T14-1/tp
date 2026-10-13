package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.model.person.Exercise;

/**
 * Tests for {@code AddExerciseCommandParser}.
 */
public class AddExerciseCommandParserTest {

    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddExerciseCommand.MESSAGE_USAGE);

    private AddExerciseCommandParser parser = new AddExerciseCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        Exercise expectedExercise = new Exercise("Bench Press", 3, 10, 60.0, "");
        AddExerciseCommand expectedCommand = new AddExerciseCommand(INDEX_FIRST_PERSON, expectedExercise);

        // standard ordering
        assertParseSuccess(parser, "1 n/Bench Press s/3 r/10 w/60", expectedCommand);

        // with leading/trailing whitespace
        assertParseSuccess(parser, "  1 n/Bench Press s/3 r/10 w/60  ", expectedCommand);

        // different prefix ordering still succeeds
        assertParseSuccess(parser, "1 w/60 r/10 s/3 n/Bench Press", expectedCommand);

        assertParseSuccess(parser, "1 n/Bench Press s/3 r/10 w/60 d/w",
                new AddExerciseCommand(INDEX_FIRST_PERSON, new Exercise("Bench Press", 3, 10, 60.0, "Wednesday")));
    }

    @Test
    public void parse_missingNamePrefix_failure() {
        // missing exercise name prefix
        assertParseFailure(parser, "1 s/3 r/10 w/60", MESSAGE_INVALID_FORMAT);

        // name prefix is required
        assertParseFailure(parser, "1", MESSAGE_INVALID_FORMAT);

        // empty args
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "   ", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_optionalPrefixCombinations_useDefaultsOnlyForOmittedValues() {
        String[] optionalArgs = {" s/4", " r/8", " w/25.5", " d/w"};
        for (int mask = 0; mask < 16; mask++) {
            StringBuilder args = new StringBuilder("1 n/Bench Press");
            for (int field = 0; field < optionalArgs.length; field++) {
                if ((mask & (1 << field)) != 0) {
                    args.append(optionalArgs[field]);
                }
            }
            int sets = (mask & 1) != 0 ? 4 : 1;
            int reps = (mask & 2) != 0 ? 8 : 1;
            double weight = (mask & 4) != 0 ? 25.5 : 0;
            String day = (mask & 8) != 0 ? "Wednesday" : "";
            Exercise expected = new Exercise("Bench Press", sets, reps, weight, day);
            assertParseSuccess(parser, args.toString(), new AddExerciseCommand(INDEX_FIRST_PERSON, expected));
        }
    }

    @Test
    public void parse_presentButEmptyOptionalPrefix_failure() {
        for (String prefix : new String[] {"s/", "r/", "w/", "d/"}) {
            assertParseFailure(parser, "1 n/Bench Press " + prefix, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_invalidPersonIndex_failure() {
        String validExerciseArgs = " n/Bench Press s/3 r/10 w/60";

        // missing index
        assertParseFailure(parser, validExerciseArgs, MESSAGE_INVALID_FORMAT);

        // non-numeric index
        assertParseFailure(parser, "a" + validExerciseArgs, MESSAGE_INVALID_FORMAT);

        // zero index
        assertParseFailure(parser, "0" + validExerciseArgs, MESSAGE_INVALID_FORMAT);

        // negative index
        assertParseFailure(parser, "-1" + validExerciseArgs, MESSAGE_INVALID_FORMAT);

        // extra preamble tokens
        assertParseFailure(parser, "1 2" + validExerciseArgs, MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidExerciseValue_failure() {
        // blank exercise name
        assertParseFailure(parser, "1 n/ s/3 r/10 w/60", MESSAGE_INVALID_FORMAT);

        // invalid sets: zero, negative and non-numeric
        assertParseFailure(parser, "1 n/Bench Press s/0 r/10 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/-1 r/10 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/abc r/10 w/60", MESSAGE_INVALID_FORMAT);

        // invalid reps: zero, negative and non-numeric
        assertParseFailure(parser, "1 n/Bench Press s/3 r/0 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/-5 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/abc w/60", MESSAGE_INVALID_FORMAT);

        // invalid weight: negative and non-numeric
        assertParseFailure(parser, "1 n/Bench Press s/3 r/10 w/-1", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/10 w/abc", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unknownPrefix_failure() {
        for (String prefix : new String[] {"x/", "t/", "ex/", "unknown/", "N/"}) {
            assertParseFailure(parser, "1 n/Stretch " + prefix + "unexpected", MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "1 n/Stretch " + prefix, MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "1 " + prefix + "unexpected n/Stretch", MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "1 s/3 n/Stretch " + prefix + "unexpected d/m", MESSAGE_INVALID_FORMAT);
            assertParseFailure(parser, "1 n/Stretch\t" + prefix + "unexpected", MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_extraTextOutsideName_failure() {
        for (String args : new String[] {"1 extra n/Stretch", "1 n/Stretch s/3 extra",
            "1 n/Stretch r/8 extra", "1 n/Stretch w/25.5 extra", "1 n/Stretch d/m extra"}) {
            assertParseFailure(parser, args, MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_multiwordNameAndEmbeddedSlash_success() {
        assertParseSuccess(parser, "1 s/3 n/Push/Pull Stretch",
                new AddExerciseCommand(INDEX_FIRST_PERSON, new Exercise("Push/Pull Stretch", 3, 1, 0)));
    }

    @Test
    public void parse_duplicatePrefix_failure() {
        // parser wraps duplicate-prefix errors in the generic invalid-format message
        assertParseFailure(parser, "1 n/Bench Press n/Squat s/3 r/10 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 s/4 r/10 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/10 r/12 w/60", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/10 w/60 w/70", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/10 w/60 d/t d/w", MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 n/Bench Press s/3 r/10 w/60 d/t", MESSAGE_INVALID_FORMAT);
    }
}
