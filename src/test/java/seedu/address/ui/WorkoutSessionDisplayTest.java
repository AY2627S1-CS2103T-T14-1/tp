package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the selected client's workout session display in headless mode.
 */
public class WorkoutSessionDisplayTest {

    private static final LocalDate DATE = LocalDate.of(2020, 1, 15);
    private static final WorkoutLogEntry SQUAT =
            new WorkoutLogEntry("Back squat", new BigDecimal("60"), 3, 10);
    private static final WorkoutLogEntry BENCH =
            new WorkoutLogEntry("Bench press", new BigDecimal("40.25"), 3, 8);

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
            // Other UI tests may already have started the shared toolkit.
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void emptyList_hidesWorkoutDetails() throws Exception {
        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList());

            assertFalse(label(panel, "workoutHeading").isManaged());
            assertFalse(label(panel, "workoutDate").isVisible());
            assertFalse(label(panel, "workoutTime").isVisible());
            assertFalse(label(panel, "workoutEntries").isManaged());
            assertFalse(label(panel, "workoutEmpty").isVisible());
            return null;
        });
    }

    @Test
    public void selectedClient_showsLastRecordedSessionAndEntryOrder() throws Exception {
        runOnFxThread(() -> {
            WorkoutSession firstRecorded = new WorkoutSession(
                    DATE, Optional.of(LocalTime.of(18, 0)), List.of(BENCH));
            WorkoutSession lastRecorded = new WorkoutSession(
                    DATE.minusDays(1), Optional.empty(), List.of(SQUAT, BENCH));
            Person client = withSessions(new PersonBuilder().withGoal("Build strength").build(),
                    List.of(firstRecorded, lastRecorded));
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(client));

            assertTrue(label(panel, "workoutHeading").isVisible());
            assertEquals("Goal: Build strength", label(panel, "goal").getText());
            assertTrue(label(panel, "goal").isVisible());
            assertEquals("Date: 2020-01-14", label(panel, "workoutDate").getText());
            assertEquals("Time: Not specified", label(panel, "workoutTime").getText());
            assertEquals("1. Back squat: 60 kg, 3 sets x 10 reps\n"
                            + "2. Bench press: 40.25 kg, 3 sets x 8 reps",
                    label(panel, "workoutEntries").getText());
            assertFalse(label(panel, "workoutEmpty").isManaged());
            return null;
        });
    }

    @Test
    public void selection_clientWithoutSessions_clearsPreviousWorkoutDetails() throws Exception {
        runOnFxThread(() -> {
            WorkoutSession session = new WorkoutSession(
                    DATE, Optional.of(LocalTime.of(18, 0)), List.of(SQUAT));
            Person first = withSessions(new PersonBuilder().withName("Alice Lee").build(),
                    List.of(session));
            Person second = new PersonBuilder().withName("Priya Nair").build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(first, second));

            assertEquals("Time: 18:00", label(panel, "workoutTime").getText());

            listView(panel).getSelectionModel().select(1);

            assertEquals("Priya Nair", label(panel, "selectedClientName").getText());
            assertTrue(label(panel, "workoutEmpty").isVisible());
            assertEquals("No workout sessions recorded.", label(panel, "workoutEmpty").getText());
            assertFalse(label(panel, "workoutDate").isManaged());
            assertFalse(label(panel, "workoutTime").isManaged());
            assertFalse(label(panel, "workoutEntries").isManaged());
            assertEquals("", label(panel, "workoutEntries").getText());
            return null;
        });
    }

    @Test
    public void replacementAndDeletion_refreshWorkoutDetails() throws Exception {
        runOnFxThread(() -> {
            Person original = new PersonBuilder().build();
            ObservableList<Person> persons = FXCollections.observableArrayList(original);
            PersonListPanel panel = new PersonListPanel(persons);

            assertTrue(label(panel, "workoutEmpty").isVisible());

            WorkoutSession session = new WorkoutSession(
                    DATE, Optional.of(LocalTime.of(18, 0)), List.of(BENCH));
            persons.set(0, withSessions(original, List.of(session)));

            assertEquals("Date: 2020-01-15", label(panel, "workoutDate").getText());
            assertEquals("Time: 18:00", label(panel, "workoutTime").getText());
            assertEquals("1. Bench press: 40.25 kg, 3 sets x 8 reps",
                    label(panel, "workoutEntries").getText());
            assertFalse(label(panel, "workoutEmpty").isManaged());

            persons.remove(0);

            assertFalse(label(panel, "workoutHeading").isVisible());
            assertFalse(label(panel, "workoutEntries").isManaged());
            assertFalse(label(panel, "workoutEmpty").isVisible());
            return null;
        });
    }

    @Test
    public void thirtyEntries_allRemainAvailableAtNarrowWidth() throws Exception {
        runOnFxThread(() -> {
            WorkoutSession session = new WorkoutSession(
                    DATE, Optional.empty(), Collections.nCopies(30, SQUAT));
            Person client = withSessions(new PersonBuilder().build(), List.of(session));
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(client));
            Region root = panel.getRoot();
            new Scene(root, 340, 600);
            root.applyCss();
            root.resize(340, 600);
            root.layout();

            Label entries = label(panel, "workoutEntries");
            assertEquals(30L, entries.getText().lines().count());
            assertTrue(entries.getText().startsWith("1. Back squat: 60 kg, 3 sets x 10 reps\n"));
            assertTrue(entries.getText().endsWith("30. Back squat: 60 kg, 3 sets x 10 reps"));
            assertTrue(entries.isWrapText());
            assertTrue(entries.prefHeight(140) > entries.prefHeight(1000));
            return null;
        });
    }

    @Test
    public void filtering_refreshesGoalAndWorkoutDetails() throws Exception {
        runOnFxThread(() -> {
            WorkoutSession session = new WorkoutSession(DATE, Optional.empty(), List.of(SQUAT));
            Person first = new PersonBuilder().withName("Alice Lee").build();
            Person second = new PersonBuilder().withName("Priya Nair").withGoal("Build strength")
                    .withWorkoutSessions(List.of(session)).build();
            FilteredList<Person> displayed = new FilteredList<>(FXCollections.observableArrayList(first, second));
            PersonListPanel panel = new PersonListPanel(displayed);

            displayed.setPredicate(person -> person.equals(second));

            assertEquals(second, listView(panel).getSelectionModel().getSelectedItem());
            assertEquals("Goal: Build strength", label(panel, "goal").getText());
            assertEquals("Date: 2020-01-15", label(panel, "workoutDate").getText());
            assertTrue(label(panel, "workoutEntries").isVisible());

            displayed.setPredicate(person -> false);

            assertFalse(label(panel, "goal").isManaged());
            assertFalse(label(panel, "workoutHeading").isManaged());
            assertFalse(label(panel, "workoutEntries").isManaged());

            displayed.setPredicate(person -> true);

            assertEquals(first, listView(panel).getSelectionModel().getSelectedItem());
            assertFalse(label(panel, "goal").isManaged());
            assertTrue(label(panel, "workoutEmpty").isVisible());
            return null;
        });
    }

    private static Person withSessions(Person base, List<WorkoutSession> sessions) {
        return new Person(
                base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getRemark(), base.getTags(), base.getWorkoutPlan(), base.getDietPlan(),
                base.getGoal().orElse(null), sessions);
    }

    private static Label label(PersonListPanel panel, String id) {
        prepareForLookup(panel);
        return (Label) panel.getRoot().lookup("#" + id);
    }

    @SuppressWarnings("unchecked")
    private static ListView<Person> listView(PersonListPanel panel) {
        prepareForLookup(panel);
        return (ListView<Person>) panel.getRoot().lookup("#personListView");
    }

    private static void prepareForLookup(PersonListPanel panel) {
        Region root = panel.getRoot();
        if (root.getScene() == null) {
            new Scene(root, 340, 600);
            root.applyCss();
        }
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
