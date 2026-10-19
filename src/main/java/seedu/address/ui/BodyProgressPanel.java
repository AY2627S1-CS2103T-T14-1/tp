package seedu.address.ui;

import java.math.BigDecimal;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.BodyProgressViewRequest;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.BodyProgressEntry;
import seedu.address.model.person.Person;

/** Scrollable body measurements, complete history and derived comparison in the centre details column. */
public class BodyProgressPanel extends UiPart<Region> {
    private static final String FXML = "BodyProgressPanel.fxml";
    private Person displayedPerson;

    @FXML
    private ScrollPane bodyScroll;
    @FXML
    private VBox bodyContent;
    @FXML
    private Label baseline;
    @FXML
    private Label historyHeading;
    @FXML
    private VBox historyRows;
    @FXML
    private Label comparisonHeading;
    @FXML
    private Label comparison;

    /** Creates an empty tracker display. */
    public BodyProgressPanel() {
        super(FXML);
        showPerson(null);
    }

    /** Rebuilds all derived fields, clearing stale data even when no client is selected. */
    public void showPerson(Person person) {
        displayedPerson = person;
        historyRows.getChildren().clear();
        baseline.setText("");
        historyHeading.setText("Progress history (0)");
        comparison.setText("");
        getRoot().setVisible(person != null);
        getRoot().setManaged(person != null);
        if (person == null) {
            return;
        }
        BodyProgress progress = person.getBodyProgress();
        if (progress.getBaseline().isEmpty()) {
            baseline.setText("No body baseline recorded for " + person.getName() + ". Use bodybase first.");
            historyRows.getChildren().add(wrappedLabel("No body progress entries recorded."));
            comparison.setText("Record starting measurements before comparing progress.");
            return;
        }
        BodyBaseline starting = progress.getBaseline().orElseThrow();
        baseline.setText("Baseline: " + starting.date() + " | " + starting.weightKg().toPlainString()
                + " kg | " + starting.heightCm().toPlainString() + " cm");
        historyHeading.setText("Progress history (" + progress.getEntries().size() + ")");
        for (BodyProgressEntry entry : progress.getEntries()) {
            historyRows.getChildren().add(wrappedLabel(entry.date() + " | " + entry.weightKg().toPlainString()
                    + " kg | " + entry.notes().orElse("—")));
        }
        if (progress.getEntries().isEmpty()) {
            historyRows.getChildren().add(wrappedLabel("No body progress entries recorded."));
            comparison.setText("No body progress entries recorded for " + person.getName()
                    + ". Use bodylog before comparing.");
            return;
        }
        BodyProgressEntry latest = progress.getLatestEntry().orElseThrow();
        BigDecimal delta = latest.weightKg().subtract(starting.weightKg());
        String signedDelta = (delta.signum() > 0 ? "+" : "") + delta.toPlainString();
        comparison.setText("Baseline: " + starting.date() + " | " + starting.weightKg().toPlainString()
                + " kg | " + starting.heightCm().toPlainString() + " cm\nLatest: " + latest.date() + " | "
                + latest.weightKg().toPlainString() + " kg\nChange: " + signedDelta + " kg");
    }

    /** Reveals the requested section after layout, provided the same resolved client is still displayed. */
    public void reveal(BodyProgressViewRequest request) {
        Platform.runLater(() -> {
            if (!request.person().equals(displayedPerson)) {
                return;
            }
            bodyContent.applyCss();
            bodyContent.layout();
            Label target = request.section() == BodyProgressViewRequest.Section.HISTORY
                    ? historyHeading : comparisonHeading;
            double overflow = bodyContent.getLayoutBounds().getHeight() - bodyScroll.getViewportBounds().getHeight();
            bodyScroll.setVvalue(overflow <= 0 ? 0
                    : Math.min(1, Math.max(0, target.getBoundsInParent().getMinY() / overflow)));
        });
    }

    private static Label wrappedLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("profile-value");
        label.setWrapText(true);
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        return label;
    }
}
