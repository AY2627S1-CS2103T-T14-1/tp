package seedu.address.ui;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.Person;

/** Client list with one card per visible person and no nested scrollbar. */
public class PersonListPanel extends UiPart<Region> {

    private static final String FXML = "PersonListPanel.fxml";

    private final ObservableList<Person> displayedPeople;
    private final ObservableList<? extends Person> sourcePeople;
    private final ReadOnlyIntegerWrapper totalCount = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<Person> selectedPerson = new ReadOnlyObjectWrapper<>();
    private int lastSelectedSourceIndex = -1;

    @FXML
    private VBox personRows;
    @FXML
    private Label shownCount;

    /** Creates a list backed by the logic component's filtered list. */
    public PersonListPanel(ObservableList<Person> personList) {
        super(FXML);
        displayedPeople = personList;
        sourcePeople = getSourceList(personList);
        personRows.setFocusTraversable(true);
        personRows.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP || event.getCode() == KeyCode.DOWN) {
                int delta = event.getCode() == KeyCode.UP ? -1 : 1;
                selectDisplayedIndex(Math.max(0, Math.min(displayedPeople.size() - 1, getSelectedIndex() + delta)));
                event.consume();
            }
        });
        displayedPeople.addListener((ListChangeListener<Person>) change -> {
            updateCounts();
            restoreSelection();
            rebuildCards();
        });
        sourcePeople.addListener((ListChangeListener<Person>) change -> {
            updateCounts();
            restoreSelection();
            rebuildCards();
        });
        updateCounts();
        restoreSelection();
        rebuildCards();
    }

    @SuppressWarnings("unchecked")
    private static ObservableList<? extends Person> getSourceList(ObservableList<Person> people) {
        if (people instanceof FilteredList<?> filtered) {
            return (ObservableList<? extends Person>) filtered.getSource();
        }
        return people;
    }

    private void updateCounts() {
        shownCount.setText(String.valueOf(displayedPeople.size()));
        totalCount.set(sourcePeople.size());
    }

    private void restoreSelection() {
        Person selected = selectedPerson.get();
        int visibleIndex = displayedPeople.indexOf(selected);
        if (visibleIndex >= 0) {
            selectedPerson.set(displayedPeople.get(visibleIndex));
            return;
        }
        if (displayedPeople.isEmpty()) {
            selectedPerson.set(null);
            return;
        }
        if (lastSelectedSourceIndex >= 0 && lastSelectedSourceIndex < sourcePeople.size()) {
            int replacementIndex = displayedPeople.indexOf(sourcePeople.get(lastSelectedSourceIndex));
            if (replacementIndex >= 0) {
                selectedPerson.set(displayedPeople.get(replacementIndex));
                return;
            }
        }
        selectedPerson.set(displayedPeople.getFirst());
    }

    private void rebuildCards() {
        personRows.getChildren().clear();
        for (int index = 0; index < displayedPeople.size(); index++) {
            Person person = displayedPeople.get(index);
            Region card = new PersonCard(person, index + 1).getRoot();
            if (index == getSelectedIndex()) {
                card.getStyleClass().add("selected");
            }
            card.setOnMouseClicked(event -> {
                selectPerson(person);
                personRows.requestFocus();
            });
            personRows.getChildren().add(card);
        }
    }

    /** Retained for callers that update the active find query. */
    public void setFilterQuery(String filterQuery) {
        rebuildCards();
    }

    /** Selects a zero-based index in the currently displayed list. */
    public void selectDisplayedIndex(int index) {
        if (index >= 0 && index < displayedPeople.size()) {
            selectedPerson.set(displayedPeople.get(index));
            lastSelectedSourceIndex = sourcePeople.indexOf(displayedPeople.get(index));
            rebuildCards();
        }
    }

    public void selectLast() {
        selectDisplayedIndex(displayedPeople.size() - 1);
    }

    public void selectPerson(Person person) {
        selectDisplayedIndex(displayedPeople.indexOf(person));
    }

    public Person getSelectedPerson() {
        return selectedPerson.get();
    }

    public int getSelectedIndex() {
        return displayedPeople.indexOf(selectedPerson.get());
    }

    public ReadOnlyObjectProperty<Person> selectedPersonProperty() {
        return selectedPerson.getReadOnlyProperty();
    }

    public ReadOnlyIntegerProperty totalCountProperty() {
        return totalCount.getReadOnlyProperty();
    }

    public int getDisplayedCount() {
        return displayedPeople.size();
    }
}
