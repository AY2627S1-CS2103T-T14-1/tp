package seedu.address.ui;

import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Logger;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/**
 * Panel containing the list of persons and the selected client's details.
 */
public class PersonListPanel extends UiPart<Region> {
    private static final String FXML = "PersonListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(PersonListPanel.class);

    @FXML
    private ListView<Person> personListView;
    @FXML
    private Label selectedClientName;
    @FXML
    private Label goal;
    @FXML
    private Label dietPlanHeading;
    @FXML
    private Label dietTarget;
    @FXML
    private Label dietNotes;
    @FXML
    private Label dietEmpty;
    @FXML
    private Label workoutHeading;
    @FXML
    private Label workoutDate;
    @FXML
    private Label workoutTime;
    @FXML
    private Label workoutEntries;
    @FXML
    private Label workoutEmpty;

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) ->
                showClientDetails(selected));
        ListChangeListener<Person> listChangeListener = change -> updateSelectionAfterListChange();
        personList.addListener(listChangeListener);

        configureWrapping(selectedClientName);
        configureWrapping(goal);
        configureWrapping(dietTarget);
        configureWrapping(dietNotes);
        configureWrapping(workoutHeading);
        configureWrapping(workoutDate);
        configureWrapping(workoutTime);
        configureWrapping(workoutEntries);
        configureWrapping(workoutEmpty);

        personListView.getSelectionModel().selectFirst();
        showClientDetails(personListView.getSelectionModel().getSelectedItem());
    }

    private void configureWrapping(Label label) {
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setWrapText(true);
    }

    /**
     * Updates the client selection after the displayed list changes.
     */
    private void updateSelectionAfterListChange() {
        Person selected = personListView.getSelectionModel().getSelectedItem();
        if (selected != null && personListView.getItems().contains(selected)) {
            showClientDetails(selected);
            return;
        }

        if (personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().clearSelection();
        } else {
            personListView.getSelectionModel().selectFirst();
        }
        showClientDetails(personListView.getSelectionModel().getSelectedItem());
    }

    private void showClientDetails(Person selected) {
        showDietPlan(selected);
        showWorkoutSession(selected);
    }

    private void showDietPlan(Person selected) {
        if (selected == null) {
            selectedClientName.setText("Select a client to view their current diet plan.");
            setShown(goal, false);
            setShown(dietPlanHeading, false);
            setShown(dietTarget, false);
            setShown(dietNotes, false);
            setShown(dietEmpty, false);
            return;
        }

        selectedClientName.setText(selected.getName().fullName);
        selected.getGoal().ifPresentOrElse(value -> {
            goal.setText("Goal: " + value.value);
            setShown(goal, true);
        }, () -> setShown(goal, false));
        setShown(dietPlanHeading, true);

        if (selected.getDietPlan().isEmpty()) {
            setShown(dietTarget, false);
            setShown(dietNotes, false);
            setShown(dietEmpty, true);
            return;
        }

        DietPlan plan = selected.getDietPlan().get();
        dietTarget.setText("Target: " + plan.getTarget());
        setShown(dietTarget, true);
        plan.getNotes().ifPresentOrElse(notes -> {
            dietNotes.setText("Notes: " + notes);
            setShown(dietNotes, true);
        }, () -> setShown(dietNotes, false));
        setShown(dietEmpty, false);
    }

    private void showWorkoutSession(Person selected) {
        setShown(workoutHeading, selected != null);

        if (selected == null || selected.getWorkoutSessions().isEmpty()) {
            workoutDate.setText("");
            workoutTime.setText("");
            workoutEntries.setText("");
            setShown(workoutDate, false);
            setShown(workoutTime, false);
            setShown(workoutEntries, false);
            setShown(workoutEmpty, selected != null);
            return;
        }

        List<WorkoutSession> sessions = selected.getWorkoutSessions();
        WorkoutSession session = sessions.get(sessions.size() - 1);

        workoutDate.setText("Date: " + session.getDate());
        workoutTime.setText(session.getTime()
                .map(time -> "Time: " + time)
                .orElse("Time: Not specified"));

        StringJoiner entries = new StringJoiner("\n");
        int entryNumber = 1;
        for (WorkoutLogEntry entry : session.getEntries()) {
            entries.add(entryNumber + ". " + entry.getName()
                    + ": " + entry.getWeightKg().toPlainString() + " kg, "
                    + entry.getSets() + " sets x " + entry.getReps() + " reps");
            entryNumber++;
        }
        workoutEntries.setText(entries.toString());

        setShown(workoutDate, true);
        setShown(workoutTime, true);
        setShown(workoutEntries, true);
        setShown(workoutEmpty, false);
    }

    private void setShown(Label label, boolean shown) {
        label.setVisible(shown);
        label.setManaged(shown);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Person} using a {@code PersonCard}.
     */
    class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);

            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
            }
        }
    }
}
