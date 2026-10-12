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
                parser.parse("1 2 Tuesday"));
        assertEquals(new SetExerciseDayCommand(Index.fromOneBased(1), Index.fromOneBased(2), "Tuesday"),
                parser.parse("1 2 Tue"));
        assertEquals(new SetExerciseDayCommand(Index.fromOneBased(1), Index.fromOneBased(2), "Tuesday"),
                parser.parse("1 2 tU"));
        assertEquals(new SetExerciseDayCommand(Index.fromOneBased(1), Index.fromOneBased(2), "Thursday"),
                parser.parse("1 2 th"));
        assertEquals(new SetExerciseDayCommand(Index.fromOneBased(1), Index.fromOneBased(2), "Monday"),
                parser.parse("1 2 m"));
    }

    @Test
    public void parse_invalidArgs_throwsParseException() {
        assertThrows(ParseException.class, () -> parser.parse("1 2"));
        assertThrows(ParseException.class, () -> parser.parse("0 2 Tuesday"));
        assertThrows(ParseException.class, () -> parser.parse("1 2"));
        assertThrows(ParseException.class, () -> parser.parse("1 2 3 4"));
        assertThrows(ParseException.class, () -> parser.parse("1 2 t"));
        assertThrows(ParseException.class, () -> parser.parse("1 2 xyz"));
    }
}
