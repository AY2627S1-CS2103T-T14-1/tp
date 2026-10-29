package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests that undo and redo keep the visible search indicator consistent with client indexes. */
public class MainWindowUndoRedoTest {
    @TempDir
    public Path temporaryFolder;

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
    public void undoRedo_afterFind_resetsFilterAndIndicatorTogether() throws Exception {
        runOnFxThread(() -> {
            Person alice = new PersonBuilder().withName("Alice").withRemark("Original").build();
            Person bob = new PersonBuilder().withName("Bob").build();
            Model model = new ModelManager();
            model.addPerson(alice);
            model.addPerson(bob);
            Path data = temporaryFolder.resolve("clients.json");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(data),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), data);
            window.fillInnerParts();
            window.openTerminal();
            try {
                enter(window, "remark 1 r/Changed Alice");
                enter(window, "find Bob");
                assertFilteredBob(window, model, bob);

                enter(window, "undo");
                assertAllClients(window, model);
                assertEquals("Original", model.getAddressBook().getPersonList().get(0).getRemark().value);

                enter(window, "find Bob");
                assertFilteredBob(window, model, bob);
                enter(window, "redo");
                assertAllClients(window, model);
                assertEquals("Changed Alice", model.getAddressBook().getPersonList().get(0).getRemark().value);

                enter(window, "remark 1 r/Target Alice");
                assertEquals("Target Alice", model.getAddressBook().getPersonList().get(0).getRemark().value);
                assertEquals(bob, model.getAddressBook().getPersonList().get(1));
            } finally {
                window.getPrimaryStage().hide();
            }
            return null;
        });
    }

    private static void enter(MainWindow window, String command) {
        window.getPrimaryStage().getScene().getRoot().applyCss();
        TextField field = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
        field.setText(command);
        field.fireEvent(new ActionEvent());
        assertEquals("", field.getText(), "Command failed: " + command);
    }

    private static void assertFilteredBob(MainWindow window, Model model, Person bob) {
        assertEquals(List.of(bob), model.getFilteredPersonList());
        assertEquals(0, window.getPersonListPanel().getSelectedIndex());
        assertEquals(bob, window.getPersonListPanel().getSelectedPerson());
        Label indicator = (Label) window.getPrimaryStage().getScene().lookup("#filterStateText");
        Label count = (Label) window.getPrimaryStage().getScene().lookup("#filterCountText");
        assertEquals("find: Bob", indicator.getText());
        assertEquals("1 of 2", count.getText());
    }

    private static void assertAllClients(MainWindow window, Model model) {
        assertEquals(model.getAddressBook().getPersonList(), model.getFilteredPersonList());
        assertEquals(2, window.getPersonListPanel().getDisplayedCount());
        Label indicator = (Label) window.getPrimaryStage().getScene().lookup("#filterStateText");
        Label count = (Label) window.getPrimaryStage().getScene().lookup("#filterCountText");
        assertEquals("", indicator.getText());
        assertEquals("2 of 2", count.getText());
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
}
