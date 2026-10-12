package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.parser.exceptions.ParseException;

public class SetExerciseDayCommandParserTest {
    private final SetExerciseDayCommandParser parser = new SetExerciseDayCommandParser();

    @Test
    public void parse_validArgs_returnsCommand() throws Exception {
        assertEquals(new SetExerciseDayCommand(Index.fromOneBased(1), Index.fromOneBased(2), "Tuesday"),
                parser.parse("1 2 day/Tuesday"));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("1 2"));
        assertThrows(ParseException.class, () -> parser.parse("0 2 day/Tuesday"));
        assertThrows(ParseException.class, () -> parser.parse("1 2 day/"));
        assertThrows(ParseException.class, () -> parser.parse("1 2 3 day/Tuesday"));
    }
}
