package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import seedu.address.model.person.PaymentRecord;
import seedu.address.testutil.PersonBuilder;

/** Tests the billing card in the right context column. */
public class ClientContextPaymentTest {
    @BeforeAll
    public static void startJavaFx() throws Exception {
        ClientDetailPanelTest.startJavaFx();
    }

    @Test
    public void payments_sortedAndCompleteWithPaidStatus() throws Exception {
        onFx(() -> {
            PaymentRecord latest = PaymentRecord.fromText("150", "2026-10-20", Optional.empty());
            PaymentRecord first = PaymentRecord.fromText("75.50", "2026-10-19", Optional.of("First"));
            PaymentRecord duplicate = PaymentRecord.fromText("75.50", "2026-10-19", Optional.of("First"));
            PaymentRecord second = PaymentRecord.fromText("75.50", "2026-10-19", Optional.of("Second"));
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(latest, first, duplicate, second))
                    .build(), 1);
            VBox rows = (VBox) panel.getRoot().lookup("#paymentRows");
            assertEquals(4, rows.getChildren().size());
            assertEquals(List.of("First", "2026-10-19", "75.50", "PAID",
                    "First", "2026-10-19", "75.50", "PAID",
                    "Second", "2026-10-19", "75.50", "PAID",
                    "Payment", "2026-10-20", "150.00", "PAID"), labelTexts(rows));
            assertTrue(rows.getParent().getStyleClass().contains("payments-card"));

            panel.showPerson(new PersonBuilder().build(), 2);
            assertTrue(rows.getChildren().isEmpty());
            assertFalse(rows.isManaged());
            Label empty = (Label) panel.getRoot().lookup("#paymentEmpty");
            assertTrue(empty.isManaged());
            assertEquals("No payments recorded.", empty.getText());
            assertTrue(rows.getParent().isManaged());
            return null;
        });
    }

    @Test
    public void longPaymentTexts_wrapWithinNarrowContextColumn() throws Exception {
        onFx(() -> {
            String remarks = "x".repeat(PaymentRecord.MAX_REMARKS_LENGTH);
            String amount = "9".repeat(120) + ".50";
            PaymentRecord payment = PaymentRecord.fromText(amount, "2026-10-20", Optional.of(remarks));
            ClientContextPanel panel = new ClientContextPanel();
            panel.showPerson(new PersonBuilder().withPaymentRecords(List.of(payment)).build(), 1);
            Scene scene = new Scene(panel.getRoot(), 340, 900);
            scene.getStylesheets().add(getClass().getResource("/view/FittixTheme.css").toExternalForm());
            panel.getRoot().applyCss();
            panel.getRoot().layout();
            for (String style : List.of("payment-note", "payment-date", "payment-amount")) {
                Label value = (Label) panel.getRoot().lookup("." + style);
                Text rendered = (Text) value.lookup(".text");
                assertEquals(value.getText(), rendered.getText());
                assertTrue(rendered.getLayoutBounds().getHeight() <= value.getHeight(), style);
                assertTrue(value.localToScene(value.getBoundsInLocal()).getMaxX() <= 340, style);
            }
            return null;
        });
    }

    private static List<String> labelTexts(Node root) {
        List<String> result = new ArrayList<>();
        collectLabels(root, result);
        return result;
    }

    private static void collectLabels(Node node, List<String> result) {
        if (node instanceof Label label) {
            result.add(label.getText());
        }
        if (node instanceof Parent parent) {
            parent.getChildrenUnmodifiable().forEach(child -> collectLabels(child, result));
        }
    }

    private static <T> T onFx(Callable<T> action) throws Exception {
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        return task.get(10, TimeUnit.SECONDS);
    }
}
