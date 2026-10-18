package seedu.address.ui;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;

/** Compact client list and selection owner for the redesigned workspace. */
public class PersonListPanel extends UiPart<Region> {

    private static final String FXML = "PersonListPanel.fxml";

    private final ObservableList<Person> displayedPeople;
    private final ObservableList<? extends Person> sourcePeople;
    private final ReadOnlyIntegerWrapper totalCount = new ReadOnlyIntegerWrapper();
    private int lastSelectedSourceIndex = -1;
    private String filterQuery;

    @FXML
    private ListView<Person> personListView;
    @FXML
    private Label shownCount;
    @FXML
    private Label emptyTitle;
    @FXML
    private Label emptyBody;
    @FXML
    private Label emptyCommand;

    /** Creates a client list backed by the logic component's filtered list. */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        displayedPeople = personList;
        sourcePeople = getSourceList(personList);

        personListView.setItems(displayedPeople);
        personListView.setCellFactory(unused -> new PersonListViewCell());
        personListView.getSelectionModel().selectedItemProperty().addListener((unused, previous, selected) -> {
            if (selected != null) {
                lastSelectedSourceIndex = sourcePeople.indexOf(selected);
            }
        });

        displayedPeople.addListener((ListChangeListener<Person>) change -> {
            updateCounts();
            restoreSelection();
            updateEmptyState();
        });
        sourcePeople.addListener((ListChangeListener<Person>) change -> {
            updateCounts();
            updateEmptyState();
        });

        updateCounts();
        updateEmptyState();
        personListView.getSelectionModel().selectFirst();
    }

    @SuppressWarnings("unchecked")
    private static ObservableList<? extends Person> getSourceList(ObservableList<Person> personList) {
        if (personList instanceof FilteredList<?> filteredList) {
            return (ObservableList<? extends Person>) filteredList.getSource();
        }
        return personList;
    }

    private void updateCounts() {
        shownCount.setText(String.valueOf(displayedPeople.size()));
        totalCount.set(sourcePeople.size());
    }

    private void restoreSelection() {
        Person selected = personListView.getSelectionModel().getSelectedItem();
        if (selected != null && displayedPeople.contains(selected)) {
            return;
        }
        if (displayedPeople.isEmpty()) {
            personListView.getSelectionModel().clearSelection();
            return;
        }
        if (lastSelectedSourceIndex >= 0 && lastSelectedSourceIndex < sourcePeople.size()) {
            Person replacement = sourcePeople.get(lastSelectedSourceIndex);
            int displayedIndex = displayedPeople.indexOf(replacement);
            if (displayedIndex >= 0) {
                personListView.getSelectionModel().select(displayedIndex);
                return;
            }
        }
        personListView.getSelectionModel().selectFirst();
    }

    /** Sets the active find query, or {@code null} when all clients are shown. */
    public void setFilterQuery(String filterQuery) {
        this.filterQuery = filterQuery == null || filterQuery.isBlank() ? null : filterQuery.trim();
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (sourcePeople.isEmpty()) {
            emptyTitle.setText("No clients yet");
            emptyBody.setText("Add a client to start building plans, sessions and workout history.");
            emptyCommand.setText("add n/NAME p/PHONE e/EMAIL a/ADDRESS");
        } else {
            emptyTitle.setText("No clients match");
            String suffix = filterQuery == null ? "." : " “" + filterQuery + "”.";
            emptyBody.setText("Nothing matched" + suffix + " Indexes refer to the visible list.");
            emptyCommand.setText("list");
        }
    }

    /** Selects a zero-based index in the currently displayed list. */
    public void selectDisplayedIndex(int index) {
        if (index >= 0 && index < displayedPeople.size()) {
            personListView.getSelectionModel().select(index);
            personListView.scrollTo(index);
        }
    }

    /** Selects the newly appended final client. */
    public void selectLast() {
        selectDisplayedIndex(displayedPeople.size() - 1);
    }

    /** Selects {@code person} when it is present in the displayed list. */
    public void selectPerson(Person person) {
        int index = displayedPeople.indexOf(person);
        if (index >= 0) {
            selectDisplayedIndex(index);
        }
    }

    public Person getSelectedPerson() {
        return personListView.getSelectionModel().getSelectedItem();
    }

    public int getSelectedIndex() {
        return personListView.getSelectionModel().getSelectedIndex();
    }

    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return personListView.getSelectionModel().selectedItemProperty();
    }

    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount.getReadOnlyProperty();
    }

    public int getDisplayedCount() {
        return displayedPeople.size();
    }

    /** Renders each list entry as a compact client card. */
    private class PersonListViewCell extends ListCell<Person> {
        @Override
        protected void updateItem(Person person, boolean empty) {
            super.updateItem(person, empty);
            if (empty || person == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(new PersonCard(person, getIndex() + 1).getRoot());
                setText(null);
            }
        }
    }
}
