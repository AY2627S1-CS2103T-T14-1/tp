package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.testutil.PersonBuilder;

public class ScheduleCommandTest {
    @Test
    public void execute_multipleSessions_addedAndViewedInTimeOrder() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().get(0);
        TrainingSession later = new TrainingSession(LocalDateTime.of(2099, 3, 16, 9, 0), "Studio");
        TrainingSession earlier = new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym");
        new ScheduleCommand(INDEX_FIRST_PERSON, later).execute(model);
        new ScheduleCommand(INDEX_FIRST_PERSON, earlier).execute(model);
        assertEquals(2, model.getFilteredPersonList().get(0).getTrainingSessions().size());
        assertEquals("Training sessions for " + original.getName() + ":\n"
                + "1. 2099-03-15 14:30 at Main gym\n2. 2099-03-16 09:00 at Studio",
                new ViewScheduleCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser());
    }

    @Test
    public void execute_missingClient_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        TrainingSession session = new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Gym");
        Index missing = Index.fromOneBased(999);
        CommandException exception = assertThrows(CommandException.class, () ->
                new ScheduleCommand(missing, session).execute(model));
        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, exception.getMessage());
    }

    @Test
    public void viewSchedule_noSessions_showsEmptyState() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person client = model.getFilteredPersonList().get(0);

        assertEquals("No training sessions saved for " + client.getName() + ".",
                new ViewScheduleCommand(INDEX_FIRST_PERSON).execute(model).getFeedbackToUser());
    }

    @Test
    public void viewSchedule_missingClient_throwsCommandException() {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        CommandException exception = assertThrows(CommandException.class, () ->
                new ViewScheduleCommand(Index.fromOneBased(999)).execute(model));

        assertEquals(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX, exception.getMessage());
    }

    @Test
    public void workoutCommands_preserveSavedTrainingSessions() throws Exception {
        Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        Person original = model.getFilteredPersonList().get(0);
        model.setPerson(original, new PersonBuilder(original).withGoal("Build strength").build());
        TrainingSession session = new TrainingSession(LocalDateTime.of(2099, 3, 15, 14, 30), "Gym");
        new ScheduleCommand(INDEX_FIRST_PERSON, session).execute(model);

        new AddExerciseCommand(INDEX_FIRST_PERSON, new Exercise("Squat", 3, 10, 60)).execute(model);
        assertEquals(List.of(session), model.getFilteredPersonList().get(0).getTrainingSessions());

        new SetExerciseDayCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON, "Monday").execute(model);
        assertEquals(List.of(session), model.getFilteredPersonList().get(0).getTrainingSessions());

        new DeleteExerciseCommand(INDEX_FIRST_PERSON, INDEX_FIRST_PERSON).execute(model);
        assertEquals(List.of(session), model.getFilteredPersonList().get(0).getTrainingSessions());

        new AddExerciseCommand(INDEX_FIRST_PERSON, new Exercise("Squat", 3, 10, 60)).execute(model);
        new ClearPlanCommand(INDEX_FIRST_PERSON).execute(model);
        assertEquals(List.of(session), model.getFilteredPersonList().get(0).getTrainingSessions());
        assertEquals("Build strength", model.getFilteredPersonList().get(0).getGoal().orElseThrow().value);
    }
}
