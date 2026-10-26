package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.DietaryRestrictionsCommand;
import seedu.address.model.person.DietaryRestrictions;

public class DietaryRestrictionsCommandParserTest {
    private final DietaryRestrictionsCommandParser parser = new DietaryRestrictionsCommandParser();

    @Test
    public void parse_replaceAndClear_success() {
        assertParseSuccess(parser, "1 r/Peanut allergy r/Halal meals",
                new DietaryRestrictionsCommand(INDEX_FIRST_PERSON,
                        new DietaryRestrictions(List.of("Peanut allergy", "Halal meals"))));
        assertParseSuccess(parser, "1 r/",
                new DietaryRestrictionsCommand(INDEX_FIRST_PERSON, new DietaryRestrictions()));
    }

    @Test
    public void parse_invalidInput_failure() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DietaryRestrictionsCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "1", usage);
        assertParseFailure(parser, "1 r/ r/Halal meals", usage);
        assertParseFailure(parser, "0 r/Halal meals", usage);
        assertParseFailure(parser, "1 r/Halal meals r/halal MEALS", DietaryRestrictions.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_unknownPrefix_rejectedBeforeReplacingRestrictions() {
        String usage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, DietaryRestrictionsCommand.MESSAGE_USAGE);
        for (String arguments : List.of("1 r/No dairy x/unknown", "1 x/unknown r/No dairy",
                "1 r/No dairy n/Peanut allergy", "1 r/ x/unknown", "1 r/No dairy\tx/unknown")) {
            assertParseFailure(parser, arguments, usage);
        }
    }

    @Test
    public void parse_slashesInsideWordsAndFractions_preserved() {
        String restriction = "No milk/egg products; limit to 1/2 cup";
        assertParseSuccess(parser, "1 r/" + restriction,
                new DietaryRestrictionsCommand(INDEX_FIRST_PERSON, new DietaryRestrictions(List.of(restriction))));
    }
}
