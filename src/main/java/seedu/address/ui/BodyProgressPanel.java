package seedu.address.ui;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.BodyProgressViewRequest;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.BodyProgressEntry;
import seedu.address.model.person.Person;

/** Body measurements and complete dated history in the right context column. */
public class BodyProgressPanel extends UiPart<Region> {
    private static final String FXML = "BodyProgressPanel.fxml";
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.ENGLISH);

    @FXML
    private VBox bodyContent;
    @FXML
    private GridPane bodyMetrics;
    @FXML
    private Label baseline;
    @FXML
    private Label weightValue;
    @FXML
    private Label weightDelta;
    @FXML
    private Label weightFrom;
    @FXML
    private Label heightValue;
    @FXML
    private Label heightFrom;
    @FXML
    private Label checkinCount;
    @FXML
    private Label checkinFrom;
    @FXML
    private VBox historySection;
    @FXML
    private Label historyHeading;
    @FXML
    private VBox historyRows;
    @FXML
    private VBox comparisonSection;
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
        historyRows.getChildren().clear();
        baseline.setText("");
        comparison.setText("");
        weightValue.setText("");
        weightDelta.setText("");
        weightFrom.setText("");
        heightValue.setText("");
        heightFrom.setText("");
        checkinCount.setText("");
        checkinFrom.setText("");
        getRoot().setVisible(person != null);
        getRoot().setManaged(person != null);
        if (person == null) {
            return;
        }
        BodyProgress progress = person.getBodyProgress();
        boolean hasBaseline = progress.getBaseline().isPresent();
        setShown(bodyContent, true);
        setShown(bodyMetrics, hasBaseline);
        setShown(historySection, hasBaseline);
        setShown(comparisonSection, hasBaseline);
        if (!hasBaseline) {
            baseline.setText("No body baseline recorded for " + person.getName() + ". Use bodybase first.");
            return;
        }
        BodyBaseline starting = progress.getBaseline().orElseThrow();
        BodyProgressEntry latest = progress.getLatestEntry().orElse(null);
        weightValue.setText((latest == null ? starting.weightKg() : latest.weightKg()).toPlainString() + " kg");
        heightValue.setText(starting.heightCm().toPlainString() + " cm");
        checkinCount.setText(String.valueOf(progress.getEntries().size()));
        weightFrom.setText(latest == null ? "baseline" : "from " + starting.weightKg().toPlainString() + " kg");
        heightFrom.setText("baseline");
        checkinFrom.setText(latest == null ? "" : "latest " + latest.date().format(DATE_FORMAT));
        if (latest == null) {
            weightDelta.setText("");
            comparison.setText("No body progress entries recorded for " + person.getName()
                    + ". Use bodylog before comparing.");
        } else {
            BigDecimal delta = latest.weightKg().subtract(starting.weightKg());
            String signedDelta = (delta.signum() > 0 ? "+" : "") + delta.toPlainString() + " kg";
            weightDelta.setText(signedDelta);
            comparison.setText("Baseline: " + starting.date() + " | " + starting.weightKg().toPlainString()
                    + " kg | " + starting.heightCm().toPlainString() + " cm\nLatest: " + latest.date()
                    + " | " + latest.weightKg().toPlainString() + " kg\nChange: " + signedDelta);
        }
        String summary = "Baseline " + starting.date().format(DATE_FORMAT) + " · "
                + starting.weightKg().toPlainString() + " kg · " + starting.heightCm().toPlainString() + " cm";
        if (latest != null) {
            summary += " · latest " + latest.date().format(DATE_FORMAT);
        }
        int count = progress.getEntries().size();
        baseline.setText(summary + " · " + count + (count == 1 ? " check-in" : " check-ins"));
        historyHeading.setText("Progress history (" + count + ")");
        if (count == 0) {
            historyRows.getChildren().add(wrappedLabel("No body progress entries recorded."));
        }
        for (BodyProgressEntry entry : progress.getEntries()) {
            Label row = wrappedLabel(entry.date() + " | " + entry.weightKg().toPlainString()
                    + " kg | " + entry.notes().orElse("—"));
            row.getStyleClass().add("body-history-row");
            historyRows.getChildren().add(row);
        }
    }

    /** Reveals the requested section after layout, provided the same resolved client is still displayed. */
    public void reveal(BodyProgressViewRequest request) {
        // The surrounding workspace owns scrolling; its caller reveals the requested section.
    }

    /** Returns the heading requested by a body-progress command. */
    public Node getRevealTarget(BodyProgressViewRequest request) {
        if (!bodyMetrics.isManaged()) {
            return baseline;
        }
        return request.section() == BodyProgressViewRequest.Section.HISTORY
                ? historyHeading : comparisonHeading;
    }

    private static void setShown(Region region, boolean shown) {
        region.setVisible(shown);
        region.setManaged(shown);
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
