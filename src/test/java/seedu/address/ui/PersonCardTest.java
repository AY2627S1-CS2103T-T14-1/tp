package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

/** Tests the compact client-list card used by the redesigned workspace. */
public class PersonCardTest {

    @BeforeAll
    public static void initializeJavaFx() throws Exception {
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
    public void personCard_showsCompactIdentityAndPlanSummary() throws Exception {
        runOnFxThread(() -> {
            WorkoutPlan plan = new WorkoutPlan(List.of(
                    new Exercise("Squat", 4, 8, 80, "Monday"),
                    new Exercise("Bench press", 3, 10, 45, "Wednesday")));
            Person person = new PersonBuilder().withName("Alex Yeoh").withPhone("87438807")
                    .withTags("strength", "beginner", "mobility").withWorkoutPlan(plan).build();
            PersonCard card = new PersonCard(person, 3);

            assertEquals("3", label(card, "id").getText());
            assertEquals("Alex Yeoh", label(card, "name").getText());
            assertEquals("87438807", label(card, "phone").getText());
            assertEquals("2 exercises · No session booked", label(card, "meta").getText());
            assertEquals(List.of("beginner", "mobility"), tagTexts(card));
            return null;
        });
    }

    @Test
    public void upcomingSession_showsNearestFutureSession() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().build()
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 16, 9, 0), "Studio"))
                    .withTrainingSession(new TrainingSession(
                            LocalDateTime.of(2099, 3, 15, 14, 30), "Main gym"));
            PersonCard card = new PersonCard(person, 1);

            assertEquals("No plan · Next Sun 15 Mar", label(card, "meta").getText());
            return null;
        });
    }

    @Test
    public void membershipTag_longName_wrapsWithinCard() throws Exception {
        runOnFxThread(() -> {
            String longName = "Gold Premium Annual Membership Plan For One Client";
            Person assigned = new PersonBuilder().build().withMembershipAssignment(
                    Optional.of(new MembershipAssignment(longName, longName, 30,
                            Optional.of(LocalDate.of(2026, 12, 30)))));
            PersonCard card = new PersonCard(assigned, 1);
            Region root = card.getRoot();
            new Scene(root, 340, 600);
            root.applyCss();
            root.layout();

            Label tag = (Label) root.lookup(".membership-tag");
            assertNotNull(tag);
            assertTrue(tag.isWrapText());
            assertTrue(tag.getWidth() > 0, "membership tag should be laid out");
            assertTrue(tag.getWidth() <= 340,
                    "membership tag must not exceed the card width, was " + tag.getWidth());
            return null;
        });
    }

    private static Label label(PersonCard card, String id) {
        prepareForLookup(card);
        return (Label) card.getRoot().lookup("#" + id);
    }

    private static List<String> tagTexts(PersonCard card) {
        prepareForLookup(card);
        FlowPane tags = (FlowPane) card.getRoot().lookup("#tags");
        return tags.getChildren().stream().map(node -> ((Label) node).getText()).toList();
    }

    private static void prepareForLookup(PersonCard card) {
        Region root = card.getRoot();
        if (root.getScene() == null) {
            new Scene(root, 300, 180);
            root.applyCss();
        }
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
