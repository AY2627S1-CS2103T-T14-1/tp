package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditLogCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/** Tests parsing of workout history queries and complete session replacements. */
public class WorkoutHistoryParserTest {
    private final EditLogCommandParser editParser = new EditLogCommandParser();
    private final HistoryCommandParser historyParser = new HistoryCommandParser();

    @Test
    public void history_validAndMissingIndex() {
        assertParseSuccess(historyParser, " 1", new HistoryCommand(Index.fromOneBased(1)));
        assertParseFailure(historyParser, "",
                String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                        HistoryCommand.MESSAGE_USAGE));
    }

    @Test
    public void edit_validReplacementAndPrefixOrder() {
        WorkoutLogEntry squat = new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
        WorkoutSession replacement = new WorkoutSession(LocalDate.of(2020, 1, 15),
                Optional.of(LocalTime.of(18, 0)), List.of(squat));
        EditLogCommand expected = new EditLogCommand(
                Index.fromOneBased(1), Index.fromOneBased(2), replacement);

        assertParseSuccess(editParser, " 1 2 d/2020-01-15 t/18:00 ex/Back squat|60|3|10", expected);
        assertParseSuccess(editParser, " 1 2 ex/Back squat|60|3|10 t/18:00 d/2020-01-15", expected);
    }

    @Test
    public void edit_badIndexAndInvalidSession() {
        String usage = String.format(seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT,
                EditLogCommand.MESSAGE_USAGE);
        assertParseFailure(editParser, " 1 d/2020-01-15 ex/Back squat|60|3|10", usage);
        assertParseFailure(editParser, " 1 2 3 d/2020-01-15 ex/Back squat|60|3|10", usage);
        assertParseFailure(editParser, " 0 1 d/2020-01-15 ex/Back squat|60|3|10",
                ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(editParser, " 1 2 d/2020-01-15 ex/Back squat|60|3",
                LogCommandParser.MESSAGE_INVALID_EXERCISE_FORMAT);
        assertParseFailure(editParser, " 1 2 d/2020-01-15 t/24:00 ex/Back squat|60|3|10",
                WorkoutSession.MESSAGE_INVALID_TIME);
    }

    @Test
    public void router_recognizesHistoryAndEditLog() throws Exception {
        AddressBookParser router = new AddressBookParser();
        assertEquals(new HistoryCommand(Index.fromOneBased(1)), router.parseCommand("history 1"));
        WorkoutSession session = new WorkoutSession(LocalDate.of(2020, 1, 15), Optional.empty(),
                List.of(new WorkoutLogEntry("Push-up", BigDecimal.ZERO, 3, 12)));
        assertEquals(new EditLogCommand(Index.fromOneBased(1), Index.fromOneBased(2), session),
                router.parseCommand("editlog 1 2 d/2020-01-15 ex/Push-up|0|3|12"));
    }
}
