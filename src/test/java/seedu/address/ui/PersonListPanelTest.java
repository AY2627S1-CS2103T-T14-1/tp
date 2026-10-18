package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
import seedu.address.testutil.PersonBuilder;

/** Tests client-list counts, filtering, and selection ownership. */
public class PersonListPanelTest {

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
    public void emptyList_showsAddClientState() throws Exception {
        runOnFxThread(() -> {
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList());

            assertEquals("0", label(panel, "shownCount").getText());
            assertEquals("No clients yet", label(panel, "emptyTitle").getText());
            assertEquals("add n/NAME p/PHONE e/EMAIL a/ADDRESS", label(panel, "emptyCommand").getText());
            assertNull(panel.getSelectedPerson());
            return null;
        });
    }

    @Test
    public void populatedList_selectsFirstAndExposesSelection() throws Exception {
        runOnFxThread(() -> {
            Person first = new PersonBuilder().withName("Alice Lee").build();
            Person second = new PersonBuilder().withName("Priya Nair").build();
            PersonListPanel panel = new PersonListPanel(FXCollections.observableArrayList(first, second));

            assertEquals(first, panel.getSelectedPerson());
            panel.selectDisplayedIndex(1);
            assertEquals(second, panel.selectedPersonProperty().get());
            assertEquals(1, panel.getSelectedIndex());
            return null;
        });
    }

    @Test
    public void filteredList_keepsSourceTotalAndReportsVisibleCount() throws Exception {
        runOnFxThread(() -> {
            Person first = new PersonBuilder().withName("Alice Lee").build();
            Person second = new PersonBuilder().withName("Priya Nair").build();
            ObservableList<Person> source = FXCollections.observableArrayList(first, second);
            FilteredList<Person> displayed = new FilteredList<>(source);
            PersonListPanel panel = new PersonListPanel(displayed);

            displayed.setPredicate(person -> person.equals(second));
            assertEquals(2, panel.totalCountProperty().get());
            assertEquals(1, panel.getDisplayedCount());
            assertEquals("1", label(panel, "shownCount").getText());
            assertEquals(second, panel.getSelectedPerson());

            panel.setFilterQuery("Nobody");
            displayed.setPredicate(person -> false);
            assertEquals("No clients match", label(panel, "emptyTitle").getText());
            assertEquals("list", label(panel, "emptyCommand").getText());
            assertNull(panel.getSelectedPerson());
            return null;
        });
    }

    @Test
    public void removalOfSelectedClient_selectsRemainingClient() throws Exception {
        runOnFxThread(() -> {
            Person first = new PersonBuilder().withName("Alice Lee").build();
            Person second = new PersonBuilder().withName("Priya Nair").build();
            ObservableList<Person> people = FXCollections.observableArrayList(first, second);
            PersonListPanel panel = new PersonListPanel(people);

            people.remove(first);

            assertEquals(second, panel.getSelectedPerson());
            assertEquals("1", label(panel, "shownCount").getText());
            return null;
        });
    }

    private static Label label(PersonListPanel panel, String id) {
        prepareForLookup(panel);
        Label result = (Label) panel.getRoot().lookup("#" + id);
        if (result == null) {
            @SuppressWarnings("unchecked")
            ListView<Person> list = (ListView<Person>) panel.getRoot().lookup("#personListView");
            result = (Label) list.getPlaceholder().lookup("#" + id);
        }
        return result;
    }

    private static void prepareForLookup(PersonListPanel panel) {
        Region root = panel.getRoot();
        if (root.getScene() == null) {
            new Scene(root, 300, 600);
        }
        root.applyCss();
        root.layout();
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
