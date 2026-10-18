package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import seedu.address.logic.commands.CommandResult;

/** Tests structured command feedback and bounded result history. */
public class ResultDisplayTest {

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
    public void results_areParsedIntoStatusMessageAndDetails() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.appendResult("list", new CommandResult("Listed all clients.\n2 clients shown"));
            display.appendError("oops", "Unknown command\nType help for the command list");

            CommandFeedback success = display.getFeedbackEntries().get(0);
            assertEquals(CommandFeedback.Kind.SUCCESS, success.kind());
            assertEquals("Listed all clients.", success.message());
            assertEquals(List.of("2 clients shown"), success.details());
            CommandFeedback error = display.getFeedbackEntries().get(1);
            assertEquals(CommandFeedback.Kind.ERROR, error.kind());
            assertEquals(List.of("Type help for the command list"), error.details());
            return null;
        });
    }

    @Test
    public void helpResult_includesRealCommandCatalog() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            display.appendResult("help", new CommandResult("Opened help.", true, false));

            CommandFeedback feedback = display.getFeedbackEntries().get(0);
            assertEquals(CommandFeedback.Kind.INFO, feedback.kind());
            assertTrue(feedback.details().stream().anyMatch(line -> line.startsWith("schedule")));
            assertTrue(feedback.details().stream().anyMatch(line -> line.startsWith("dayex")));
            return null;
        });
    }

    @Test
    public void resultHistory_keepsMostRecentFiftyEntries() throws Exception {
        runOnFxThread(() -> {
            ResultDisplay display = new ResultDisplay();
            for (int i = 0; i < 55; i++) {
                display.appendInfo("command " + i, "message " + i, List.of());
            }

            assertEquals(50, display.getFeedbackEntries().size());
            assertEquals("command 5", display.getFeedbackEntries().get(0).command());
            assertEquals("command 54", display.getFeedbackEntries().get(49).command());
            return null;
        });
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
