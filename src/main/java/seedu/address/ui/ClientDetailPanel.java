package seedu.address.ui;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.Messages;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.Weekday;

/** Centre-column profile and workout-plan view for the selected client. */
public class ClientDetailPanel extends UiPart<Region> {

    private static final String FXML = "ClientDetailPanel.fxml";

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

    /** Creates an initially empty client detail panel. */
    public ClientDetailPanel() {
        super(FXML);
        clientName.maxWidthProperty().bind(selectedContent.widthProperty().subtract(40));
        showPerson(null, -1);
    }

    /** Displays {@code person} using its one-based index in the visible list. */
    public void showPerson(Person person, int displayedIndex) {
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
        showWorkoutPlan(person, displayedIndex);
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
