package seedu.address.ui;

import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.Messages;
import seedu.address.model.membership.MembershipName;
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
    private Label goal;
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
        person.getGoal().ifPresentOrElse(value -> {
            goal.setText("Goal: " + value.value);
            goal.setVisible(true);
            goal.setManaged(true);
        }, () -> {
            goal.setVisible(false);
            goal.setManaged(false);
        });
        person.getTags().stream()
                .sorted(Comparator.comparing(tag -> tag.tagName))
                .forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        displayMembership();
        displayWorkoutPlan();
    }

    /**
     * Adds the client's membership as a single tag reading
     * {@code Plan_Name Member: N sessions left}, with {@code [expires YYYY-MM-DD]}
     * appended when the assignment defines an expiry date. The tag joins the other
     * tags in the {@code tags} flow pane so they render side by side under the name;
     * nothing is added when the client has no assigned plan.
     */
    private void displayMembership() {
        person.getMembershipAssignment().ifPresent(assignment -> {
            StringBuilder text = new StringBuilder(
                    MembershipName.toDisplayName(assignment.getPlanName()))
                    .append(" Member: ").append(assignment.getRemainingSessions())
                    .append(" sessions left");
            assignment.getExpiryDate().ifPresent(date ->
                    text.append(" [expires ").append(date).append("]"));
            Label tag = new Label(text.toString());
            tag.getStyleClass().add("membership-tag");
            tag.setWrapText(true);
            tags.getChildren().add(tag);
        });
    }

    /**
     * Populates the workout plan section of this card.
     * Shows one row per exercise formatted as {@code Name SetsxReps @Weightkg}
     * (e.g. {@code Bench Press 4x10 @60kg}), numbered so the index matches
     * {@code DeleteExerciseCommand}'s exercise index.
     * An empty plan shows the placeholder text instead.
     * Labels wrap so long lists grow vertically without breaking the card layout.
     * Exercise text formatting is delegated to {@code Messages#formatExercise}
     * so the card and command feedback stay consistent.
     */
    private void displayWorkoutPlan() {
        WorkoutPlan plan = person.getWorkoutPlan();
        List<Exercise> exercises = plan.getExercises();

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

        String currentGroup = null;
        for (Messages.IndexedExercise indexed : Messages.getExercisesInDisplayOrder(plan)) {
            Exercise exercise = indexed.exercise();
            String group = Messages.getExerciseDayHeading(exercise);
            if (!group.equals(currentGroup)) {
                if (currentGroup != null) {
                    Label spacer = new Label();
                    spacer.setMinHeight(8);
                    spacer.setPrefHeight(8);
                    workoutPlanContainer.getChildren().add(spacer);
                }
                Label dayHeading = new Label(group);
                dayHeading.getStyleClass().add("cell_small_label");
                dayHeading.setStyle("-fx-font-weight: bold;");
                workoutPlanContainer.getChildren().add(dayHeading);
                currentGroup = group;
            }
            Label exerciseLabel = new Label(indexed.index() + ". " + Messages.formatExercise(exercise));
            exerciseLabel.getStyleClass().add("cell_small_label");
            exerciseLabel.setWrapText(true);
            exerciseLabel.setMaxWidth(350);
            workoutPlanContainer.getChildren().add(exerciseLabel);
        }
    }
}
