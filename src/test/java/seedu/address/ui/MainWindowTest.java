package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
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

    @Test
    public void keyboard_ignoresModifiedBlankAndControlKeysButTypesIntoOpenTerminal() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = newWindow(new ModelManager(), "keyboard");
            Scene scene = window.getPrimaryStage().getScene();
            typeKey(scene, "h", false, false, false);

            window.fillInnerParts();
            SplitPane workspace = (SplitPane) scene.lookup("#workspace");
            typeKey(scene, "h", true, false, false);
            typeKey(scene, "h", false, true, false);
            typeKey(scene, "h", false, false, true);
            typeKey(scene, "", false, false, false);
            typeKey(scene, " ", false, false, false);
            typeKey(scene, "\u0007", false, false, false);
            pressEscape(scene);
            assertEquals(1, workspace.getItems().size());

            window.openTerminal();
            scene.lookup("#personRows").requestFocus();
            typeKey(scene, "x", false, false, false);
            scene.getRoot().applyCss();
            assertEquals("x", ((TextField) scene.lookup("#commandTextField")).getText());
            assertEquals(2, workspace.getItems().size());
            return null;
        });
    }

    @Test
    public void terminalButtons_openAndCloseAndRepeatedCloseIsHarmless() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = newWindow(new ModelManager(), "buttons");
            window.fillInnerParts();
            Scene scene = window.getPrimaryStage().getScene();
            SplitPane workspace = (SplitPane) scene.lookup("#workspace");
            HBox closedBar = (HBox) scene.lookup("#terminalClosedBar");

            Button open = (Button) scene.lookup(".terminal-btn");
            open.fire();
            assertEquals(2, workspace.getItems().size());
            assertFalse(closedBar.isManaged());

            scene.getRoot().applyCss();
            Button close = (Button) scene.lookup(".icon-btn");
            close.fire();
            assertEquals(1, workspace.getItems().size());
            assertTrue(closedBar.isManaged());
            close.fire();
            assertEquals(1, workspace.getItems().size());

            click(closedBar);
            assertEquals(2, workspace.getItems().size());
            return null;
        });
    }

    @Test
    public void dateToggle_expandsToClockAndCollapsesAgain() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = newWindow(new ModelManager(), "clock");
            window.fillInnerParts();
            Scene scene = window.getPrimaryStage().getScene();
            HBox toggle = (HBox) scene.lookup("#dateToggle");
            Label time = (Label) scene.lookup("#currentTime");
            assertFalse(time.isManaged());
            assertEquals("Click for full date and live time", toggle.getAccessibleText());

            click(toggle);
            assertTrue(time.isManaged());
            assertTrue(time.isVisible());
            assertTrue(time.getText().matches("\\d{2}:\\d{2}:\\d{2}"), time.getText());
            assertEquals("Click to collapse", toggle.getAccessibleText());

            click(toggle);
            assertFalse(time.isManaged());
            assertEquals("Click for full date and live time", toggle.getAccessibleText());
            return null;
        });
    }

    @Test
    public void filterPill_clickClearsActiveFind() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice Lee").build());
            model.addPerson(new PersonBuilder().withName("Priya Nair").build());
            MainWindow window = newWindow(model, "pill");
            window.fillInnerParts();
            window.openTerminal();
            Scene scene = window.getPrimaryStage().getScene();
            HBox pill = (HBox) scene.lookup("#filterPill");
            assertFalse(pill.isManaged());

            execute(scene, "find Alice");
            assertTrue(pill.isManaged());
            assertEquals(1, window.getPersonListPanel().getDisplayedCount());

            click(pill);
            assertFalse(pill.isManaged());
            assertEquals(2, window.getPersonListPanel().getDisplayedCount());
            return null;
        });
    }

    @Test
    public void exitCommand_savesWindowAndTerminalHeightAndHidesWindow() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.setGuiSettings(new GuiSettings(1400, 900, 5, 6, 400));
            MainWindow window = newWindow(model, "exit");
            window.fillInnerParts();
            window.openTerminal();

            execute(window.getPrimaryStage().getScene(), "exit");

            GuiSettings saved = model.getGuiSettings();
            assertTrue(saved.getTerminalHeight() >= 170 && saved.getTerminalHeight() <= 640);
            assertTrue(saved.getWindowWidth() >= 1280);
            assertFalse(window.getPrimaryStage().isShowing());
            return null;
        });
    }

    @Test
    public void closeRequestWhileTerminalClosed_keepsRememberedTerminalHeight() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.setGuiSettings(new GuiSettings(1400, 900, 5, 6, 400));
            MainWindow window = newWindow(model, "close");
            window.fillInnerParts();
            Stage stage = window.getPrimaryStage();

            stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));

            assertEquals(400, model.getGuiSettings().getTerminalHeight());
            return null;
        });
    }

    @Test
    public void help_beforeAndAfterInnerPartsOpensHelpWindow() throws Exception {
        runOnFxThread(() -> {
            MainWindow window = newWindow(new ModelManager(), "help");
            window.handleHelp();

            window.fillInnerParts();
            window.handleHelp();
            Scene scene = window.getPrimaryStage().getScene();
            assertEquals(2, ((SplitPane) scene.lookup("#workspace")).getItems().size());

            execute(scene, "exit");
            return null;
        });
    }

    @Test
    public void failedIndexedCommands_reportErrorsAndKeepSelection() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            Person alice = new PersonBuilder().withName("Alice Lee").build();
            Person priya = new PersonBuilder().withName("Priya Nair").build();
            model.addPerson(alice);
            model.addPerson(priya);
            MainWindow window = newWindow(model, "failed");
            window.fillInnerParts();
            window.openTerminal();
            Scene scene = window.getPrimaryStage().getScene();
            window.getPersonListPanel().selectDisplayedIndex(1);

            for (String command : new String[] {"delete", "delete abc", "delete 99"}) {
                execute(scene, command);
                assertEquals(CommandFeedback.Kind.ERROR, lastFeedback(scene).kind(), command);
                assertEquals(priya, window.getPersonListPanel().getSelectedPerson(), command);
            }
            assertEquals(2, window.getPersonListPanel().getDisplayedCount());
            return null;
        });
    }

    @Test
    public void noOpUndoRedoAndEmptyClearPlan_keepFindFilterUntilPlanIsCleared() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice Lee").build());
            model.addPerson(new PersonBuilder().withName("Daniel Tan").withWorkoutPlan(
                    new WorkoutPlan(List.of(new Exercise("Push up", 3, 12, 0)))).build());
            MainWindow window = newWindow(model, "filter");
            window.fillInnerParts();
            window.openTerminal();
            Scene scene = window.getPrimaryStage().getScene();
            Label filterState = (Label) scene.lookup("#filterStateText");

            execute(scene, "find Alice");
            for (String command : new String[] {"undo", "redo", "clearplan 1"}) {
                execute(scene, command);
                assertEquals("find: Alice", filterState.getText(), command);
            }

            execute(scene, "find Daniel");
            execute(scene, "clearplan 1");
            assertEquals("", filterState.getText());
            assertEquals(2, window.getPersonListPanel().getDisplayedCount());
            return null;
        });
    }

    @Test
    public void addCommand_selectsNewClient() throws Exception {
        runOnFxThread(() -> {
            Model model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Alice Lee").build());
            model.addPerson(new PersonBuilder().withName("Priya Nair").build());
            MainWindow window = newWindow(model, "add");
            window.fillInnerParts();
            window.openTerminal();

            execute(window.getPrimaryStage().getScene(),
                    "add n/Zed Tan p/91234567 e/zed@example.com a/1 Test Street");

            assertEquals("Zed Tan", window.getPersonListPanel().getSelectedPerson().getName().fullName);
            assertEquals(2, window.getPersonListPanel().getSelectedIndex());
            return null;
        });
    }

    private MainWindow newWindow(Model model, String name) {
        JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve(name + "AddressBook.json"));
        Logic logic = new LogicManager(model, new StorageManager(data,
                new JsonUserPrefsStorage(temporaryFolder.resolve(name + "Prefs.json"))));
        return new MainWindow(new Stage(), logic, data.getAddressBookFilePath());
    }

    private static void typeKey(Scene scene, String character, boolean control, boolean alt, boolean meta) {
        scene.getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED,
                false, control, alt, meta));
    }

    private static void pressEscape(Scene scene) {
        scene.getRoot().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                false, false, false, false));
    }

    private static void click(Node node) {
        node.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, false, null));
    }

    private static CommandFeedback lastFeedback(Scene scene) {
        return (CommandFeedback) ((ListView<?>) scene.lookup("#resultList")).getItems().getLast();
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
