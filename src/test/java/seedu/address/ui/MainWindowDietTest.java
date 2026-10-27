package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.DietCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.person.DietPlan;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Tests that a diet-prefix typo reports an error without clearing visible or saved coaching notes. */
public class MainWindowDietTest {
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
    public void diet_mistypedNotesPrefix_preservesDisplayedAndSavedPlanAndAllowsRetry() throws Exception {
        FutureTask<Void> task = new FutureTask<>(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            Path data = temporaryFolder.resolve("clients.json");
            JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(data);
            StorageManager storage = new StorageManager(addressBookStorage,
                    new JsonUserPrefsStorage(temporaryFolder.resolve("prefs.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), data);
            window.fillInnerParts();
            try {
                window.getPrimaryStage().getScene().getRoot().applyCss();
                window.getPrimaryStage().getScene().getRoot().layout();
                TextField input = (TextField) window.getPrimaryStage().getScene().lookup("#commandTextField");
                input.setText("diet 1 t/1800 kcal n/Keep existing notes");
                input.fireEvent(new ActionEvent());
                assertEquals("", input.getText());
                byte[] previousFile = Files.readAllBytes(data);

                String rejected = "diet 1 t/2000 kcal nn/Include vegetables";
                input.setText(rejected);
                input.fireEvent(new ActionEvent());
                assertEquals(rejected, input.getText());
                ListView<?> results = (ListView<?>) window.getPrimaryStage().getScene().lookup("#resultList");
                CommandFeedback feedback = (CommandFeedback) results.getItems().getLast();
                assertEquals(CommandFeedback.Kind.ERROR, feedback.kind());
                assertEquals(DietCommand.MESSAGE_INVALID_FORMAT, feedback.message());
                assertEquals("1800 kcal", label(window, "dietTarget").getText());
                assertEquals("Keep existing notes", label(window, "dietNotes").getText());
                assertArrayEquals(previousFile, Files.readAllBytes(data));
                assertEquals(new DietPlan("1800 kcal", "Keep existing notes"),
                        addressBookStorage.readAddressBook().orElseThrow().getPersonList().getFirst()
                                .getDietPlan().orElseThrow());

                input.setText("diet 1 t/2000 kcal/day n/Include vegetables");
                input.fireEvent(new ActionEvent());
                assertEquals("", input.getText());
                assertEquals("2000 kcal/day", label(window, "dietTarget").getText());
                assertEquals("Include vegetables", label(window, "dietNotes").getText());
                assertEquals(new DietPlan("2000 kcal/day", "Include vegetables"),
                        addressBookStorage.readAddressBook().orElseThrow().getPersonList().getFirst()
                                .getDietPlan().orElseThrow());
            } finally {
                window.getPrimaryStage().hide();
            }
            return null;
        });
        Platform.runLater(task);
        task.get(20, TimeUnit.SECONDS);
    }

    private static Label label(MainWindow window, String id) {
        return (Label) window.getPrimaryStage().getScene().lookup("#" + id);
    }
}
