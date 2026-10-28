package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests main-window command routing for indexed payment commands. */
public class MainWindowTest {

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
    public void payCommand_keepsTargetSelectedAndRefreshesPaymentRows() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person first = new PersonBuilder().withName("Alice Lee").build();
            Person second = new PersonBuilder().withName("Priya Nair").build();
            model.addPerson(first);
            model.addPerson(second);
            JsonAddressBookStorage addressBookStorage =
                    new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
            Logic logic = new LogicManager(model, new StorageManager(addressBookStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"))));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, addressBookStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.openTerminal();

            Scene scene = stage.getScene();
            assertNotNull(scene);
            scene.getRoot().applyCss();
            TextField commandField = (TextField) scene.lookup("#commandTextField");
            commandField.setText("pay 2 a/150 d/2026-10-20");
            commandField.fireEvent(new ActionEvent());

            PaymentRecord expectedPayment =
                    PaymentRecord.fromText("150", "2026-10-20", Optional.empty());
            assertEquals(second.withAddedPayment(expectedPayment),
                    window.getPersonListPanel().getSelectedPerson());
            assertEquals(1, window.getPersonListPanel().getSelectedIndex());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            VBox paymentRows = (VBox) scene.lookup("#paymentRows");
            assertEquals(1, paymentRows.getChildren().size());
            assertFalse(((Label) scene.lookup("#paymentEmpty")).isManaged());
            return null;
        });
    }

    @Test
    public void undoAndRedo_restorePaidClientSelectionAndPaymentRowsAndResetFindFilter() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice Lee").build();
            Person priya = new PersonBuilder().withName("Priya Nair").build();
            model.addPerson(alice);
            model.addPerson(priya);
            JsonAddressBookStorage addressBookStorage =
                    new JsonAddressBookStorage(temporaryFolder.resolve("undoAddressBook.json"));
            Logic logic = new LogicManager(model, new StorageManager(addressBookStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("undoUserPrefs.json"))));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, addressBookStorage.getAddressBookFilePath());
            window.fillInnerParts();
            window.openTerminal();
            Scene scene = stage.getScene();

            execute(scene, "find Alice");
            assertEquals(1, window.getPersonListPanel().getDisplayedCount());
            assertEquals("find: Alice", ((Label) scene.lookup("#filterStateText")).getText());

            PaymentRecord payment = PaymentRecord.fromText("150", "2026-10-20", Optional.empty());
            execute(scene, "pay 1 a/150 d/2026-10-20");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertEquals(1, ((VBox) scene.lookup("#paymentRows")).getChildren().size());

            execute(scene, "undo");
            assertEquals(2, window.getPersonListPanel().getDisplayedCount());
            assertEquals("", ((Label) scene.lookup("#filterStateText")).getText());
            assertEquals(alice, window.getPersonListPanel().getSelectedPerson());
            assertEquals(0, ((VBox) scene.lookup("#paymentRows")).getChildren().size());
            assertTrue(((Label) scene.lookup("#paymentEmpty")).isManaged());

            execute(scene, "redo");
            assertEquals(alice.withAddedPayment(payment),
                    window.getPersonListPanel().getSelectedPerson());
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            assertEquals(1, ((VBox) scene.lookup("#paymentRows")).getChildren().size());
            assertFalse(((Label) scene.lookup("#paymentEmpty")).isManaged());
            assertEquals("", ((Label) scene.lookup("#filterStateText")).getText());
            return null;
        });
    }

    @Test
    public void terminal_opensFromTypingAndClosesWithEscape() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            JsonAddressBookStorage data = new JsonAddressBookStorage(
                    temporaryFolder.resolve("terminalAddressBook.json"));
            Logic logic = new LogicManager(model, new StorageManager(data,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("terminalPrefs.json"))));
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, data.getAddressBookFilePath());
            window.fillInnerParts();
            Scene scene = stage.getScene();
            SplitPane workspace = (SplitPane) scene.lookup("#workspace");
            HBox closed = (HBox) scene.lookup("#terminalClosedBar");
            assertEquals(1, workspace.getItems().size());
            assertTrue(closed.isManaged());

            scene.getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_TYPED, "h", "h", KeyCode.UNDEFINED,
                    false, false, false, false));
            assertEquals(2, workspace.getItems().size());
            assertFalse(closed.isManaged());
            scene.getRoot().applyCss();
            Node divider = workspace.lookup(".split-pane-divider");
            assertNotNull(divider);
            Region grip = (Region) divider.lookup(".vertical-grabber");
            assertNotNull(grip);
            assertEquals(48, grip.getPrefWidth());
            assertEquals(4, grip.getPrefHeight());
            assertEquals(Color.web("#d4f25a"), grip.getBackground().getFills().getFirst().getFill());
            TextField command = (TextField) scene.lookup("#commandTextField");
            assertEquals("h", command.getText());
            command.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false));
            assertEquals(1, workspace.getItems().size());
            assertTrue(closed.isManaged());
            scene.getRoot().applyCss();
            assertNull(workspace.lookup(".vertical-grabber"));
            return null;
        });
    }

    private static void execute(Scene scene, String commandText) {
        scene.getRoot().applyCss();
        TextField commandField = (TextField) scene.lookup("#commandTextField");
        commandField.setText(commandText);
        commandField.fireEvent(new ActionEvent());
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
