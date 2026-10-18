package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

/** Tests the redesigned centre client profile and workout-plan panel. */
public class ClientDetailPanelTest {

    @BeforeAll
    public static void startJavaFx() throws Exception {
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("java.awt.headless", "true");
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
    public void noSelection_showsDetailPlaceholder() throws Exception {
        runOnFxThread(() -> {
            ClientDetailPanel panel = new ClientDetailPanel();

            assertFalse(region(panel, "selectedContent").isManaged());
            assertTrue(region(panel, "emptyContent").isManaged());
            return null;
        });
    }

    @Test
    public void person_showsProfileGoalRemarkAndCommandIndex() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Alex Yeoh").withPhone("87438807")
                    .withEmail("alex@example.com").withAddress("30 Geylang Street")
                    .withGoal("Build strength").withRemark("Prefers morning sessions")
                    .withTags("strength", "beginner").build();
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(person, 3);

            assertEquals("3", label(panel, "profileIndex").getText());
            assertEquals("Alex Yeoh", label(panel, "clientName").getText());
            assertEquals("87438807", label(panel, "phone").getText());
            assertEquals("alex@example.com", label(panel, "email").getText());
            assertEquals("30 Geylang Street", label(panel, "address").getText());
            assertEquals("Build strength", label(panel, "goal").getText());
            assertEquals("Prefers morning sessions", label(panel, "remark").getText());
            assertTrue(region(panel, "remarkBox").isManaged());
            assertEquals("edit 3 …  ·  delete 3", label(panel, "profileCommandHint").getText());
            return null;
        });
    }

    @Test
    public void healthProfile_showsOnlyWhenRecorded() throws Exception {
        runOnFxThread(() -> {
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withHealthProfile("Avoid jumping").build(), 1);
            assertTrue(region(panel, "healthProfileBox").isManaged());
            assertEquals("Avoid jumping", label(panel, "healthProfile").getText());

            panel.showPerson(new PersonBuilder().build(), 2);
            assertFalse(region(panel, "healthProfileBox").isManaged());
            return null;
        });
    }

    @Test
    public void workoutPlan_preservesDisplayIndexesAndUnassignedGroup() throws Exception {
        runOnFxThread(() -> {
            WorkoutPlan plan = new WorkoutPlan(List.of(
                    new Exercise("Back squat", 4, 8, 80, "Monday"),
                    new Exercise("Push up", 3, 12, 0),
                    new Exercise("Bench press", 3, 10, 40.25, "Wednesday")));
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withWorkoutPlan(plan).build(), 1);
            VBox rows = vbox(panel, "workoutRows");
            List<String> text = labelTexts(rows);

            assertEquals(8, rows.getChildren().size());
            assertTrue(text.containsAll(List.of("MON", "Back squat", "1", "4 × 8", "80 kg",
                    "WED", "Bench press", "2", "3 × 10", "40.25 kg",
                    "UNSET", "Push up", "3", "3 × 12", "BW")));
            assertEquals("3 exercises · 2 training days", label(panel, "planSummary").getText());
            return null;
        });
    }

    @Test
    public void emptyPlan_showsCompatibleAddExerciseCommand() throws Exception {
        runOnFxThread(() -> {
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().build(), 2);

            assertTrue(region(panel, "workoutEmpty").isManaged());
            assertFalse(region(panel, "workoutRows").isManaged());
            assertEquals("addex 2 n/Back Squat s/4 r/8 w/80 d/MON",
                    label(panel, "workoutEmptyCommand").getText());
            assertEquals("—", label(panel, "goal").getText());
            assertFalse(region(panel, "remarkBox").isManaged());
            return null;
        });
    }

    private static Label label(ClientDetailPanel panel, String id) {
        prepareForLookup(panel);
        return (Label) panel.getRoot().lookup("#" + id);
    }

    private static Region region(ClientDetailPanel panel, String id) {
        prepareForLookup(panel);
        return (Region) panel.getRoot().lookup("#" + id);
    }

    private static VBox vbox(ClientDetailPanel panel, String id) {
        return (VBox) region(panel, id);
    }

    private static List<String> labelTexts(Node root) {
        List<String> result = new ArrayList<>();
        collectLabelTexts(root, result);
        return result;
    }

    private static void collectLabelTexts(Node node, List<String> result) {
        if (node instanceof Label label) {
            result.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collectLabelTexts(child, result));
        }
    }

    private static void prepareForLookup(ClientDetailPanel panel) {
        Region root = panel.getRoot();
        if (root.getScene() == null) {
            new Scene(root, 660, 900);
            root.applyCss();
        }
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
