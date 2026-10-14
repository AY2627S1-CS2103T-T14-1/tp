package seedu.address.ui;

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

/**
 * Panel containing the list of persons.
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

    /**
     * Creates a {@code PersonListPanel} with the given {@code ObservableList}.
     */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        personListView.setItems(personList);
        personListView.setCellFactory(listView -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((observable, previous, selected) ->
                showDietPlan(selected));
        ListChangeListener<Person> listChangeListener = change -> updateSelectionAfterListChange();
        personList.addListener(listChangeListener);
        configureWrapping(selectedClientName);
        configureWrapping(goal);
        configureWrapping(dietTarget);
        configureWrapping(dietNotes);
        personListView.getSelectionModel().selectFirst();
        showDietPlan(personListView.getSelectionModel().getSelectedItem());
    }

    private void configureWrapping(Label label) {
        label.setMinWidth(0);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setWrapText(true);
    }

    /**
     * Updates the client selection after the displayed list changes.
     */
    private void updateSelectionAfterListChange() {
        Person selected = personListView.getSelectionModel().getSelectedItem();
        if (selected != null && personListView.getItems().contains(selected)) {
            showDietPlan(selected);
            return;
        }

        if (personListView.getItems().isEmpty()) {
            personListView.getSelectionModel().clearSelection();
        } else {
            personListView.getSelectionModel().selectFirst();
        }
        showDietPlan(personListView.getSelectionModel().getSelectedItem());
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
