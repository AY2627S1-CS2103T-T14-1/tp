package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.DietCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.DietPlan;

public class DietCommandParserTest {
    private final DietCommandParser parser = new DietCommandParser();

    @Test
    public void parse_validArguments_preservesFreeTextAndSupportsOrderAndWhitespace() throws Exception {
        DietCommand expected = new DietCommand(Index.fromOneBased(1),
                new DietPlan("1800 kcal/day,  120 g protein", "Halal meals; avoid shellfish"));
        assertEquals(expected, parser.parse(" 01 t/ 1800 kcal/day,  120 g protein n/ Halal meals; avoid shellfish "));
        assertEquals(expected, parser.parse("1\tn/Halal meals; avoid shellfish\tt/1800 kcal/day,  120 g protein"));
        assertEquals(new DietCommand(Index.fromOneBased(1), new DietPlan("目标 1800 千卡")),
                parser.parse("1 t/目标 1800 千卡"));
    }

    @Test
    public void parse_missingIndexOrTarget_reportsSpecificError() {
        assertParseFailure(parser, "", DietCommand.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, "t/1800 kcal", DietCommand.MESSAGE_INDEX_REQUIRED);
        assertParseFailure(parser, "1", DietCommand.MESSAGE_TARGET_REQUIRED);
        assertParseFailure(parser, "1 n/Lean protein", DietCommand.MESSAGE_TARGET_REQUIRED);
    }

    @Test
    public void parse_invalidIndexOrExtraPreamble_rejectsInput() {
        for (String index : new String[] {"0", "-1", "+1", "1.5", "abc", "2147483648"}) {
            assertParseFailure(parser, index + " t/1800 kcal", DietCommand.MESSAGE_INVALID_INDEX);
        }
        assertParseFailure(parser, "1 extra t/1800 kcal", DietCommand.MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 2 t/1800 kcal", DietCommand.MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateOrUnknownPrefix_rejectsInput() {
        assertParseFailure(parser, "1 t/1800 kcal t/2000 kcal", DietCommand.MESSAGE_REPEATED_PREFIX);
        assertParseFailure(parser, "1 t/1800 kcal n/Lean n/Vegetables", DietCommand.MESSAGE_REPEATED_PREFIX);
        for (String prefix : new String[] {"r/", "x/", "T/", "a/"}) {
            assertParseFailure(parser, "1 t/1800 kcal " + prefix + "extra", DietCommand.MESSAGE_INVALID_FORMAT);
        }
    }

    @Test
    public void parse_blankLongOrNonPrintableFields_rejectsInput() {
        for (String target : new String[] {"", " ", "!!!", "a".repeat(201), "1800\n kcal", "1800\t kcal"}) {
            assertParseFailure(parser, "1 t/" + target, DietPlan.MESSAGE_CONSTRAINTS_TARGET);
        }
        for (String notes : new String[] {"", " ", "a".repeat(301), "Lean\nprotein", "Lean\u0000protein"}) {
            assertParseFailure(parser, "1 t/1800 kcal n/" + notes, DietPlan.MESSAGE_CONSTRAINTS_NOTES);
        }
    }

    @Test
    public void parse_lengthBoundaries_acceptsMaximumValues() throws Exception {
        assertEquals(new DietCommand(Index.fromOneBased(1), new DietPlan("a".repeat(200), "b".repeat(300))),
                parser.parse("1 t/" + "a".repeat(200) + " n/" + "b".repeat(300)));
    }

    @Test
    public void parseCommand_routingPreservesTrailingInvalidLineBreak() throws Exception {
        AddressBookParser router = new AddressBookParser();
        assertEquals(new DietCommand(Index.fromOneBased(1), new DietPlan("1800 kcal")),
                router.parseCommand("  diet 1 t/1800 kcal  "));
        assertThrows(ParseException.class, DietPlan.MESSAGE_CONSTRAINTS_TARGET, () ->
                router.parseCommand("diet 1 t/1800 kcal\n"));
        assertThrows(ParseException.class, DietPlan.MESSAGE_CONSTRAINTS_NOTES, () ->
                router.parseCommand("diet 1 t/1800 kcal n/Lean protein\n"));
    }

    @Test
    public void parse_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
