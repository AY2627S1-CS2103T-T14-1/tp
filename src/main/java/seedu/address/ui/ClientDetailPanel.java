package seedu.address.ui;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.BodyProgressViewRequest;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;
import seedu.address.model.person.Weekday;

/** Centre-column profile and workout-plan view for the selected client. */
public class ClientDetailPanel extends UiPart<Region> {

    private static final String FXML = "ClientDetailPanel.fxml";

    private BodyProgressPanel bodyProgressPanel;
    private Person displayedPerson;
    @FXML
    private StackPane bodyProgressPlaceholder;
    @FXML
    private VBox selectedContent;
    @FXML
    private VBox emptyContent;
    @FXML
    private Label profileIndex;
    @FXML
    private Label clientName;
    @FXML
    private FlowPane profileTags;
    @FXML
    private Label phone;
    @FXML
    private Label email;
    @FXML
    private Label address;
    @FXML
    private Label goal;
    @FXML
    private VBox healthProfileBox;
    @FXML
    private Label healthProfile;
    @FXML
    private VBox remarkBox;
    @FXML
    private Label remark;
    @FXML
    private Label planSummary;
    @FXML
    private VBox workoutRows;
    @FXML
    private Region workoutEmpty;
    @FXML
    private Label paymentSummary;
    @FXML
    private VBox paymentRows;
    @FXML
    private Region paymentEmpty;

    /** Creates an initially empty client detail panel. */
    public ClientDetailPanel() {
        super(FXML);
        bodyProgressPanel = new BodyProgressPanel();
        bodyProgressPlaceholder.getChildren().add(bodyProgressPanel.getRoot());
        clientName.maxWidthProperty().bind(selectedContent.widthProperty().subtract(40));
        showPerson(null, -1);
    }

