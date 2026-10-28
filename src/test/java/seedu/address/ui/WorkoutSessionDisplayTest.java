package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.logic.commands.BodyProgressViewRequest;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;
import seedu.address.testutil.PersonBuilder;

/** Tests the redesigned right-hand diet, session, and workout-history context. */
public class WorkoutSessionDisplayTest {

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
            Platform.runLater(ready);
        }
        ready.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void noSelection_showsContextPlaceholder() throws Exception {
        runOnFxThread(() -> {
            ClientContextPanel panel = new ClientContextPanel();

            assertFalse(region(panel, "selectedContent").isManaged());
            assertTrue(region(panel, "emptyContent").isVisible());
            return null;
        });
    }

    @Test
    public void dietPlan_showsTargetAndOptionalNotes() throws Exception {
        runOnFxThread(() -> {
            ClientContextPanel panel = new ClientContextPanel();
            Person withNotes = new PersonBuilder()
                    .withDietPlan(new DietPlan("1800 kcal per day", "Prioritise vegetables")).build();
            panel.showPerson(withNotes, 2);

            assertEquals("1800 kcal per day", label(panel, "dietTarget").getText());
            assertEquals("Prioritise vegetables", label(panel, "dietNotes").getText());
            assertTrue(region(panel, "dietNotesBox").isManaged());

            panel.showPerson(new PersonBuilder().withDietPlan(new DietPlan("High protein")).build(), 1);
            assertFalse(region(panel, "dietNotesBox").isManaged());
            return null;
        });
    }

    @Test
    public void dietaryRestrictions_showWithoutDietAndClearWhenSwitchingClients() throws Exception {
        runOnFxThread(() -> {
            ClientContextPanel panel = new ClientContextPanel();
            Person restricted = new PersonBuilder()
                    .withDietaryRestrictions("Peanut allergy", "Halal meals").build();
            panel.showPerson(restricted, 2);

            assertFalse(region(panel, "dietContent").isManaged());
            assertTrue(region(panel, "dietaryRestrictionsBox").isManaged());
            assertEquals("Peanut allergy; Halal meals", label(panel, "dietaryRestrictions").getText());

            panel.showPerson(new PersonBuilder().build(), 1);
            assertFalse(region(panel, "dietaryRestrictionsBox").isManaged());
            assertEquals("", label(panel, "dietaryRestrictions").getText());
            return null;
        });
    }

    @Test
    public void sessions_areChronologicalAndMarkedByStatus() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().build()
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"))
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2020, 1, 15, 18, 0), "Old studio"));
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(person, 1);
            VBox rows = vbox(panel, "sessionRows");

            assertEquals(2, rows.getChildren().size());
            assertEquals(List.of("WED", "15", "JAN", "Old studio", "Past", "18:00"),
                    labelTexts(rows.getChildren().get(0)));
            assertEquals(List.of("SUN", "15", "MAR", "Main gym", "Next session", "14:30"),
                    labelTexts(rows.getChildren().get(1)));
            assertTrue(rows.getChildren().get(0).getStyleClass().contains("past"));
            assertTrue(rows.getChildren().get(1).getStyleClass().contains("next"));
            return null;
        });
    }

    @Test
    public void sessions_longLocation_keepsAppointmentTimeReadable() throws Exception {
        runOnFxThread(() -> {
            for (String location : List.of("ActiveSG Clementi Sports Centre - Level 2",
                    "NUS University Sports Centre, Sports Hall 1")) {
                Person person = new PersonBuilder().build().withTrainingSession(new TrainingSession(
                        LocalDateTime.of(2099, 3, 15, 14, 30), location));
                ClientContextPanel panel = new ClientContextPanel();
                panel.showPerson(person, 1);
                Scene scene = new Scene(panel.getRoot(), 340, 900);
                scene.getStylesheets().add(WorkoutSessionDisplayTest.class
                        .getResource("/view/FittixTheme.css").toExternalForm());
                panel.getRoot().applyCss();
                panel.getRoot().layout();

                VBox rows = vbox(panel, "sessionRows");
                GridPane row = (GridPane) rows.getChildren().getFirst();
                Label time = (Label) row.getChildren().stream()
                        .filter(node -> node.getStyleClass().contains("session-time"))
                        .findFirst().orElseThrow();
                assertEquals("14:30", time.getText());
                assertTrue(row.getWidth() <= rows.getWidth() + 0.5,
                        "Appointment row exceeds the Schedule card for: " + location);
                assertTrue(time.getWidth() + 0.5 >= time.prefWidth(-1),
                        "Appointment time is clipped for: " + location);
            }
            return null;
        });
    }

    @Test
    public void meeting_isIdentifiedInClientAppointmentCard() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().build()
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 15, 15, 30), "Cafe", TrainingSession.Kind.MEETING));
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(person, 1);

            assertEquals(List.of("SUN", "15", "MAR", "Cafe", "Next meeting", "15:30"),
                    labelTexts(vbox(panel, "sessionRows").getChildren().getFirst()));
            return null;
        });
    }

    @Test
    public void meetings_showPastNextAndUpcomingStatuses() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().build()
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2020, 1, 15, 18, 0), "Old cafe", TrainingSession.Kind.MEETING))
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 15, 15, 30), "Cafe", TrainingSession.Kind.MEETING))
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 16, 16, 0), "Office", TrainingSession.Kind.MEETING))
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 17, 17, 0), "Main gym"));
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(person, 1);
            VBox rows = vbox(panel, "sessionRows");

            assertEquals(4, rows.getChildren().size());
            assertEquals(List.of("WED", "15", "JAN", "Old cafe", "Past meeting", "18:00"),
                    labelTexts(rows.getChildren().get(0)));
            assertEquals(List.of("SUN", "15", "MAR", "Cafe", "Next meeting", "15:30"),
                    labelTexts(rows.getChildren().get(1)));
            assertEquals(List.of("MON", "16", "MAR", "Office", "Upcoming meeting", "16:00"),
                    labelTexts(rows.getChildren().get(2)));
            assertEquals(List.of("TUE", "17", "MAR", "Main gym", "Upcoming", "17:00"),
                    labelTexts(rows.getChildren().get(3)));
            return null;
        });
    }

    @Test
    public void completedWorkouts_areNewestFirstAndKeepAllEntries() throws Exception {
        runOnFxThread(() -> {
            WorkoutSession older = new WorkoutSession(LocalDate.of(2020, 1, 14),
                    Optional.empty(), List.of(SQUAT));
            WorkoutSession newer = new WorkoutSession(LocalDate.of(2020, 1, 15),
                    Optional.of(LocalTime.of(18, 0)), List.of(BENCH, SQUAT));
            Person person = new PersonBuilder().withWorkoutSessions(List.of(older, newer)).build();
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(person, 4);
            VBox rows = vbox(panel, "historyRows");

            assertEquals(2, rows.getChildren().size());
            assertEquals(List.of("✓", "1. Wed 15 Jan 2020", "18:00",
                    "Bench press · 3 × 8 · 40.25 kg", "Back squat · 3 × 10 · 60 kg"),
                    labelTexts(rows.getChildren().get(0)));
            assertEquals(List.of("✓", "2. Tue 14 Jan 2020", "Time not specified",
                    "Back squat · 3 × 10 · 60 kg"), labelTexts(rows.getChildren().get(1)));
            assertTrue(rows.getChildren().get(0).lookup(".history-date") instanceof Label);
            return null;
        });
    }

    @Test
    public void completedWorkouts_zeroWeightEntry_showsBodyweight() throws Exception {
        runOnFxThread(() -> {
            WorkoutLogEntry pushUp = new WorkoutLogEntry("Push up", BigDecimal.ZERO, 3, 12);
            Person person = new PersonBuilder().withWorkoutSessions(List.of(
                    new WorkoutSession(LocalDate.of(2020, 1, 15), Optional.empty(), List.of(pushUp)))).build();
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(person, 1);

            assertEquals(List.of("✓", "1. Wed 15 Jan 2020", "Time not specified", "Push up · 3 × 12 · BW"),
                    labelTexts(vbox(panel, "historyRows").getChildren().getFirst()));
            return null;
        });
    }

    @Test
    public void revealBodyProgress_requestForAnotherClient_isIgnored() throws Exception {
        runOnFxThread(() -> {
            Person displayed = new PersonBuilder().withName("Alice Lee").build();
            Person other = new PersonBuilder().withName("Priya Nair").build();
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(displayed, 1);

            panel.revealBodyProgress(new BodyProgressViewRequest(other, BodyProgressViewRequest.Section.HISTORY));
            panel.revealBodyProgress(new BodyProgressViewRequest(displayed,
                    BodyProgressViewRequest.Section.HISTORY));
            return null;
        });
    }

    @Test
    public void completedWorkouts_sameDate_numbersUnspecifiedBeforeEveningAndMidnight() throws Exception {
        runOnFxThread(() -> {
            LocalDate date = LocalDate.of(2020, 1, 15);
            WorkoutSession noTime = new WorkoutSession(date, Optional.empty(), List.of(SQUAT));
            WorkoutSession midnight = new WorkoutSession(date, Optional.of(LocalTime.MIDNIGHT), List.of(SQUAT));
            WorkoutSession evening = new WorkoutSession(date, Optional.of(LocalTime.of(18, 0)), List.of(BENCH));
            ClientContextPanel panel = new ClientContextPanel();
            for (List<WorkoutSession> stored : List.of(
                    List.of(noTime, midnight, evening), List.of(evening, midnight, noTime))) {
                panel.showPerson(new PersonBuilder().withWorkoutSessions(stored).build(), 1);
                VBox rows = vbox(panel, "historyRows");

                assertEquals(3, rows.getChildren().size());
                assertEquals(List.of("✓", "1. Wed 15 Jan 2020", "Time not specified",
                        "Back squat · 3 × 10 · 60 kg"), labelTexts(rows.getChildren().get(0)));
                assertEquals(List.of("✓", "2. Wed 15 Jan 2020", "18:00",
                        "Bench press · 3 × 8 · 40.25 kg"), labelTexts(rows.getChildren().get(1)));
                assertEquals(List.of("✓", "3. Wed 15 Jan 2020", "00:00",
                        "Back squat · 3 × 10 · 60 kg"), labelTexts(rows.getChildren().get(2)));
            }
            return null;
        });
    }

    @Test
    public void completedWorkouts_fullYearAndUnspecifiedTimeRenderInNarrowColumn() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withWorkoutSessions(List.of(
                    new WorkoutSession(LocalDate.of(2020, 1, 15), Optional.empty(), List.of(SQUAT)),
                    new WorkoutSession(LocalDate.of(2014, 1, 15), Optional.empty(), List.of(SQUAT)),
                    new WorkoutSession(LocalDate.of(2020, 1, 14), Optional.of(LocalTime.of(18, 0)),
                            List.of(BENCH)))).build();
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(person, 1);
            Region root = panel.getRoot();
            Scene scene = new Scene(root, 340, 900);
            scene.getStylesheets().add(getClass().getResource("/view/FittixTheme.css").toExternalForm());
            root.applyCss();
            root.layout();

            VBox rows = (VBox) root.lookup("#historyRows");
            assertEquals(3, rows.getChildren().size());
            for (String style : List.of("history-date", "history-time")) {
                for (Node node : root.lookupAll("." + style)) {
                    Label label = (Label) node;
                    Text rendered = (Text) label.lookup(".text");
                    assertEquals(label.getText(), rendered.getText());
                    assertTrue(rendered.getLayoutBounds().getWidth() <= label.getWidth());
                    assertTrue(rendered.getLayoutBounds().getHeight() <= label.getHeight());
                }
            }
            assertEquals(List.of("✓", "1. Wed 15 Jan 2020", "Time not specified",
                    "Back squat · 3 × 10 · 60 kg"), labelTexts(rows.getChildren().get(0)));
            assertEquals(List.of("✓", "3. Wed 15 Jan 2014", "Time not specified",
                    "Back squat · 3 × 10 · 60 kg"), labelTexts(rows.getChildren().get(2)));
            return null;
        });
    }

    @Test
    public void switchingToClientWithoutRecords_clearsPriorRows() throws Exception {
        runOnFxThread(() -> {
            Person populated = new PersonBuilder().withWorkoutSessions(List.of(
                    new WorkoutSession(LocalDate.of(2020, 1, 15), Optional.empty(), List.of(SQUAT)))).build();
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(populated, 1);
            panel.showPerson(new PersonBuilder().build(), 2);

            assertEquals(0, vbox(panel, "sessionRows").getChildren().size());
            assertEquals(0, vbox(panel, "historyRows").getChildren().size());
            assertFalse(region(panel, "dietContent").isManaged());
            assertFalse(region(panel, "sessionRows").isManaged());
            assertFalse(region(panel, "historyRows").isManaged());
            return null;
        });
    }

    private static Label label(ClientContextPanel panel, String id) {
        prepareForLookup(panel);
        return (Label) panel.getRoot().lookup("#" + id);
    }

    private static Region region(ClientContextPanel panel, String id) {
        prepareForLookup(panel);
        return (Region) panel.getRoot().lookup("#" + id);
    }

    private static VBox vbox(ClientContextPanel panel, String id) {
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

    private static void prepareForLookup(ClientContextPanel panel) {
        Region root = panel.getRoot();
        if (root.getScene() == null) {
            new Scene(root, 340, 900);
            root.applyCss();
        }
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
