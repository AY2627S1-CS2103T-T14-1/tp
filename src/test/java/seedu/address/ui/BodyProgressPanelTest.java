package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import seedu.address.logic.LogicManager;
import seedu.address.logic.commands.BodyProgressViewRequest;
import seedu.address.model.ModelManager;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.BodyProgressEntry;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

class BodyProgressPanelTest {
    @TempDir
    private Path directory;

    @BeforeAll
    static void startJavaFx() throws Exception {
        ClientDetailPanelTest.startJavaFx();
    }

    @Test
    void panel_completeHistorySignedComparisonsAndStaleFieldClearing() throws Exception {
        onFx(() -> {
            BodyProgressPanel panel = new BodyProgressPanel();
            Region root = panel.getRoot();
            new Scene(root, 580, 240);
            assertFalse(root.isManaged());
            for (String weight : List.of("74.20", "76.80", "75.50")) {
                panel.showPerson(person(weight, "Measured before breakfast"));
                root.applyCss();
                root.layout();
                assertEquals(2, ((VBox) root.lookup("#historyRows")).getChildren().size());
                String comparison = label(root, "comparison").getText();
                String expected = weight.equals("74.20") ? "-1.30" : weight.equals("76.80") ? "+1.30" : "0.00";
                assertTrue(comparison.contains("Change: " + expected + " kg"), comparison);
                assertTrue(comparison.contains("Latest: 2020-01-15"));
                assertTrue(label(root, "baseline").getText().contains("175.00 cm"));
                assertTrue(((Label) ((VBox) root.lookup("#historyRows")).getChildren().getFirst())
                        .getText().contains("2020-01-08"));
            }
            Person baselineOnly = new PersonBuilder().withBodyProgress(
                    person("74.20", null).getBodyProgress().withoutEntry(LocalDate.of(2020, 1, 8))
                            .withoutEntry(LocalDate.of(2020, 1, 15))).build();
            panel.showPerson(baselineOnly);
            assertTrue(label(root, "comparison").getText().contains("Use bodylog before comparing."));
            panel.showPerson(new PersonBuilder().withName("Daniel Tan").build());
            assertTrue(label(root, "baseline").getText().contains("Daniel Tan"));
            assertFalse(label(root, "comparison").getText().contains("74.20"));
            panel.showPerson(null);
            assertEquals("", label(root, "baseline").getText());
            assertEquals("", label(root, "comparison").getText());
            assertTrue(((VBox) root.lookup("#historyRows")).getChildren().isEmpty());
            assertFalse(root.isManaged());
            return null;
        });
    }

    @Test
    void longNotes_wrapWithoutTruncationAndComparisonCanBeRevealed() throws Exception {
        String note = "Measurements ".repeat(23).substring(0, 299) + "X";
        BodyProgressPanel panel = onFx(() -> {
            BodyProgressPanel value = new BodyProgressPanel();
            new Scene(value.getRoot(), 580, 240);
            Person person = person("74.20", note);
            value.showPerson(person);
            value.getRoot().applyCss();
            value.getRoot().layout();
            VBox rows = (VBox) value.getRoot().lookup("#historyRows");
            Label row = (Label) rows.getChildren().getFirst();
            assertTrue(row.getText().endsWith(note));
            assertTrue(row.isWrapText());
            assertTrue(row.getHeight() > 30);
            assertTrue(row.getWidth() <= 580);
            value.reveal(new BodyProgressViewRequest(person, BodyProgressViewRequest.Section.COMPARISON));
            return value;
        });
        onFx(() -> {
            assertEquals(panel.getRevealTarget(new BodyProgressViewRequest(
                    person("74.20", note), BodyProgressViewRequest.Section.COMPARISON)),
                    panel.getRoot().lookup("#comparisonHeading"));
            return null;
        });
    }