    /** Displays {@code person} using its one-based index in the visible list. */
    public void showPerson(Person person, int displayedIndex) {
        displayedPerson = person;
        bodyProgressPanel.showPerson(person);
        boolean hasPerson = person != null;
        setShown(selectedContent, hasPerson);
        setShown(emptyContent, !hasPerson);
        if (!hasPerson) {
            return;
        }

        profileIndex.setText(String.valueOf(displayedIndex));
        clientName.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        email.setText(person.getEmail().value.replace("@", "@\u200b").replace(".", ".\u200b"));
        address.setText(person.getAddress().value);
        goal.setText(person.getGoal().map(value -> value.value).orElse("—"));

        profileTags.getChildren().clear();
        person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName)).forEach(tag -> {
            Label label = new Label(tag.tagName);
            label.getStyleClass().add("tag");
            profileTags.getChildren().add(label);
        });

        String healthText = person.getHealthProfile().map(value -> value.value).orElse("");
        healthProfile.setText(healthText);
        setShown(healthProfileBox, !healthText.isBlank());

        String remarkText = person.getRemark().value;
        remark.setText(remarkText);
        setShown(remarkBox, !remarkText.isBlank());
        showPayments(person);
        showWorkoutPlan(person, displayedIndex);
    }

    /** Reveals the requested tracker section after MainWindow selects its resolved client. */
    public void revealBodyProgress(BodyProgressViewRequest request) {
        if (request.person().equals(displayedPerson)) {
            bodyProgressPanel.reveal(request);
        }
    }

    /** The requested body-progress heading within the shared workspace scroll pane. */
    public Node getBodyProgressNode(BodyProgressViewRequest request) {
        return bodyProgressPanel.getRevealTarget(request);
    }

    private void showPayments(Person person) {
        List<PaymentRecord> payments = new ArrayList<>(person.getPaymentRecords());
        payments.sort(Comparator.comparing(PaymentRecord::getDate));
        paymentSummary.setText(payments.isEmpty() ? ""
                : payments.size() + (payments.size() == 1 ? " record" : " records"));

        paymentRows.getChildren().clear();
        setShown(paymentRows, !payments.isEmpty());
        setShown(paymentEmpty, payments.isEmpty());
        for (PaymentRecord payment : payments) {
            paymentRows.getChildren().add(createPaymentRow(payment));
        }
    }

    private GridPane createPaymentRow(PaymentRecord payment) {
        GridPane row = new GridPane();
        row.getStyleClass().add("payment-row");
        row.setHgap(14);
        ColumnConstraints dateColumn = new ColumnConstraints(104, 104, 104);
        ColumnConstraints amountColumn = new ColumnConstraints(100, 100, 100);
        ColumnConstraints remarksColumn = new ColumnConstraints();
        remarksColumn.setHgrow(Priority.ALWAYS);
        row.getColumnConstraints().addAll(dateColumn, amountColumn, remarksColumn);

        Label date = new Label(payment.getDate().toString());
        date.getStyleClass().addAll("payment-date", "mono");
        Label amount = new Label(payment.getFormattedAmount());
        amount.getStyleClass().addAll("payment-amount", "mono");
        amount.setWrapText(true);
        amount.setMinHeight(Region.USE_PREF_SIZE);
        amount.setMaxWidth(Double.MAX_VALUE);
        amount.setAlignment(Pos.CENTER_RIGHT);
        row.add(date, 0, 0);
        row.add(amount, 1, 0);

        payment.getRemarks().ifPresent(remarks -> {
            Label remarksLabel = new Label(remarks);
            remarksLabel.getStyleClass().add("payment-remarks");
            remarksLabel.setWrapText(true);
            remarksLabel.setMinHeight(Region.USE_PREF_SIZE);
            remarksLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(remarksLabel, Priority.ALWAYS);
            row.add(remarksLabel, 2, 0);
        });
        return row;
    }

    private void showWorkoutPlan(Person person, int displayedIndex) {
        List<Exercise> exercises = person.getWorkoutPlan().getExercises();
        long trainingDays = exercises.stream().map(Exercise::getDay)
                .map(Weekday::fromPrefix).flatMap(java.util.Optional::stream).distinct().count();
        planSummary.setText(exercises.isEmpty() ? "" : exercises.size() + " exercise"
                + (exercises.size() == 1 ? "" : "s") + " · " + trainingDays + " training day"
                + (trainingDays == 1 ? "" : "s"));

        workoutRows.getChildren().clear();
        setShown(workoutRows, !exercises.isEmpty());
        setShown(workoutEmpty, exercises.isEmpty());
        if (exercises.isEmpty()) {
            return;
        }

        List<Messages.IndexedExercise> indexed = Messages.getExercisesInDisplayOrder(person.getWorkoutPlan());
        for (Weekday weekday : Weekday.values()) {
            List<Messages.IndexedExercise> dayExercises = indexed.stream()
                    .filter(item -> Weekday.fromPrefix(item.exercise().getDay()).orElse(null) == weekday).toList();
            workoutRows.getChildren().add(createDayRow(weekday, dayExercises));
        }
        List<Messages.IndexedExercise> unspecified = indexed.stream()
                .filter(item -> Weekday.fromPrefix(item.exercise().getDay()).isEmpty()).toList();
        if (!unspecified.isEmpty()) {
            workoutRows.getChildren().add(createUnspecifiedRow(unspecified));
        }
    }

    private GridPane createDayRow(Weekday weekday, List<Messages.IndexedExercise> exercises) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        boolean isToday = weekday.name().equals(today.name());
        String shortName = weekday.name().substring(0, 3);
        String subtitle = weekday.getDisplayName() + (exercises.isEmpty() ? "" : " · " + exercises.size());
        return createDayRowContent(shortName, subtitle, exercises, isToday);
    }

    private GridPane createUnspecifiedRow(List<Messages.IndexedExercise> exercises) {
        return createDayRowContent("UNSET", "Day not specified · " + exercises.size(), exercises, false);
    }

    private GridPane createDayRowContent(String shortName, String subtitle,
                                         List<Messages.IndexedExercise> exercises, boolean isToday) {
        GridPane row = new GridPane();
        row.getStyleClass().add("day-row");
        row.setHgap(14);
        ColumnConstraints dayColumn = new ColumnConstraints(104, 104, 104);
        ColumnConstraints contentColumn = new ColumnConstraints();
        contentColumn.setHgrow(Priority.ALWAYS);
        row.getColumnConstraints().addAll(dayColumn, contentColumn);

        HBox dayHeading = new HBox(7);
        dayHeading.setAlignment(Pos.CENTER_LEFT);
        Label shortLabel = new Label(shortName);
        shortLabel.getStyleClass().addAll("day-code", "mono");
        if (isToday) {
            shortLabel.getStyleClass().add("today-text");
            Label todayLabel = new Label("TODAY");
            todayLabel.getStyleClass().add("today-pill");
            dayHeading.getChildren().addAll(shortLabel, todayLabel);
        } else {
            dayHeading.getChildren().add(shortLabel);
        }
        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("day-subtitle");
        VBox dayInfo = new VBox(3, dayHeading, subtitleLabel);
        dayInfo.setAlignment(Pos.TOP_LEFT);
        row.add(dayInfo, 0, 0);

        if (exercises.isEmpty()) {
            Label rest = new Label("Rest");
            rest.getStyleClass().add("rest-label");
            row.add(rest, 1, 0);
        } else {
            GridPane tiles = new GridPane();
            tiles.setHgap(8);
            tiles.setVgap(8);
            for (int i = 0; i < 3; i++) {
                ColumnConstraints column = new ColumnConstraints();
                column.setPercentWidth(100.0 / 3);
                column.setHgrow(Priority.ALWAYS);
                tiles.getColumnConstraints().add(column);
            }
            for (int i = 0; i < exercises.size(); i++) {
                tiles.add(createExerciseTile(exercises.get(i), isToday), i % 3, i / 3);
            }
            row.add(tiles, 1, 0);
        }
        return row;
    }

    private VBox createExerciseTile(Messages.IndexedExercise indexedExercise, boolean isToday) {
        Exercise exercise = indexedExercise.exercise();
        Label index = new Label(String.valueOf(indexedExercise.index()));
        index.getStyleClass().addAll("exercise-index", "mono");
        Label name = new Label(exercise.getName());
        name.getStyleClass().add("exercise-name");
        name.setWrapText(true);
        name.setMinWidth(0);
        name.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(name, Priority.ALWAYS);
        HBox title = new HBox(8, index, name);
        title.setAlignment(Pos.CENTER_LEFT);

        Label setsAndReps = new Label(exercise.getSets() + " × " + exercise.getReps());
        setsAndReps.getStyleClass().addAll("exercise-metric", "mono");
        Label weight = new Label(formatWeight(exercise.getWeightKg()));
        weight.getStyleClass().addAll("exercise-weight", "mono");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox metrics = new HBox(setsAndReps, spacer, weight);

        VBox tile = new VBox(6, title, metrics);
        tile.getStyleClass().add("tile");
        if (isToday) {
            tile.getStyleClass().add("today");
        }
        tile.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(tile, Priority.ALWAYS);
        return tile;
    }

    private static String formatWeight(double weightKg) {
        if (Double.compare(weightKg, 0) == 0) {
            return "BW";
        }
        return BigDecimal.valueOf(weightKg).stripTrailingZeros().toPlainString() + " kg";
    }

    private static void setShown(Region region, boolean shown) {
        region.setVisible(shown);
        region.setManaged(shown);
    }
}
