package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

public class DietCommandTest {
    @Test
    public void execute_createAndReplace_preservesOtherClientDataAndClearsOmittedNotes() throws Exception {
        Person original = new PersonBuilder().withRemark("Regular client").withTags("regular")
                .withWorkoutPlan(new WorkoutPlan().addExercise(new Exercise("Squat", 3, 10, 60))).build();
        ModelManager model = new ModelManager();
        model.addPerson(original);
        DietPlan firstPlan = new DietPlan("1800 kcal", "Lean protein");
        assertEquals("Created diet plan for Amy Bee.",
                new DietCommand(Index.fromOneBased(1), firstPlan).execute(model).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withDietPlan(firstPlan).build(), model.getFilteredPersonList().get(0));
        DietPlan replacement = new DietPlan("2000 kcal");
        assertEquals("Replaced diet plan for Amy Bee.",
                new DietCommand(Index.fromOneBased(1), replacement).execute(model).getFeedbackToUser());
        assertEquals(new PersonBuilder(original).withDietPlan(replacement).build(),
                model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_duplicatePlan_rejectsWithoutChangingOriginalText() {
        Person original = new PersonBuilder().withDietPlan(new DietPlan("1800 kcal", "Lean protein")).build();
        ModelManager model = new ModelManager();
        model.addPerson(original);
        assertThrows(CommandException.class, "This is already the current diet plan for Amy Bee.", () ->
                new DietCommand(Index.fromOneBased(1), new DietPlan(" 1800 KCAL ", " lean PROTEIN ")).execute(model));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void execute_changedNotes_replacesPlan() throws Exception {
        Person original = new PersonBuilder().withDietPlan(new DietPlan("1800 kcal", "Lean protein")).build();
        ModelManager model = new ModelManager();
        model.addPerson(original);
        DietPlan changed = new DietPlan("1800 kcal", "More vegetables");
        new DietCommand(Index.fromOneBased(1), changed).execute(model);
        assertEquals(changed, model.getFilteredPersonList().get(0).getDietPlan().orElseThrow());
    }

    @Test
    public void execute_filteredIndex_targetsDisplayedClientAndKeepsFilter() throws Exception {
        ModelManager model = new ModelManager();
        Person amy = new PersonBuilder().build();
        Person bob = new PersonBuilder().withName("Bob Tan").build();
        model.addPerson(amy);
        model.addPerson(bob);
        model.updateFilteredPersonList(new NameContainsKeywordsPredicate(Arrays.asList("Bob")));
        DietPlan plan = new DietPlan("1800 kcal");
        new DietCommand(Index.fromOneBased(1), plan).execute(model);
        assertEquals(1, model.getFilteredPersonList().size());
        assertEquals(new PersonBuilder(bob).withDietPlan(plan).build(), model.getFilteredPersonList().get(0));
        assertEquals(amy, model.getAddressBook().getPersonList().get(0));
    }

    @Test
    public void execute_outOfRange_rejectsWithoutChanges() {
        ModelManager model = new ModelManager();
        Person original = new PersonBuilder().build();
        model.addPerson(original);
        assertThrows(CommandException.class, "No client exists at index 2.", () ->
                new DietCommand(Index.fromOneBased(2), new DietPlan("1800 kcal")).execute(model));
        assertEquals(original, model.getFilteredPersonList().get(0));
    }

    @Test
    public void constructorAndExecute_nullArguments_rejectNull() {
        assertThrows(NullPointerException.class, () -> new DietCommand(null, new DietPlan("1800 kcal")));
        assertThrows(NullPointerException.class, () -> new DietCommand(Index.fromOneBased(1), null));
        assertThrows(NullPointerException.class, () -> new DietCommand(Index.fromOneBased(1),
                new DietPlan("1800 kcal")).execute(null));
    }

    @Test
    public void equalsAndToString_compareCommandData() {
        DietPlan plan = new DietPlan("1800 kcal");
        DietCommand command = new DietCommand(Index.fromOneBased(1), plan);
        assertEquals(command, command);
        assertEquals(command, new DietCommand(Index.fromOneBased(1), plan));
        assertFalse(command.equals(null));
        assertFalse(command.equals("diet"));
        assertFalse(command.equals(new DietCommand(Index.fromOneBased(2), plan)));
        assertFalse(command.equals(new DietCommand(Index.fromOneBased(1), new DietPlan("2000 kcal"))));
        assertEquals(DietCommand.class.getCanonicalName() + "{index=" + Index.fromOneBased(1)
                + ", dietPlan=" + plan + "}", command.toString());
    }
}
