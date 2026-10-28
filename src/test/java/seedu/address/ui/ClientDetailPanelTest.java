package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

/** Tests the redesigned centre client profile and workout-plan panel. */
public class ClientDetailPanelTest {

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
    public void noSelection_showsDetailPlaceholder() throws Exception {
        runOnFxThread(() -> {
            ClientDetailPanel panel = new ClientDetailPanel();

            assertFalse(region(panel, "selectedContent").isManaged());
            assertTrue(region(panel, "emptyContent").isManaged());
            return null;
        });
    }

    @Test
    public void person_showsProfileGoalRemarkAndCommandIndex() throws Exception {
        runOnFxThread(() -> {
            Person person = new PersonBuilder().withName("Alex Yeoh").withPhone("87438807")
                    .withEmail("alex@example.com").withAddress("30 Geylang Street")
                    .withGoal("Build strength").withRemark("Prefers morning sessions")
                    .withTags("strength", "beginner").build();
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(person, 3);

            assertEquals("3", label(panel, "profileIndex").getText());
            assertEquals("Alex Yeoh", label(panel, "clientName").getText());
            assertEquals("87438807", label(panel, "phone").getText());
            assertEquals("alex@example.com", label(panel, "email").getText().replace("\u200b", ""));
            assertEquals("30 Geylang Street", label(panel, "address").getText());
            assertEquals("Build strength", label(panel, "goal").getText());
            assertEquals("Prefers morning sessions", label(panel, "remark").getText());
            assertTrue(region(panel, "remarkBox").isManaged());
            assertTrue(label(panel, "clientName").isWrapText());
            return null;
        });
    }

