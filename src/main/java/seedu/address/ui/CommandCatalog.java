package seedu.address.ui;

import java.util.List;
import java.util.Locale;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.logic.commands.BodyBaselineCommand;
import seedu.address.logic.commands.BodyCompareCommand;
import seedu.address.logic.commands.BodyDeleteCommand;
import seedu.address.logic.commands.BodyHistoryCommand;
import seedu.address.logic.commands.BodyLogCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ClearPlanCommand;
import seedu.address.logic.commands.DayScheduleCommand;
import seedu.address.logic.commands.DeleteCommand;
import seedu.address.logic.commands.DeleteExerciseCommand;
import seedu.address.logic.commands.DietCommand;
import seedu.address.logic.commands.DietaryRestrictionsCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditLogCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HealthCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.HistoryCommand;
import seedu.address.logic.commands.InactiveCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.LogCommand;
import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.ViewScheduleCommand;

/** UI-facing catalogue of the existing command words and usage strings. */
final class CommandCatalog {

    private static final List<Entry> ENTRIES = List.of(
            new Entry(AddCommand.COMMAND_WORD, AddCommand.MESSAGE_USAGE),
            new Entry(EditCommand.COMMAND_WORD, EditCommand.MESSAGE_USAGE),
            new Entry(DeleteCommand.COMMAND_WORD, DeleteCommand.MESSAGE_USAGE),
            new Entry(DietCommand.COMMAND_WORD,
                    DietCommand.MESSAGE_USAGE + "\n" + DietCommand.MESSAGE_STRUCTURED_USAGE),
            new Entry(DietaryRestrictionsCommand.COMMAND_WORD, DietaryRestrictionsCommand.MESSAGE_USAGE),
            new Entry(HealthCommand.COMMAND_WORD, HealthCommand.MESSAGE_USAGE),
            new Entry(BodyBaselineCommand.COMMAND_WORD, BodyBaselineCommand.MESSAGE_USAGE),
            new Entry(BodyCompareCommand.COMMAND_WORD, BodyCompareCommand.MESSAGE_USAGE),
            new Entry(BodyDeleteCommand.COMMAND_WORD, BodyDeleteCommand.MESSAGE_USAGE),
            new Entry(BodyHistoryCommand.COMMAND_WORD, BodyHistoryCommand.MESSAGE_USAGE),
            new Entry(BodyLogCommand.COMMAND_WORD, BodyLogCommand.MESSAGE_USAGE),
            new Entry(ClearCommand.COMMAND_WORD, "clear: Clears every client."),
            new Entry(FindCommand.COMMAND_WORD, FindCommand.MESSAGE_USAGE),
            new Entry(ListCommand.COMMAND_WORD, "list: Lists all clients."),
            new Entry(LogCommand.COMMAND_WORD, LogCommand.MESSAGE_USAGE),
            new Entry(EditLogCommand.COMMAND_WORD, EditLogCommand.MESSAGE_USAGE),
            new Entry(HistoryCommand.COMMAND_WORD, HistoryCommand.MESSAGE_USAGE),
            new Entry(PayCommand.COMMAND_WORD, PayCommand.MESSAGE_USAGE),
            new Entry(RemarkCommand.COMMAND_WORD, RemarkCommand.MESSAGE_USAGE),
            new Entry(ScheduleCommand.COMMAND_WORD, ScheduleCommand.MESSAGE_USAGE),
            new Entry(ViewScheduleCommand.COMMAND_WORD, ViewScheduleCommand.MESSAGE_USAGE),
            new Entry(MeetingCommand.COMMAND_WORD, MeetingCommand.MESSAGE_USAGE),
            new Entry(DayScheduleCommand.COMMAND_WORD, DayScheduleCommand.MESSAGE_USAGE),
            new Entry(InactiveCommand.COMMAND_WORD, InactiveCommand.MESSAGE_USAGE),
            new Entry(AddExerciseCommand.COMMAND_WORD, AddExerciseCommand.MESSAGE_USAGE),
            new Entry(DeleteExerciseCommand.COMMAND_WORD, DeleteExerciseCommand.MESSAGE_USAGE),
            new Entry(ClearPlanCommand.COMMAND_WORD, ClearPlanCommand.MESSAGE_USAGE),
            new Entry(SetExerciseDayCommand.COMMAND_WORD, SetExerciseDayCommand.MESSAGE_USAGE),
            new Entry(UndoCommand.COMMAND_WORD, UndoCommand.MESSAGE_USAGE),
            new Entry(RedoCommand.COMMAND_WORD, RedoCommand.MESSAGE_USAGE),
            new Entry(ExitCommand.COMMAND_WORD, "exit: Exits Fittix."),
            new Entry(HelpCommand.COMMAND_WORD, HelpCommand.MESSAGE_USAGE));

    private static final List<Suggestion> DEFAULT_SUGGESTIONS = List.of(
            new Suggestion("help", "help"),
            new Suggestion("find alex", "find alex"),
            new Suggestion("addex 1 n/Squat s/5 r/5 w/100 d/SAT",
                    "addex 1 n/Squat s/5 r/5 w/100 d/SAT"),
            new Suggestion("schedule 1 d/2099-03-15 tm/14:30 l/Main gym",
                    "schedule 1 d/2099-03-15 tm/14:30 l/Main gym"));

    private CommandCatalog() {
    }

    static List<Entry> matching(String prefix) {
        String normalized = prefix.toLowerCase(Locale.ROOT);
        return ENTRIES.stream().filter(entry -> entry.word().startsWith(normalized)).limit(4).toList();
    }

    static List<Suggestion> defaultSuggestions() {
        return DEFAULT_SUGGESTIONS;
    }

    static List<String> helpDetails() {
        return ENTRIES.stream().map(Entry::usage).toList();
    }

    /** A command and its existing usage text. */
    record Entry(String word, String usage) {
    }

    /** Text displayed on a hint chip and the command inserted when it is clicked. */
    record Suggestion(String label, String command) {
    }
}
