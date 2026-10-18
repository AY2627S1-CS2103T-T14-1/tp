package seedu.address.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;

/** Compact card for a client in the left-hand list. */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final DateTimeFormatter NEXT_SESSION_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM");

    public final Person person;

    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label phone;
    @FXML
    private FlowPane tags;
    @FXML
    private Label meta;

    /** Creates a compact card for {@code person}. */
    public PersonCard(Person person, int displayedIndex) {
        super(FXML);
        this.person = person;
        id.setText(String.valueOf(displayedIndex));
        name.setText(person.getName().fullName);
        phone.setText(person.getPhone().value);
        person.getTags().stream().sorted(Comparator.comparing(tag -> tag.tagName)).limit(2).forEach(tag -> {
            Label tagLabel = new Label(tag.tagName);
            tagLabel.getStyleClass().add("tag");
            tags.getChildren().add(tagLabel);
        });
        meta.setText(planSummary(person) + " · " + nextSessionSummary(person));
    }

    private static String planSummary(Person person) {
        int exerciseCount = person.getWorkoutPlan().size();
        return exerciseCount == 0 ? "No plan"
                : exerciseCount + " exercise" + (exerciseCount == 1 ? "" : "s");
    }

    private static String nextSessionSummary(Person person) {
        LocalDateTime now = LocalDateTime.now();
        return person.getTrainingSessions().stream()
                .filter(session -> !session.getDateTime().isBefore(now))
                .min(Comparator.comparing(TrainingSession::getDateTime))
                .map(session -> "Next " + session.getDateTime().format(NEXT_SESSION_FORMAT))
                .orElse("No session booked");
    }
}
