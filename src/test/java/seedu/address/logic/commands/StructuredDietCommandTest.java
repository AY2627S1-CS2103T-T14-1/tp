package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;
import seedu.address.model.person.DietPlan;
import seedu.address.testutil.PersonBuilder;

/** End-to-end command parsing and model updates for the optional numeric diet format. */
public class StructuredDietCommandTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void structuredValues_acceptOrderWhitespaceAndPartialMacros() throws Exception {
        ModelManager model = twoClients();
        run(model, "diet 2 cal/2400 pro/170 carb/250 fat/70 n/Lean bulk");
        DietPlan full = model.getFilteredPersonList().get(1).getDietPlan().orElseThrow();
        assertEquals(Optional.of(2400), full.getCalories());
        assertEquals(Optional.of(170), full.getProteinGrams());
        assertEquals(Optional.of(250), full.getCarbsGrams());
        assertEquals(Optional.of(70), full.getFatGrams());
        assertEquals(Optional.of("Lean bulk"), full.getNotes());

        run(model, "diet   2   n/Low sugar pro/170 cal/2400");
        DietPlan partial = model.getFilteredPersonList().get(1).getDietPlan().orElseThrow();
        assertEquals(Optional.of(170), partial.getProteinGrams());
        assertTrue(partial.getCarbsGrams().isEmpty());
        assertTrue(partial.getFatGrams().isEmpty());
        assertEquals(Optional.of("Low sugar"), partial.getNotes());

        run(model, "diet 2 cal/2400");
        DietPlan caloriesOnly = model.getFilteredPersonList().get(1).getDietPlan().orElseThrow();
        assertEquals(Optional.of(2400), caloriesOnly.getCalories());
        assertTrue(caloriesOnly.getProteinGrams().isEmpty());
        assertEquals(Optional.of("Low sugar"), caloriesOnly.getNotes());

        run(model, "diet 2 cal/2200 pro/170 pro/");
        assertTrue(model.getFilteredPersonList().get(1).getDietPlan().orElseThrow()
                .getProteinGrams().isEmpty());
    }

    @Test
    public void invalidNumericValues_reportSpecificErrorWithoutChangingPlan() throws Exception {
        ModelManager model = twoClients();
        run(model, "diet 2 cal/2000");
        DietPlan previous = model.getFilteredPersonList().get(1).getDietPlan().orElseThrow();
        String[] badCalories = {"diet 2 pro/170", "diet 2 cal/2400.5",
            "diet 2 cal/abc", "diet 2 cal/", "diet 2 cal/2 400", "diet 2 cal/0"};
        for (String command : badCalories) {
            CommandException error = assertThrows(CommandException.class, () -> run(model, command));
            assertEquals(DietCommand.MESSAGE_INVALID_CALORIES, error.getMessage());
            assertEquals(previous, model.getFilteredPersonList().get(1).getDietPlan().orElseThrow());
        }
        String[] badMacros = {"diet 2 cal/2000 pro/abc",
            "diet 2 cal/2000 carb/2.5", "diet 2 cal/2000 fat/-1"};
        for (String command : badMacros) {
            CommandException error = assertThrows(CommandException.class, () -> run(model, command));
            assertEquals(DietCommand.MESSAGE_INVALID_MACROS, error.getMessage());
            assertEquals(previous, model.getFilteredPersonList().get(1).getDietPlan().orElseThrow());
        }
    }

    @Test
    public void invalidIndexOrFormat_precedesNumericValidation() {
        ModelManager model = twoClients();
        CommandException range = assertThrows(CommandException.class, () -> run(model, "diet 99 cal/abc"));
        assertEquals("The client index provided is invalid.", range.getMessage());
        String[] badFormats = {"diet cal/2000", "diet 2 cal /2000"};
        for (String command : badFormats) {
            ParseException error = assertThrows(ParseException.class, () -> run(model, command));
            assertTrue(error.getMessage().startsWith("Invalid command format"));
        }
    }

    @Test
    public void legacyDietFormat_remainsCompatible() throws Exception {
        ModelManager model = twoClients();
        run(model, "diet 2 t/1800 kcal/day n/Old notes");
        assertTrue(model.getFilteredPersonList().get(1).getDietPlan().orElseThrow().getCalories().isEmpty());
        run(model, "diet 2 cal/2200 pro/100");
        assertEquals(Optional.of("Old notes"),
                model.getFilteredPersonList().get(1).getDietPlan().orElseThrow().getNotes());
        run(model, "diet 2 t/2000 kcal per day");
        DietPlan legacy = model.getFilteredPersonList().get(1).getDietPlan().orElseThrow();
        assertTrue(legacy.getCalories().isEmpty());
        assertTrue(legacy.getNotes().isEmpty());
    }

    private void run(ModelManager model, String command) throws CommandException, ParseException {
        parser.parseCommand(command).execute(model);
    }

    private static ModelManager twoClients() {
        ModelManager model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("First Client").build());
        model.addPerson(new PersonBuilder().withName("Second Client").build());
        return model;
    }
}
