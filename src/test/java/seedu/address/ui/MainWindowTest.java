package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
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

            Scene scene = stage.getScene();
            assertNotNull(scene);
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
            assertEquals("1 record", ((Label) scene.lookup("#paymentSummary")).getText());
            return null;
        });
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
