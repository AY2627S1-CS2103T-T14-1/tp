package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.LogicManager;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Drives a shown main window with layout pulses between steps, so that focus, selection and layout
 * timing behave as they do for native input.
 */
public class MainWindowShownTest {

    @TempDir
    public Path temporaryFolder;

    private MainWindow window;

    @BeforeAll
    public static void startJavaFx() throws Exception {
        MainWindowTest.startJavaFx();
    }

    @AfterEach
    public void hideWindow() throws Exception {
        if (window != null) {
            runOnFxThread(() -> {
                window.getPrimaryStage().hide();
                return null;
            });
        }
    }

    @Test
    public void typeToOpen_keepsFirstCharacterWhenTypingContinues() throws Exception {
        showWindow(new ModelManager());

        runOnFxThread(() -> type("l"));
        waitForFx();
        runOnFxThread(() -> {
            TextField field = commandField();
            assertEquals("l", field.getText());
            assertEquals(0, field.getSelection().getLength());
            assertEquals(1, field.getCaretPosition());
            return type("ist");
        });
        waitForFx();
        runOnFxThread(() -> {
            assertEquals("list", commandField().getText());
            press(KeyCode.ENTER);
            assertEquals("list", lastFeedback().command());
            assertEquals(CommandFeedback.Kind.SUCCESS, lastFeedback().kind());
            return null;
        });
    }

    @Test
    public void typingAfterClickingClient_withTerminalOpen_goesToCommandField() throws Exception {
        Model model = new ModelManager();
        model.addPerson(new PersonBuilder().withName("Alice Lee").build());
        model.addPerson(new PersonBuilder().withName("Priya Nair").build());
        showWindow(model);

        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        runOnFxThread(() -> {
            click(personRows().getChildren().get(1));
            assertEquals(personRows(), scene().getFocusOwner());
            return type("l");
        });
        waitForFx();
        runOnFxThread(() -> type("ist"));
        waitForFx();
        runOnFxThread(() -> {
            assertEquals("list", commandField().getText());
            assertEquals(commandField(), scene().getFocusOwner());
            return null;
        });
    }

    @Test
    public void typing_withButtonFocused_ignoresSpaceButOpensForLetters() throws Exception {
        showWindow(new ModelManager());

        runOnFxThread(() -> {
            Button help = (Button) scene().lookup(".help-pill");
            help.requestFocus();
            type(" ");
            assertEquals(1, workspace().getItems().size());
            return type("h");
        });
        waitForFx();
        runOnFxThread(() -> {
            assertEquals(2, workspace().getItems().size());
            assertEquals("h", commandField().getText());
            return null;
        });
    }

    @Test
    public void terminalHeight_isKeptAcrossRenderedCloseAndReopen() throws Exception {
        showWindow(new ModelManager());

        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        runOnFxThread(() -> {
            workspace().setDividerPositions(0.45);
            return null;
        });
        waitForFx();
        double dragged = runOnFxThread(this::terminalHeight);
        assertTrue(dragged >= 170 && dragged <= 640, "dragged height " + dragged);

        runOnFxThread(() -> {
            terminalCloseButton().fire();
            return null;
        });
        waitForFx();
        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        assertEquals(dragged, runOnFxThread(this::terminalHeight), 1.0);
    }

    @Test
    public void terminalHeight_isRestoredFromSavedSettings() throws Exception {
        Model model = new ModelManager();
        model.setGuiSettings(new GuiSettings(1280, 800, 0, 0, 434));
        showWindow(model);

        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        assertEquals(434, runOnFxThread(this::terminalHeight), 1.0);
    }

    @Test
    public void terminalHeight_survivesExitSavedPreferencesAndRelaunch() throws Exception {
        Model model = new ModelManager();
        showWindow(model);
        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        runOnFxThread(() -> {
            workspace().setDividerPositions(0.42);
            return null;
        });
        waitForFx();
        double dragged = runOnFxThread(this::terminalHeight);
        runOnFxThread(() -> {
            window.getPrimaryStage().fireEvent(
                    new WindowEvent(window.getPrimaryStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
            return null;
        });

        JsonUserPrefsStorage prefs = new JsonUserPrefsStorage(temporaryFolder.resolve("savedPrefs.json"));
        prefs.saveUserPrefs(model.getUserPrefs());
        UserPrefs relaunchPrefs = prefs.readUserPrefs().orElseThrow();
        assertEquals(dragged, relaunchPrefs.getGuiSettings().getTerminalHeight(), 1.0);

        showWindow(new ModelManager(new AddressBook(), relaunchPrefs));
        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        assertEquals(dragged, runOnFxThread(this::terminalHeight), 1.0);
    }

    @Test
    public void terminalHeight_isKeptWhenWindowResizes() throws Exception {
        Model model = new ModelManager();
        model.setGuiSettings(new GuiSettings(1280, 800, 0, 0, 300));
        showWindow(model);

        runOnFxThread(() -> {
            window.openTerminal();
            return null;
        });
        waitForFx();
        runOnFxThread(() -> {
            window.getPrimaryStage().setHeight(window.getPrimaryStage().getHeight() - 60);
            return null;
        });
        waitForFx();
        assertEquals(300, runOnFxThread(this::terminalHeight), 1.0);
    }

    private void showWindow(Model model) throws Exception {
        window = runOnFxThread(() -> {
            JsonAddressBookStorage data = new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
            Logic logic = new LogicManager(model, new StorageManager(data,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json"))));
            MainWindow shown = new MainWindow(new Stage(), logic, data.getAddressBookFilePath());
            // The packaged JavaFX 25 runtime selects a text field's contents when it gains focus other than
            // by a mouse click. The JavaFX 17 test runtime does not, so emulate it here.
            shown.getPrimaryStage().getScene().focusOwnerProperty().addListener((unused, previous, current) -> {
                if (current instanceof TextField field) {
                    field.selectRange(field.getLength(), 0);
                }
            });
            shown.fillInnerParts();
            shown.show();
            return shown;
        });
        waitForFx();
    }

    private Scene scene() {
        return window.getPrimaryStage().getScene();
    }

    private SplitPane workspace() {
        return (SplitPane) scene().lookup("#workspace");
    }

    private TextField commandField() {
        scene().getRoot().applyCss();
        return (TextField) scene().lookup("#commandTextField");
    }

    private VBox personRows() {
        return (VBox) scene().lookup("#personRows");
    }

    private Button terminalCloseButton() {
        scene().getRoot().applyCss();
        return (Button) scene().lookup(".icon-btn");
    }

    private double terminalHeight() {
        return ((Region) scene().lookup("#terminalPanel")).getHeight();
    }

    private CommandFeedback lastFeedback() {
        return (CommandFeedback) ((ListView<?>) scene().lookup("#resultList")).getItems().getLast();
    }

    /** Sends each character to the focus owner, as native key input is delivered. */
    private Void type(String text) {
        for (char c : text.toCharArray()) {
            String character = String.valueOf(c);
            focusTarget().fireEvent(new KeyEvent(KeyEvent.KEY_TYPED, character, character, KeyCode.UNDEFINED,
                    false, false, false, false));
        }
        return null;
    }

    private void press(KeyCode code) {
        focusTarget().fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private Node focusTarget() {
        Node owner = scene().getFocusOwner();
        return owner == null ? scene().getRoot() : owner;
    }

    private static void click(Node node) {
        node.fireEvent(new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false, true, false, false, null));
    }

    /** Lets queued runLater work and several layout pulses complete. */
    private static void waitForFx() throws Exception {
        for (int i = 0; i < 6; i++) {
            Thread.sleep(40);
            runOnFxThread(() -> null);
        }
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
