package seedu.address.ui;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;

/** Compact card for a client in the left-hand list. */
public class PersonCard extends UiPart<Region> {

    private static final String FXML = "PersonListCard.fxml";
    private static final DateTimeFormatter NEXT_SESSION_FORMAT =
            DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

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
        displayMembership();
        meta.setText(planSummary(person) + " · " + nextSessionSummary(person));
    }

    /**
     * Adds the client's membership as a single tag reading
     * {@code Plan_Name Member: N sessions left}, with {@code [expires YYYY-MM-DD]}
     * appended when the assignment defines an expiry date. The tag joins the other
     * tags in the {@code tags} flow pane so they render side by side under the name;
     * nothing is added when the client has no assigned plan.
     * The tag's width is capped at the flow pane's width so a permitted long plan name
     * wraps instead of overflowing the card.
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
            tag.setMinWidth(0);
            tags.setMinWidth(0);
            tag.maxWidthProperty().bind(tags.widthProperty());
            tags.getChildren().add(tag);
        });
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
