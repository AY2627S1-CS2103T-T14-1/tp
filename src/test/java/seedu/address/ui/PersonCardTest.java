package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

/** Checks the actual card labels produced by addex and legacy unspecified days. */
public class PersonCardTest {
    @BeforeAll
    public static void initializeJavaFx() throws Exception {
        FutureTask<Void> ready = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
        try {
            Platform.startup(ready);
        } catch (IllegalStateException e) {
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void addExercise_nameOnly_displaysDefaultValues() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().build());
        new AddressBookParser().parseCommand("addex 1 n/Shoulder Press").execute(model);

        assertCardLabels(model.getFilteredPersonList().get(0), List.of(
                "Workout Plan (1):", "Day not specified", "1. Shoulder Press 1x1 @0kg"));
    }

    @Test
    public void addExercise_mixedLegacyDays_displaysOneUnspecifiedHeading() throws Exception {
        WorkoutPlan plan = new WorkoutPlan(List.of(
                new Exercise("Sit Up", 3, 20, 0),
                new Exercise("Jumping Jacks", 3, 30, 0, "day not specified"),
                new Exercise("Stretch", 1, 1, 0, " Day not specified ")));
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withWorkoutPlan(plan).build());
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("addex 1 n/Shoulder Press").execute(model);
        parser.parseCommand("addex 1 n/Squat s/4 r/8 w/80 d/m").execute(model);

        assertCardLabels(model.getFilteredPersonList().get(0), List.of(
                "Workout Plan (5):", "Monday", "1. Squat 4x8 @80kg", "Day not specified",
                "2. Sit Up 3x20 @0kg", "3. Jumping Jacks 3x30 @0kg", "4. Stretch 1x1 @0kg",
                "5. Shoulder Press 1x1 @0kg"));
    }

    private void assertCardLabels(Person person, List<String> expected) throws Exception {
        FutureTask<Void> assertions = new FutureTask<>(() -> {
            PersonCard card = new PersonCard(person, 1);
            VBox container = (VBox) card.getRoot().lookup("#workoutPlanContainer");
            assertNotNull(container);
            List<String> labels = container.getChildren().stream()
                    .filter(Label.class::isInstance).map(Label.class::cast)
                    .map(Label::getText).filter(text -> !text.isEmpty()).toList();
            assertEquals(expected, labels);
            return null;
        });
        Platform.runLater(assertions);
        assertions.get(10, TimeUnit.SECONDS);
    }
}