    @Test
    void revealTarget_withoutBaselineIsSummary_withBaselineFollowsRequestedSection() throws Exception {
        onFx(() -> {
            BodyProgressPanel panel = new BodyProgressPanel();
            Person unrecorded = new PersonBuilder().withName("Daniel Tan").build();
            panel.showPerson(unrecorded);
            assertEquals(label(panel.getRoot(), "baseline"), panel.getRevealTarget(
                    new BodyProgressViewRequest(unrecorded, BodyProgressViewRequest.Section.HISTORY)));

            Person recorded = person("74.20", "Measured before breakfast");
            panel.showPerson(recorded);
            assertEquals(panel.getRoot().lookup("#historyHeading"), panel.getRevealTarget(
                    new BodyProgressViewRequest(recorded, BodyProgressViewRequest.Section.HISTORY)));
            assertEquals(panel.getRoot().lookup("#comparisonHeading"), panel.getRevealTarget(
                    new BodyProgressViewRequest(recorded, BodyProgressViewRequest.Section.COMPARISON)));
            return null;
        });
    }

    @Test
    void narrowContextColumn_showsCompleteSummaryAndComparison() throws Exception {
        onFx(() -> {
            BodyProgressPanel panel = new BodyProgressPanel();
            panel.showPerson(person("74.20", "Measured before breakfast"));
            Scene scene = new Scene(panel.getRoot(), 340, 900);
            scene.getStylesheets().add(getClass().getResource("/view/FittixTheme.css").toExternalForm());
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            for (String id : List.of("baseline", "comparison")) {
                Label value = label(panel.getRoot(), id);
                Text rendered = (Text) value.lookup(".text");
                assertEquals(value.getText(), rendered.getText(), id + " should not be ellipsised");
                assertTrue(rendered.getLayoutBounds().getHeight() <= value.getHeight(), id);
            }
            return null;
        });
    }

