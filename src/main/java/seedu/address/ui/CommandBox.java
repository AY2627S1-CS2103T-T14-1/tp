package seedu.address.ui;

import java.util.ArrayList;
import java.util.List;

import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;

/** Receives command input and presents command history and contextual usage hints. */
public class CommandBox extends UiPart<Region> {

    private static final String FXML = "CommandBox.fxml";
    private static final PseudoClass FOCUSED_FIELD = PseudoClass.getPseudoClass("focused-field");
    private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

    private final CommandExecutor commandExecutor;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex;

    @FXML
    private HBox commandBar;
    @FXML
    private TextField commandTextField;
    @FXML
    private Label hintLabel;
    @FXML
    private FlowPane hintContainer;

    /** Creates a command box backed by {@code commandExecutor}. */
    public CommandBox(CommandExecutor commandExecutor) {
        super(FXML);
        this.commandExecutor = commandExecutor;
        historyIndex = 0;

        commandTextField.focusedProperty().addListener((unused, oldValue, focused) ->
                commandBar.pseudoClassStateChanged(FOCUSED_FIELD, focused));
        commandTextField.textProperty().addListener((unused, oldValue, newValue) -> {
            setErrorState(false);
            updateHints(newValue);
        });
        updateHints("");
    }

    @FXML
    private void handleCommandEntered() {
        String commandText = commandTextField.getText().trim();
        if (commandText.isEmpty()) {
            return;
        }

        commandHistory.add(commandText);
        historyIndex = commandHistory.size();
        try {
            commandExecutor.execute(commandText);
            commandTextField.clear();
        } catch (CommandException | ParseException e) {
            setErrorState(true);
        }
    }

    @FXML
    private void handleHistoryKey(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            showPreviousCommand();
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            showNextCommand();
            event.consume();
        }
    }

    private void showPreviousCommand() {
        if (commandHistory.isEmpty()) {
            return;
        }
        historyIndex = Math.max(0, historyIndex - 1);
        commandTextField.setText(commandHistory.get(historyIndex));
        commandTextField.positionCaret(commandTextField.getText().length());
    }

    private void showNextCommand() {
        if (commandHistory.isEmpty() || historyIndex >= commandHistory.size()) {
            return;
        }
        historyIndex++;
        if (historyIndex == commandHistory.size()) {
            commandTextField.clear();
        } else {
            commandTextField.setText(commandHistory.get(historyIndex));
            commandTextField.positionCaret(commandTextField.getText().length());
        }
    }

    private void setErrorState(boolean hasError) {
        commandBar.pseudoClassStateChanged(ERROR, hasError);
    }

    private void updateHints(String commandText) {
        hintContainer.getChildren().clear();
        String trimmed = commandText.trim();
        if (trimmed.isEmpty()) {
            hintLabel.setText("Try");
            CommandCatalog.defaultSuggestions().forEach(suggestion ->
                    addHint(suggestion.label(), suggestion.command(), suggestion.label()));
            return;
        }

        String commandWord = trimmed.split("\\s+", 2)[0];
        List<CommandCatalog.Entry> matches = CommandCatalog.matching(commandWord);
        hintLabel.setText(matches.isEmpty() ? "No matching command" : "Usage");
        matches.forEach(entry -> addHint(firstLine(entry.usage()), entry.word() + " ", entry.usage()));
    }

    private void addHint(String label, String command, String tooltip) {
        Button hint = new Button(label);
        hint.getStyleClass().add("hint-chip");
        hint.setFocusTraversable(false);
        hint.setTooltip(new Tooltip(tooltip));
        hint.setOnAction(event -> {
            commandTextField.setText(command);
            commandTextField.positionCaret(command.length());
            commandTextField.requestFocus();
        });
        hintContainer.getChildren().add(hint);
    }

    private static String firstLine(String text) {
        int newline = text.indexOf('\n');
        return newline < 0 ? text : text.substring(0, newline);
    }

    /** Focuses the command text field. */
    public void requestFocus() {
        commandTextField.requestFocus();
    }

    /** Returns the command field for UI tests. */
    TextField getCommandTextField() {
        return commandTextField;
    }

    /** Executes command text and returns its logic result. */
    @FunctionalInterface
    public interface CommandExecutor {
        CommandResult execute(String commandText) throws CommandException, ParseException;
    }
}
