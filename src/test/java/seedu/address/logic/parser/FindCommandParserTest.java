package seedu.address.logic.parser;

import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.ClientMatchesAnyPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", "Search keyword is required. Use: find KEYWORD... or "
                + "find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]");
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand = new FindCommand(new ClientMatchesAnyPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")))));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_prefixedName_returnsFindCommand() {
        FindCommand expectedFindCommand = new FindCommand(new ClientMatchesAnyPredicate(
                List.of(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")))));
        assertParseSuccess(parser, " n/Alice Bob", expectedFindCommand);
        assertParseSuccess(parser, " Alice Bob", expectedFindCommand);
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertParseFailure(parser, " alex n/bob", "Invalid command format. Use: find KEYWORD... or "
                + "find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]");
        assertParseFailure(parser, " n/alex n/bob", "The n/, p/, e/ and g/ prefixes may each be used only once.");
        assertParseFailure(parser, " n/", "The n/ search value cannot be empty.");
    }

}
