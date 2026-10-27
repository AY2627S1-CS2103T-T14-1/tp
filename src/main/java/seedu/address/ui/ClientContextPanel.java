package seedu.address.ui;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutLogEntry;
import seedu.address.model.person.WorkoutSession;

/** Right-hand context column for diet, sessions, and completed workout history. */
public class ClientContextPanel extends UiPart<Region> {

    private static final String FXML = "ClientContextPanel.fxml";
    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.ofPattern("EEE");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMM");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM yyyy");

    @FXML
    private VBox selectedContent;
    @FXML
    private VBox emptyContent;
    @FXML
    private Label dietCommandHint;
    @FXML
    private VBox dietContent;
    @FXML
    private Label dietTarget;
    @FXML
    private VBox dietNotesBox;
    @FXML
    private Label dietNotes;
    @FXML
    private VBox dietEmpty;
    @FXML
    private Label dietEmptyCommand;
    @FXML
    private VBox dietaryRestrictionsBox;
    @FXML
    private Label dietaryRestrictions;
    @FXML
    private Label sessionCommandHint;
    @FXML
    private VBox sessionRows;
    @FXML
    private VBox sessionsEmpty;
    @FXML
    private Label sessionsEmptyCommand;
    @FXML
    private Label historyCommandHint;
    @FXML
    private VBox historyRows;
    @FXML
    private VBox historyEmpty;
    @FXML
    private Label historyEmptyCommand;

    /** Creates an initially empty client context panel. */
    public ClientContextPanel() {
        super(FXML);
        showPerson(null, -1);
    }

    /** Displays context records for {@code person}. */
    public void showPerson(Person person, int displayedIndex) {
        boolean hasPerson = person != null;
        setShown(selectedContent, hasPerson);
        setShown(emptyContent, !hasPerson);
        if (!hasPerson) {
            return;
        }

        dietCommandHint.setText("diet " + displayedIndex);
        sessionCommandHint.setText("schedule " + displayedIndex);
        historyCommandHint.setText("history " + displayedIndex);
        showDiet(person, displayedIndex);
        showDietaryRestrictions(person);
        showSessions(person, displayedIndex);
        showHistory(person, displayedIndex);
    }

    private void showDiet(Person person, int displayedIndex) {
        boolean hasDiet = person.getDietPlan().isPresent();
        setShown(dietContent, hasDiet);
        setShown(dietEmpty, !hasDiet);
        dietEmptyCommand.setText("diet " + displayedIndex + " t/1800 kcal per day n/Prioritise vegetables");
        if (!hasDiet) {
            return;
        }

        DietPlan plan = person.getDietPlan().orElseThrow();
        dietTarget.setText(plan.getTarget());
        plan.getNotes().ifPresentOrElse(notes -> {
            dietNotes.setText(notes);
            setShown(dietNotesBox, true);
        }, () -> setShown(dietNotesBox, false));
    }

    private void showDietaryRestrictions(Person person) {
        List<String> restrictions = person.getDietaryRestrictions().getValues();
        boolean hasRestrictions = !restrictions.isEmpty();
        dietaryRestrictions.setText(hasRestrictions ? String.join("; ", restrictions) : "");
        setShown(dietaryRestrictionsBox, hasRestrictions);
    }

    private void showSessions(Person person, int displayedIndex) {
        List<TrainingSession> sessions = person.getTrainingSessions().stream()
                .sorted(Comparator.comparing(TrainingSession::getDateTime)).toList();
        sessionRows.getChildren().clear();
        setShown(sessionRows, !sessions.isEmpty());
        setShown(sessionsEmpty, sessions.isEmpty());
        sessionsEmptyCommand.setText("schedule " + displayedIndex
                + " d/2099-03-15 tm/14:30 l/Main gym");
        if (sessions.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean nextMarked = false;
        for (TrainingSession session : sessions) {
            boolean past = session.getDateTime().isBefore(now);
            boolean next = !past && !nextMarked;
            if (next) {
                nextMarked = true;
            }
            sessionRows.getChildren().add(createSessionRow(session, past, next));
        }
    }

    private GridPane createSessionRow(TrainingSession session, boolean past, boolean next) {
        GridPane row = new GridPane();
        row.getStyleClass().add("session-row");
        if (past) {
            row.getStyleClass().add("past");
        } else if (next) {
            row.getStyleClass().add("next");
        }
        row.setHgap(10);
        row.setAlignment(Pos.CENTER_LEFT);

        ColumnConstraints dateColumn = new ColumnConstraints(48, 48, 48);
        ColumnConstraints detailColumn = new ColumnConstraints();
        detailColumn.setMinWidth(0);
        detailColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints timeColumn = new ColumnConstraints();
        timeColumn.setMinWidth(Region.USE_PREF_SIZE);
        row.getColumnConstraints().addAll(dateColumn, detailColumn, timeColumn);

        LocalDateTime dateTime = session.getDateTime();
        Label dow = new Label(dateTime.format(DAY_FORMAT).toUpperCase());
        dow.getStyleClass().add("session-dow");
        Label day = new Label(String.valueOf(dateTime.getDayOfMonth()));
        day.getStyleClass().add("session-day");
        Label month = new Label(dateTime.format(MONTH_FORMAT).toUpperCase());
        month.getStyleClass().add("session-month");
        VBox dateBlock = new VBox(0, dow, day, month);
        dateBlock.setAlignment(Pos.CENTER);

        Label location = new Label(session.getLocation());
        location.getStyleClass().add("session-location");
        location.setWrapText(true);
        boolean meeting = session.getKind() == TrainingSession.Kind.MEETING;
        String statusText = past ? (meeting ? "Past meeting" : "Past")
                : next ? (meeting ? "Next meeting" : "Next session")
                : (meeting ? "Upcoming meeting" : "Upcoming");
        Label status = new Label(statusText);
        status.getStyleClass().add("session-status");
        VBox details = new VBox(3, location, status);

        Label time = new Label(dateTime.format(TIME_FORMAT));
        time.getStyleClass().addAll("session-time", "mono");
        row.add(dateBlock, 0, 0);
        row.add(details, 1, 0);
        row.add(time, 2, 0);
        return row;
    }

    private void showHistory(Person person, int displayedIndex) {
        Comparator<WorkoutSession> newestFirst =
                Comparator.comparing(WorkoutSession::getDate, Comparator.reverseOrder())
                .thenComparing(session -> session.getTime().isPresent())
                .thenComparing(session -> session.getTime().orElse(LocalTime.MIN), Comparator.reverseOrder());
        List<WorkoutSession> sessions = person.getWorkoutSessions().stream().sorted(newestFirst).toList();
        historyRows.getChildren().clear();
        setShown(historyRows, !sessions.isEmpty());
        setShown(historyEmpty, sessions.isEmpty());
        historyEmptyCommand.setText("log " + displayedIndex
                + " d/2020-01-15 t/18:00 ex/Back squat|60|3|10");
        for (int i = 0; i < sessions.size(); i++) {
            historyRows.getChildren().add(createHistoryRow(sessions.get(i), i + 1));
        }
    }

    private HBox createHistoryRow(WorkoutSession session, int historyIndex) {
        Label icon = new Label("✓");
        icon.getStyleClass().add("history-icon");
        icon.setAlignment(Pos.CENTER);
        icon.setMinSize(22, 22);

        Label date = new Label(historyIndex + ". " + session.getDate().format(HISTORY_DATE_FORMAT));
        date.getStyleClass().add("history-date");
        Label time = new Label(session.getTime().map(value -> value.format(TIME_FORMAT)).orElse("Time not specified"));
        time.getStyleClass().addAll("history-time", "mono");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox heading = new HBox(8, date, spacer, time);
        heading.setAlignment(Pos.BASELINE_LEFT);

        VBox details = new VBox(3, heading);
        for (WorkoutLogEntry entry : session.getEntries()) {
            Label exercise = new Label(formatLogEntry(entry));
            exercise.getStyleClass().addAll("history-exercise", "mono");
            exercise.setWrapText(true);
            details.getChildren().add(exercise);
        }
        HBox.setHgrow(details, Priority.ALWAYS);
        HBox row = new HBox(10, icon, details);
        row.getStyleClass().add("history-row");
        row.setAlignment(Pos.TOP_LEFT);
        return row;
    }

    private static String formatLogEntry(WorkoutLogEntry entry) {
        String weight = entry.getWeightKg().compareTo(BigDecimal.ZERO) == 0
                ? "BW" : entry.getWeightKg().toPlainString() + " kg";
        return entry.getName() + " · " + entry.getSets() + " × " + entry.getReps() + " · " + weight;
    }

    private static void setShown(Region region, boolean shown) {
        region.setVisible(shown);
        region.setManaged(shown);
    }
}
