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
    private Region emptyContent;
    @FXML
    private VBox dietContent;
    @FXML
    private Label dietTarget;
    @FXML
    private Label dietUnit;
    @FXML
    private VBox macroSection;
    @FXML
    private HBox macroSplit;
    @FXML
    private Region macroProtein;
    @FXML
    private Region macroCarbs;
    @FXML
    private Region macroFat;
    @FXML
    private Label proteinGrams;
    @FXML
    private Label carbsGrams;
    @FXML
    private Label fatGrams;
    @FXML
    private Label proteinShare;
    @FXML
    private Label carbsShare;
    @FXML
    private Label fatShare;
    @FXML
    private VBox dietNotesBox;
    @FXML
    private Label dietNotes;
    @FXML
    private Region dietEmpty;
    @FXML
    private VBox dietaryRestrictionsBox;
    @FXML
    private Label dietaryRestrictions;
    @FXML
    private VBox sessionRows;
    @FXML
    private Region sessionsEmpty;
    @FXML
    private VBox historyRows;
    @FXML
    private Region historyEmpty;

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

        showDiet(person);
        showDietaryRestrictions(person);
        showSessions(person);
        showHistory(person);
    }

    private void showDiet(Person person) {
        boolean hasDiet = person.getDietPlan().isPresent();
        setShown(dietContent, hasDiet);
        setShown(dietEmpty, !hasDiet);
        if (!hasDiet) {
            return;
        }

        DietPlan plan = person.getDietPlan().orElseThrow();
        boolean structured = plan.getCalories().isPresent();
        dietTarget.setText(structured ? String.valueOf(plan.getCalories().orElseThrow()) : plan.getTarget());
        if (structured && !dietTarget.getStyleClass().contains("metric")) {
            dietTarget.getStyleClass().add("metric");
        } else if (!structured) {
            dietTarget.getStyleClass().remove("metric");
        }
        setShown(dietUnit, structured);
        setShown(macroSection, structured);
        if (structured) {
            int calories = plan.getCalories().orElseThrow();
            int proteinPct = showMacro(plan.getProteinGrams(), calories, 4, proteinGrams, proteinShare, macroProtein);
            int carbsPct = showMacro(plan.getCarbsGrams(), calories, 4, carbsGrams, carbsShare, macroCarbs);
            int fatPct = showMacro(plan.getFatGrams(), calories, 9, fatGrams, fatShare, macroFat);
            int proteinWidth = Math.min(100, proteinPct);
            int carbsWidth = Math.min(100 - proteinWidth, carbsPct);
            int fatWidth = Math.min(100 - proteinWidth - carbsWidth, fatPct);
            bindMacroWidth(macroProtein, proteinWidth);
            bindMacroWidth(macroCarbs, carbsWidth);
            bindMacroWidth(macroFat, fatWidth);
        }
        plan.getNotes().ifPresentOrElse(notes -> {
            dietNotes.setText(notes);
            setShown(dietNotesBox, true);
        }, () -> setShown(dietNotesBox, false));
    }

    private int showMacro(java.util.Optional<Integer> grams, int calories, int kcalPerGram,
            Label value, Label share, Region bar) {
        boolean present = grams.isPresent() && grams.orElseThrow() > 0;
        int percentage = present ? (int) Math.round(grams.orElseThrow() * kcalPerGram * 100.0 / calories) : 0;
        value.setText(present ? grams.orElseThrow() + " g" : "—");
        share.setText(present ? percentage + "% of kcal" : "");
        setShown(bar, present);
        return percentage;
    }

    private void bindMacroWidth(Region bar, int percentage) {
        bar.prefWidthProperty().unbind();
        bar.prefWidthProperty().bind(macroSplit.widthProperty().subtract(4).multiply(percentage / 100.0));
    }

    private void showDietaryRestrictions(Person person) {
        List<String> restrictions = person.getDietaryRestrictions().getValues();
        boolean hasRestrictions = !restrictions.isEmpty();
        dietaryRestrictions.setText(hasRestrictions ? String.join("; ", restrictions) : "");
        setShown(dietaryRestrictionsBox, hasRestrictions);
    }

    private void showSessions(Person person) {
        List<TrainingSession> sessions = person.getTrainingSessions().stream()
                .sorted(Comparator.comparing(TrainingSession::getDateTime)).toList();
        sessionRows.getChildren().clear();
        setShown(sessionRows, !sessions.isEmpty());
        setShown(sessionsEmpty, sessions.isEmpty());
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
        detailColumn.setHgrow(Priority.ALWAYS);
        ColumnConstraints timeColumn = new ColumnConstraints();
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
        details.setMinWidth(0);

        Label time = new Label(dateTime.format(TIME_FORMAT));
        time.getStyleClass().addAll("session-time", "mono");
        row.add(dateBlock, 0, 0);
        row.add(details, 1, 0);
        row.add(time, 2, 0);
        return row;
    }

    private void showHistory(Person person) {
        Comparator<WorkoutSession> newestFirst =
                Comparator.comparing(WorkoutSession::getDate, Comparator.reverseOrder())
                .thenComparing(session -> session.getTime().isPresent())
                .thenComparing(session -> session.getTime().orElse(LocalTime.MIN), Comparator.reverseOrder());
        List<WorkoutSession> sessions = person.getWorkoutSessions().stream().sorted(newestFirst).toList();
        historyRows.getChildren().clear();
        setShown(historyRows, !sessions.isEmpty());
        setShown(historyEmpty, sessions.isEmpty());
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
        date.setWrapText(true);
        date.setMinWidth(0);
        date.setMaxWidth(Double.MAX_VALUE);
        Label time = new Label(session.getTime().map(value -> value.format(TIME_FORMAT)).orElse("Time not specified"));
        time.getStyleClass().addAll("history-time", "mono");
        time.setWrapText(true);
        time.setMinWidth(0);
        time.setMaxWidth(Double.MAX_VALUE);

        VBox details = new VBox(3, date, time);
        details.setMinWidth(0);
        for (WorkoutLogEntry entry : session.getEntries()) {
            Label exercise = new Label(formatLogEntry(entry));
            exercise.getStyleClass().addAll("history-exercise", "mono");
            exercise.setWrapText(true);
            exercise.setMinWidth(0);
            exercise.setMaxWidth(Double.MAX_VALUE);
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