    @Test
    public void healthProfile_showsOnlyWhenRecorded() throws Exception {
        runOnFxThread(() -> {
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withHealthProfile("Avoid jumping").build(), 1);
            assertTrue(region(panel, "healthProfileBox").isManaged());
            assertEquals("Avoid jumping", label(panel, "healthProfile").getText());

            panel.showPerson(new PersonBuilder().build(), 2);
            assertFalse(region(panel, "healthProfileBox").isManaged());
            return null;
        });
    }

    @Test
    public void workoutPlan_preservesDisplayIndexesAndUnassignedGroup() throws Exception {
        runOnFxThread(() -> {
            WorkoutPlan plan = new WorkoutPlan(List.of(
                    new Exercise("Back squat", 4, 8, 80, "Monday"),
                    new Exercise("Push up", 3, 12, 0),
                    new Exercise("Bench press", 3, 10, 40.25, "Wednesday")));
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withWorkoutPlan(plan).build(), 1);
            VBox rows = vbox(panel, "workoutRows");
            List<String> text = labelTexts(rows);

            assertEquals(8, rows.getChildren().size());
            assertTrue(text.containsAll(List.of("MON", "Back squat", "1", "4 × 8", "80 kg",
                    "WED", "Bench press", "2", "3 × 10", "40.25 kg",
                    "UNSET", "Push up", "3", "3 × 12", "BW")));
            assertEquals("3 exercises · 2 training days", label(panel, "planSummary").getText());
            return null;
        });
    }

    @Test
    public void emptyPlan_keepsBlankCardBody() throws Exception {
        runOnFxThread(() -> {
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().build(), 2);

            assertTrue(region(panel, "workoutEmpty").isManaged());
            assertFalse(region(panel, "workoutRows").isManaged());
            assertEquals("", label(panel, "planSummary").getText());
            assertEquals("—", label(panel, "goal").getText());
            assertFalse(region(panel, "remarkBox").isManaged());
            return null;
        });
    }

    @Test
    public void payments_sortedByDateWithStableSameDateOrderAndDuplicates() throws Exception {
        runOnFxThread(() -> {
            PaymentRecord latest = PaymentRecord.fromText("150", "2026-10-20", Optional.empty());
            PaymentRecord firstOnDate = PaymentRecord.fromText("75.50", "2026-10-19", Optional.of("First"));
            PaymentRecord duplicate = PaymentRecord.fromText("75.50", "2026-10-19", Optional.of("First"));
            PaymentRecord secondOnDate = PaymentRecord.fromText("75.50", "2026-10-19", Optional.of("Second"));
            Person person = new PersonBuilder()
                    .withPaymentRecords(List.of(latest, firstOnDate, duplicate, secondOnDate))
                    .build();
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(person, 1);

            List<String> text = labelTexts(vbox(panel, "paymentRows"));
            assertEquals(List.of(
                    "2026-10-19", "75.50", "First",
                    "2026-10-19", "75.50", "First",
                    "2026-10-19", "75.50", "Second",
                    "2026-10-20", "150.00"), text);
            assertEquals("4 records", label(panel, "paymentSummary").getText());
            assertTrue(region(panel, "paymentRows").isManaged());
            assertFalse(region(panel, "paymentEmpty").isManaged());
            return null;
        });
    }

    @Test
    public void payments_emptyStateClearsPreviousRows() throws Exception {
        runOnFxThread(() -> {
            PaymentRecord payment = PaymentRecord.fromText("50", "2026-01-01", Optional.empty());
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(payment)).build(), 1);
            assertEquals(1, vbox(panel, "paymentRows").getChildren().size());
            assertFalse(region(panel, "paymentEmpty").isManaged());

            panel.showPerson(new PersonBuilder().withName("No Payments").build(), 2);

            assertEquals(0, vbox(panel, "paymentRows").getChildren().size());
            assertFalse(region(panel, "paymentRows").isManaged());
            assertTrue(region(panel, "paymentEmpty").isManaged());
            assertEquals("", label(panel, "paymentSummary").getText());
            return null;
        });
    }

    @Test
    public void payments_singularSummaryAndVariedAmountsShowExactDateAndTwoDecimals() throws Exception {
        runOnFxThread(() -> {
            PaymentRecord single = PaymentRecord.fromText("7.5", "2024-02-29", Optional.empty());
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(single)).build(), 1);

            assertEquals("1 record", label(panel, "paymentSummary").getText());
            assertEquals(List.of("2024-02-29", "7.50"), labelTexts(vbox(panel, "paymentRows")));

            PaymentRecord whole = PaymentRecord.fromText("50", "2026-01-01", Optional.empty());
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(whole, single)).build(), 1);

            assertEquals("2 records", label(panel, "paymentSummary").getText());
            assertEquals(List.of("2024-02-29", "7.50", "2026-01-01", "50.00"),
                    labelTexts(vbox(panel, "paymentRows")));
            return null;
        });
    }

    @Test
    public void payments_sectionStaysIndependentOfWorkoutAndEmptyStates() throws Exception {
        runOnFxThread(() -> {
            PaymentRecord payment = PaymentRecord.fromText("50", "2026-01-01", Optional.empty());
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(payment)).build(), 1);

            VBox paymentRows = vbox(panel, "paymentRows");
            VBox workoutRows = vbox(panel, "workoutRows");
            assertTrue(paymentRows.getParent().getStyleClass().contains("payments-card"));
            assertFalse(paymentRows.getParent().getStyleClass().contains("workout-card"));
            assertNotSame(paymentRows.getParent(), workoutRows.getParent());
            assertTrue(region(panel, "paymentRows").isManaged());
            assertTrue(region(panel, "workoutEmpty").isManaged());
            assertFalse(region(panel, "paymentEmpty").isManaged());

            panel.showPerson(new PersonBuilder()
                    .withWorkoutPlan(new WorkoutPlan(List.of(new Exercise("Squat", 3, 8, 60))))
                    .build(), 2);

            assertEquals(0, vbox(panel, "paymentRows").getChildren().size());
            assertTrue(region(panel, "paymentEmpty").isManaged());
            assertTrue(region(panel, "workoutRows").isManaged());
            assertFalse(region(panel, "workoutEmpty").isManaged());
            return null;
        });
    }

    @Test
    public void payments_optionalRemarksAndMaximumLengthRemarksWrap() throws Exception {
        runOnFxThread(() -> {
            String longRemarks = "x".repeat(PaymentRecord.MAX_REMARKS_LENGTH);
            PaymentRecord plain = PaymentRecord.fromText("20", "2026-01-01", Optional.empty());
            PaymentRecord noted = PaymentRecord.fromText("30", "2026-01-02", Optional.of(longRemarks));
            ClientDetailPanel panel = new ClientDetailPanel();
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(plain, noted)).build(), 1);
            VBox rows = vbox(panel, "paymentRows");

            assertEquals(List.of("2026-01-01", "20.00", "2026-01-02", "30.00", longRemarks),
                    labelTexts(rows));
            Label remarks = labels(rows).stream()
                    .filter(node -> node.getText().equals(longRemarks))
                    .findFirst().orElseThrow();
            assertTrue(remarks.isWrapText());
            assertTrue(remarks.getMaxWidth() > 0);
            assertEquals(PaymentRecord.MAX_REMARKS_LENGTH, remarks.getText().length());
            return null;
        });
    }

    @Test
    public void payments_largeAmountsAndRemarksRemainFullyRenderedAfterLayout() throws Exception {
        runOnFxThread(() -> {
            for (int width : List.of(480, 660, 1000)) {
                for (String amount : List.of("123456789012345678901234567890.12", "9".repeat(120) + ".50")) {
                    String remarks = "x".repeat(PaymentRecord.MAX_REMARKS_LENGTH);
                    PaymentRecord payment = PaymentRecord.fromText(amount, "2026-10-20", Optional.of(remarks));
                    ClientDetailPanel panel = new ClientDetailPanel();
                    panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(payment)).build(), 1);
                    Scene scene = new Scene(panel.getRoot(), width, 900);
                    scene.getStylesheets().add(getClass().getResource("/view/FittixTheme.css").toExternalForm());
                    panel.getRoot().applyCss();
                    panel.getRoot().layout();

                    for (String style : List.of("payment-amount", "payment-remarks")) {
                        Label value = (Label) panel.getRoot().lookup("." + style);
                        Text rendered = (Text) value.lookup(".text");
                        assertEquals(value.getText(), rendered.getText(), style + " at width " + width);
                        assertTrue(rendered.getLayoutBounds().getHeight() <= value.getHeight(),
                                style + " must have enough height for every wrapped line");
                        assertTrue(rendered.getLayoutBounds().getWidth() <= value.getWidth(),
                                style + " must fit within its column");
                    }
                }
            }
            return null;
        });
    }

    private static Label label(ClientDetailPanel panel, String id) {
        prepareForLookup(panel);
        return (Label) panel.getRoot().lookup("#" + id);
    }

    private static Region region(ClientDetailPanel panel, String id) {
        prepareForLookup(panel);
        return (Region) panel.getRoot().lookup("#" + id);
    }

    private static VBox vbox(ClientDetailPanel panel, String id) {
        return (VBox) region(panel, id);
    }

    private static List<String> labelTexts(Node root) {
        return labels(root).stream().map(Label::getText).toList();
    }

    private static List<Label> labels(Node root) {
        List<Label> result = new ArrayList<>();
        collectLabels(root, result);
        return result;
    }

    private static void collectLabels(Node node, List<Label> result) {
        if (node instanceof Label label) {
            result.add(label);
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collectLabels(child, result));
        }
    }

    private static void prepareForLookup(ClientDetailPanel panel) {
        Region root = panel.getRoot();
        if (root.getScene() == null) {
            new Scene(root, 660, 900);
            root.applyCss();
        }
    }

    private static <T> T runOnFxThread(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