    @Test
    void fullWindow_commandsSelectAfterSaveKeepFilterAndRestoreOnFailure() throws Exception {
        Stage rendered = onFx(() -> {
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().withName("Daniel Tan").build());
            model.addPerson(person("74.20", "Measured before breakfast"));
            boolean[] failSave = {false};
            Clock clock = Clock.fixed(Instant.parse("2020-02-01T12:00:00Z"), ZoneOffset.UTC);
            JsonAddressBookStorage storage = new JsonAddressBookStorage(directory.resolve("book.json"), clock) {
                @Override
                protected void replaceAddressBookFile(Path temporaryFile, Path destination) throws IOException {
                    if (failSave[0]) {
                        throw new IOException("Injected UI save failure");
                    }
                    super.replaceAddressBookFile(temporaryFile, destination);
                }
            };
            LogicManager logic = new LogicManager(model, new StorageManager(storage,
                    new JsonUserPrefsStorage(directory.resolve("prefs.json"))), clock);
            Stage stage = new Stage();
            MainWindow window = new MainWindow(stage, logic, storage.getAddressBookFilePath());
            window.fillInnerParts();
            window.openTerminal();
            stage.setWidth(1280);
            stage.setHeight(800);
            stage.show();
            Scene scene = stage.getScene();
            scene.getRoot().resize(1280, 800);
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            TextField input = (TextField) scene.lookup("#commandTextField");
            run(input, "bodyhistory 2");
            assertEquals("Amy Bee", window.getPersonListPanel().getSelectedPerson().getName().fullName);
            assertEquals("Progress history (2)", ((Label) scene.lookup("#historyHeading")).getText());
            run(input, "find Amy");
            run(input, "bodylog 1 d/2020-01-22 w/74 n/Walk 5 km/day");
            assertEquals(1, model.getFilteredPersonList().size());
            assertTrue(((Label) scene.lookup("#comparison")).getText().contains("-1.50 kg"));
            Person before = window.getPersonListPanel().getSelectedPerson();
            run(input, "bodycompare 2");
            assertEquals(before, window.getPersonListPanel().getSelectedPerson());
            failSave[0] = true;
            run(input, "bodylog 1 d/2020-01-22 w/72");
            assertEquals(before, window.getPersonListPanel().getSelectedPerson());
            assertTrue(((Label) scene.lookup("#comparison")).getText().contains("-1.50 kg"));
            failSave[0] = false;
            run(input, "bodydelete 1 d/2020-01-22");
            assertTrue(((Label) scene.lookup("#comparison")).getText().contains("-1.30 kg"));
            run(input, "list");
            window.getPersonListPanel().selectDisplayedIndex(0);
            Person daniel = window.getPersonListPanel().getSelectedPerson();
            failSave[0] = true;
            run(input, "bodylog 2 d/2020-01-22 w/72");
            assertEquals(daniel, window.getPersonListPanel().getSelectedPerson());
            failSave[0] = false;
            run(input, "find Amy");
            run(input, "bodycompare 1");
            scene.getRoot().applyCss();
            scene.getRoot().layout();
            return stage;
        });
        onFx(() -> {
            assertEquals(1280, rendered.getWidth());
            assertEquals(800, rendered.getHeight());
            TextField input = (TextField) rendered.getScene().lookup("#commandTextField");
            assertTrue(input.localToScene(input.getBoundsInLocal()).getMaxY() <= rendered.getScene().getHeight(),
                    "The command field must remain visible at the minimum window size.");
            assertBalancedColumns(rendered.getScene());
            writeScreenshot(rendered.getScene(), Path.of("build/reports/body-progress-ui.png"));
            rendered.setWidth(1440);
            rendered.setHeight(980);
            rendered.getScene().getRoot().applyCss();
            rendered.getScene().getRoot().layout();
            assertBalancedColumns(rendered.getScene());
            rendered.close();
            return null;
        });
    }

    @Test
    void commandCatalog_includesAllBodyCommands() {
        for (String word : List.of("bodybase", "bodylog", "bodyhistory", "bodycompare", "bodydelete")) {
            assertTrue(CommandCatalog.matching(word).stream().anyMatch(entry -> entry.word().equals(word)));
            assertTrue(CommandCatalog.helpDetails().stream().anyMatch(usage -> usage.startsWith(word + " ")));
        }
    }

    private static Person person(String latestWeight, String notes) {
        BodyBaseline baseline = new BodyBaseline(LocalDate.of(2020, 1, 1),
                new BigDecimal("75.5"), new BigDecimal("175"));
        BodyProgress progress = new BodyProgress(Optional.of(baseline), List.of(
                new BodyProgressEntry(LocalDate.of(2020, 1, 15), new BigDecimal(latestWeight), Optional.empty()),
                new BodyProgressEntry(LocalDate.of(2020, 1, 8), new BigDecimal("74.8"), Optional.ofNullable(notes))));
        return new PersonBuilder().withBodyProgress(progress).build();
    }

    private static Label label(Region root, String id) {
        return (Label) root.lookup("#" + id);
    }

    private static void assertBalancedColumns(Scene scene) {
        Node list = scene.lookup(".client-list-card");
        Node plan = scene.lookup(".workout-card");
        Node payments = scene.lookup(".payments-card");
        double listBottom = list.localToScene(list.getBoundsInLocal()).getMaxY();
        double planBottom = plan.localToScene(plan.getBoundsInLocal()).getMaxY();
        double paymentsBottom = payments.localToScene(payments.getBoundsInLocal()).getMaxY();
        assertEquals(listBottom, planBottom, 1, "The plan should reach the list column's bottom.");
        assertEquals(listBottom, paymentsBottom, 1, "Payments should reach the list column's bottom.");
    }

    private static void run(TextField field, String command) {
        field.setText(command);
        field.fireEvent(new ActionEvent());
    }

    private static void writeScreenshot(Scene scene, Path path) throws IOException {
        WritableImage image = scene.snapshot(null);
        BufferedImage output = new BufferedImage((int) image.getWidth(), (int) image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < output.getHeight(); y++) {
            for (int x = 0; x < output.getWidth(); x++) {
                output.setRGB(x, y, image.getPixelReader().getArgb(x, y));
            }
        }
        Files.createDirectories(path.getParent());
        ImageIO.write(output, "png", path.toFile());
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(20, TimeUnit.SECONDS);
    }
}
