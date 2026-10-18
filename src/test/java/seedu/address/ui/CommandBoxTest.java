package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.parser.exceptions.ParseException;

/** Tests command entry, failure retention, history, and contextual hints. */
public class CommandBoxTest {

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
    public void successfulCommand_executesAndClearsField() throws Exception {
        runOnFxThread(() -> {
            List<String> executed = new ArrayList<>();
            CommandBox box = new CommandBox(command -> {
                executed.add(command);
                return new CommandResult("Done");
            });
            TextField field = box.getCommandTextField();
            field.setText("  list  ");
            field.fireEvent(new ActionEvent());

            assertEquals(List.of("list"), executed);
            assertEquals("", field.getText());
            return null;
        });
    }

    @Test
    public void failedCommand_remainsEditableAndIsKeptInHistory() throws Exception {
        runOnFxThread(() -> {
            CommandBox box = new CommandBox(command -> {
                if (command.equals("bad")) {
                    throw new ParseException("Invalid command");
                }
                return new CommandResult("Done");
            });
            TextField field = box.getCommandTextField();
            field.setText("bad");
            field.fireEvent(new ActionEvent());
            assertEquals("bad", field.getText());

            field.setText("list");
            field.fireEvent(new ActionEvent());
            field.fireEvent(keyPress(KeyCode.UP));
            assertEquals("list", field.getText());
            field.fireEvent(keyPress(KeyCode.UP));
            assertEquals("bad", field.getText());
            field.fireEvent(keyPress(KeyCode.DOWN));
            assertEquals("list", field.getText());
            return null;
        });
    }

    @Test
    public void commandPrefix_updatesUsageHintsAndHintIsClickable() throws Exception {
        runOnFxThread(() -> {
            CommandBox box = new CommandBox(command -> new CommandResult("Done"));
            TextField field = box.getCommandTextField();
            field.setText("sched");
            FlowPane hints = (FlowPane) box.getRoot().lookup("#hintContainer");

            assertEquals(1, hints.getChildren().size());
            Button hint = (Button) hints.getChildren().get(0);
            hint.fire();
            assertEquals("schedule ", field.getText());
            return null;
        });
    }

    private static KeyEvent keyPress(KeyCode keyCode) {
        return new KeyEvent(KeyEvent.KEY_PRESSED, "", "", keyCode,
                false, false, false, false);
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
