package seedu.address.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import seedu.address.logic.commands.ClearPlanCommand;
import seedu.address.logic.commands.CommandResult;

/** Displays a scrolling history of command results. */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final int MAX_ENTRIES = 50;

    private final ObservableList<CommandFeedback> feedbackEntries = FXCollections.observableArrayList();

    @FXML
    private ListView<CommandFeedback> resultList;

    /** Creates an empty command-result history. */
    public ResultDisplay() {
        super(FXML);
        resultList.setItems(feedbackEntries);
        resultList.setCellFactory(unused -> new FeedbackCell());
    }

    /** Appends a successful result returned by the logic layer. */
    public void appendResult(String command, CommandResult result) {
        CommandFeedback.Kind kind = result.isShowHelp()
                ? CommandFeedback.Kind.INFO : classifySuccessfulMessage(result.getFeedbackToUser());
        ParsedFeedback parsed = parseFeedback(result.getFeedbackToUser());
        List<String> details = new ArrayList<>(parsed.details());
        if (result.isShowHelp()) {
            details.addAll(CommandCatalog.helpDetails());
        }
        append(new CommandFeedback(command, kind, parsed.message(), details));
    }

    /** Appends an error thrown while parsing or executing a command. */
    public void appendError(String command, String feedback) {
        ParsedFeedback parsed = parseFeedback(feedback);
        append(new CommandFeedback(command, CommandFeedback.Kind.ERROR, parsed.message(), parsed.details()));
    }

    /** Appends an informational entry not produced by a typed command. */
    public void appendInfo(String command, String message, List<String> details) {
        append(new CommandFeedback(command, CommandFeedback.Kind.INFO, message, details));
    }

    private void append(CommandFeedback feedback) {
        feedbackEntries.add(feedback);
        if (feedbackEntries.size() > MAX_ENTRIES) {
            feedbackEntries.remove(0, feedbackEntries.size() - MAX_ENTRIES);
        }
        resultList.scrollTo(feedbackEntries.size() - 1);
    }

    private static CommandFeedback.Kind classifySuccessfulMessage(String message) {
        if (message.startsWith("Workout plan is already empty")
                || message.startsWith(ClearPlanCommand.MESSAGE_PLAN_ALREADY_EMPTY.split("%", 2)[0])) {
            return CommandFeedback.Kind.WARNING;
        }
        return CommandFeedback.Kind.SUCCESS;
    }

    private static ParsedFeedback parseFeedback(String feedback) {
        List<String> lines = Arrays.asList(feedback.split("\\R", -1));
        String message = lines.isEmpty() ? "" : lines.get(0);
        List<String> details = lines.size() <= 1 ? List.of() : lines.subList(1, lines.size()).stream()
                .filter(line -> !line.isBlank()).toList();
        return new ParsedFeedback(message, details);
    }

    ObservableList<CommandFeedback> getFeedbackEntries() {
        return FXCollections.unmodifiableObservableList(feedbackEntries);
    }

    private record ParsedFeedback(String message, List<String> details) {
    }

    /** Renders one command result with a coloured status marker. */
    private static class FeedbackCell extends ListCell<CommandFeedback> {
        @Override
        protected void updateItem(CommandFeedback feedback, boolean empty) {
            super.updateItem(feedback, empty);
            if (empty || feedback == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            String kindClass = feedback.kind().name().toLowerCase();
            Label icon = new Label(iconFor(feedback.kind()));
            icon.getStyleClass().addAll("result-icon", kindClass);
            icon.setMinSize(22, 22);
            icon.setPrefSize(22, 22);
            icon.setAlignment(Pos.CENTER);

            Label command = new Label("› " + feedback.command());
            command.getStyleClass().addAll("result-command", "mono");
            command.setMaxWidth(Double.MAX_VALUE);
            command.setWrapText(true);

            Label message = new Label(feedback.message());
            message.getStyleClass().addAll("result-message", kindClass);
            message.setWrapText(true);

            VBox text = new VBox(2, command, message);
            text.setMinWidth(0);
            feedback.details().forEach(detail -> {
                Label detailLabel = new Label(detail);
                detailLabel.getStyleClass().addAll("result-detail", "mono");
                detailLabel.setWrapText(true);
                text.getChildren().add(detailLabel);
            });
            HBox.setHgrow(text, Priority.ALWAYS);

            HBox row = new HBox(10, icon, text);
            row.setAlignment(Pos.TOP_LEFT);
            row.getStyleClass().add("result-row");
            setGraphic(row);
            setText(null);
        }

        private static String iconFor(CommandFeedback.Kind kind) {
            return switch (kind) {
                case SUCCESS -> "✓";
                case ERROR -> "!";
                case INFO -> "?";
                case WARNING -> "–";
            };
        }
    }
}
