package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Tests the selected client's diet details with the JavaFX toolkit in headless mode.
 */
public class PersonListPanelTest {

    @BeforeAll
    public static void startJavaFx() {
        System.setProperty("glass.platform", "Monocle");
        System.setProperty("monocle.platform", "Headless");
        System.setProperty("prism.order", "sw");
        System.setProperty("java.awt.headless", "true");
        Platform.startup(() -> { });
    }

    @AfterAll
    public static void stopJavaFx() {
        Platform.exit();
    }

    @Test
    public void emptyList_promptsForClientSelection() throws Exception {
        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList());
            assertEquals("Select a client to view their current diet plan.",
                    label(panel, "selectedClientName").getText());
            assertFalse(label(panel, "dietPlanHeading").isManaged());
            assertFalse(label(panel, "dietEmpty").isVisible());
            return null;
        });
    }

    @Test
    public void selectionShowsCurrentPlanAndHidesAbsentNotes() throws Exception {
        runOnFxThread(() -> {
            Person first = new PersonBuilder().withName("Alice Lee").build();
            Person second = new PersonBuilder().withName("Priya Nair")
                    .withDietPlan(new DietPlan("1800 kcal per day", "Prioritise vegetables")).build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(first, second));
            assertEquals("Alice Lee", label(panel, "selectedClientName").getText());
            assertTrue(label(panel, "dietEmpty").isVisible());

            listView(panel).getSelectionModel().select(1);
            assertEquals("Priya Nair", label(panel, "selectedClientName").getText());
            assertEquals("Target: 1800 kcal per day", label(panel, "dietTarget").getText());
            assertEquals("Notes: Prioritise vegetables", label(panel, "dietNotes").getText());
            assertFalse(label(panel, "dietEmpty").isManaged());
            return null;
        });
    }

    @Test
    public void replacementAndDeletionRefreshSelectedDetails() throws Exception {
        runOnFxThread(() -> {
            Person original = new PersonBuilder().withName("Priya Nair")
                    .withDietPlan(new DietPlan("1800 kcal", "Old notes")).build();
            ObservableList<Person> persons = FXCollections.observableArrayList(original);
            PersonListPanel panel = new PersonListPanel(persons);
            Person replacement = new PersonBuilder(original).withDietPlan(new DietPlan("2000 kcal/day")).build();

            persons.set(0, replacement);
            assertEquals("Target: 2000 kcal/day", label(panel, "dietTarget").getText());
            assertFalse(label(panel, "dietNotes").isVisible());
            assertFalse(label(panel, "dietNotes").isManaged());

            persons.remove(0);
            assertEquals("Select a client to view their current diet plan.",
                    label(panel, "selectedClientName").getText());
            assertFalse(label(panel, "dietTarget").isVisible());
            return null;
        });
    }

    @Test
    public void longValuesRemainAvailableAndWrapAtNarrowWidth() throws Exception {
        runOnFxThread(() -> {
            String target = "Protein".repeat(28); // 196 characters
            String notes = "Vegetables".repeat(30); // 300 characters
            Person client = new PersonBuilder().withDietPlan(new DietPlan(target, notes)).build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(client));
            Region root = panel.getRoot();
            new Scene(root, 340, 600);
            root.applyCss();
            root.resize(340, 600);
            root.layout();

            Label targetLabel = label(panel, "dietTarget");
            Label notesLabel = label(panel, "dietNotes");
            assertEquals("Target: " + target, targetLabel.getText());
            assertEquals("Notes: " + notes, notesLabel.getText());
            assertTrue(targetLabel.isWrapText());
            assertTrue(notesLabel.isWrapText());
            assertTrue(targetLabel.prefHeight(280) > targetLabel.prefHeight(1000));
            assertTrue(notesLabel.prefHeight(280) > notesLabel.prefHeight(1000));
            return null;
        });
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
