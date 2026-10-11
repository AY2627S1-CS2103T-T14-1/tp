package seedu.address.ui;

import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutPlan;

/**
 * A UI component that displays information of a {@code Person}.
 */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Person person;

    @FXML
    private HBox cardPane;
    @FXML
    private Label name;
    @FXML
    private Label id;
    @FXML
    private Label phone;
    @FXML
    private Label address;
    @FXML
    private Label email;
    @FXML
    private Label remark;
    @FXML
    private FlowPane tags;
    @FXML
    private VBox workoutPlanContainer;
    @FXML
    private Label workoutPlanPlaceholder;

    /**
     * Creates a {@code PersonCard} with the given {@code Person} and index to display.
     */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(displayedIndex + ". ");
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        address.setText(person.getAddress().value);
        email.setText(person.getEmail().value);
        remark.setText(person.getRemark().value);
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        displayWorkoutPlan();
    }

    /**
     * Populates the workout plan section of this card.
     * Shows one row per exercise formatted as {@code Name SetsxReps @Weightkg}
     * (e.g. {@code Bench Press 4x10 @60kg}), numbered so the index matches
     * {@code DeleteExerciseCommand}'s exercise index.
     * An empty (or {@code null}) plan shows the placeholder text instead.
     * Labels wrap so long lists grow vertically without breaking the card layout.
     */
    private void displayWorkoutPlan() {
        WorkoutPlan plan = person.getWorkoutPlan();
        List<Exercise> exercises = (plan == null) ? List.of() : plan.getExercises();

        workoutPlanContainer.getChildren().clear();

        Label header = new Label(
                exercises.isEmpty() ? "Workout Plan:" : "Workout Plan (" + exercises.size() + "):");
        header.getStyleClass().add("cell_small_label");
        workoutPlanContainer.getChildren().add(header);

        if (exercises.isEmpty()) {
            workoutPlanPlaceholder.setText("No workout plan yet");
            workoutPlanContainer.getChildren().add(workoutPlanPlaceholder);
            return;
        }

        for (int i = 0; i < exercises.size(); i++) {
            Exercise exercise = exercises.get(i);
            Label exerciseLabel = new Label((i + 1) + ". " + formatExercise(exercise));
            exerciseLabel.getStyleClass().add("cell_small_label");
            exerciseLabel.setWrapText(true);
            exerciseLabel.setMaxWidth(350);
            workoutPlanContainer.getChildren().add(exerciseLabel);
        }
    }

    /**
     * Formats an exercise as {@code Name SetsxReps @Weightkg}, e.g. {@code Bench Press 4x10 @60kg}.
     */
    private static String formatExercise(Exercise exercise) {
        String weight = (exercise.getWeightKg() % 1 == 0)
                ? String.valueOf((int) exercise.getWeightKg())
                : String.valueOf(exercise.getWeightKg());
        return String.format("%s %dx%d @%skg",
                exercise.getName(), exercise.getSets(), exercise.getReps(), weight);
    }
}
