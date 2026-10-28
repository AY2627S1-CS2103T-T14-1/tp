package seedu.address.ui;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.AddExerciseCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.ClearPlanCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DeleteExerciseCommand;
import seedu.address.logic.commands.DietaryRestrictionsCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.commands.HealthCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.PayCommand;
import seedu.address.logic.commands.RedoCommand;
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.commands.UndoCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/** Main Fittix desktop workspace. */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final double MIN_WIDTH = 1280;
    private static final double MIN_HEIGHT = 800;
    private static final DateTimeFormatter HEADER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter FULL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter CLOCK_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final Set<String> INDEX_COMMANDS = Set.of(
            "edit", "delete", "diet", "remark", "schedule", "viewschedule",
            "addex", "delex", "clearplan", "dayex", "log", "editlog", "history", PayCommand.COMMAND_WORD,
            DietaryRestrictionsCommand.COMMAND_WORD, HealthCommand.COMMAND_WORD);
    private static final Set<String> RESET_FILTER_COMMANDS = Set.of(
            AddCommand.COMMAND_WORD, EditCommand.COMMAND_WORD, ClearCommand.COMMAND_WORD,
            ListCommand.COMMAND_WORD, RemarkCommand.COMMAND_WORD, AddExerciseCommand.COMMAND_WORD,
            DeleteExerciseCommand.COMMAND_WORD, SetExerciseDayCommand.COMMAND_WORD);

    private final Logger logger = LogsCenter.getLogger(getClass());
    private final Stage primaryStage;
    private final Logic logic;
    private final Path dataFilePath;

    private PersonListPanel personListPanel;
    private ClientDetailPanel clientDetailPanel;
    private ClientContextPanel clientContextPanel;
    private ResultDisplay resultDisplay;
    private CommandBox commandBox;
    private HelpWindow helpWindow;
    private String filterQuery;
    private double terminalHeight;
    private boolean clockExpanded;
    private Timeline clockTimeline;
    private Tooltip dateTooltip;

    @FXML
    private StackPane brandIcon;
    @FXML
    private StackPane personListPanelPlaceholder;
    @FXML
    private StackPane clientDetailPanelPlaceholder;
    @FXML
    private StackPane clientContextPanelPlaceholder;
    @FXML
    private StackPane resultDisplayPlaceholder;
    @FXML
    private StackPane commandBoxPlaceholder;
    @FXML
    private StackPane statusbarPlaceholder;
    @FXML
    private HBox filterPill;
    @FXML
    private Label filterStateText;
    @FXML
    private Label filterCountText;
    @FXML
    private Label currentDate;
    @FXML
    private Label currentTime;
    @FXML
    private HBox dateToggle;
    @FXML
    private SplitPane workspace;
    @FXML
    private ScrollPane contentScroll;
    @FXML
    private VBox terminalPanel;
    @FXML
    private HBox terminalClosedBar;

    /** Creates the main window. */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        setWindowDefaultSize(logic.getGuiSettings());
        terminalHeight = clampTerminalHeight(logic.getGuiSettings().getTerminalHeight());
        workspace.getItems().remove(terminalPanel);
        brandIcon.getChildren().add(FittixIcon.createNode());
        setAccelerators();
        dateTooltip = new Tooltip();
        Tooltip.install(dateToggle, dateTooltip);
        refreshDateTime();
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), unused -> refreshDateTime()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        installKeyboardHandlers();
        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        primaryStage.getScene().getAccelerators().put(KeyCombination.valueOf("F1"), this::handleHelp);
    }

    private void installKeyboardHandlers() {
        primaryStage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && isTerminalOpen()) {
                Node focused = primaryStage.getScene().getFocusOwner();
                if (focused == null || focused instanceof TextInputControl) {
                    closeTerminal();
                    event.consume();
                }
            }
        });
        primaryStage.getScene().addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (isTerminalOpen() || commandBox == null || event.isControlDown()
                    || event.isMetaDown() || event.isAltDown()
                    || primaryStage.getScene().getFocusOwner() instanceof TextInputControl) {
                return;
            }
            String character = event.getCharacter();
            if (character == null || character.isEmpty() || character.codePoints()
                    .anyMatch(Character::isISOControl)) {
                return;
            }
            openTerminal();
            commandBox.insertText(character);
            event.consume();
        });
    }

    private static double clampTerminalHeight(double height) {
        return Math.min(640, Math.max(170, height));
    }

    private boolean isTerminalOpen() {
        return workspace.getItems().contains(terminalPanel);
    }

    void openTerminal() {
        if (!isTerminalOpen()) {
            workspace.getItems().add(terminalPanel);
            terminalClosedBar.setVisible(false);
            terminalClosedBar.setManaged(false);
            Platform.runLater(() -> {
                if (isTerminalOpen() && workspace.getHeight() > 0) {
                    workspace.setDividerPositions(1 - terminalHeight / workspace.getHeight());
                }
                commandBox.requestFocus();
            });
        } else {
            commandBox.requestFocus();
        }
    }

    private void closeTerminal() {
        if (!isTerminalOpen()) {
            return;
        }
        terminalHeight = clampTerminalHeight(terminalPanel.getHeight());
        workspace.getItems().remove(terminalPanel);
        terminalClosedBar.setVisible(true);
        terminalClosedBar.setManaged(true);
        contentScroll.requestFocus();
    }

    @FXML
    private void handleOpenTerminal() {
        openTerminal();
    }

    @FXML
    private void handleCloseTerminal() {
        closeTerminal();
    }

    @FXML
    private void handleClearFilter() {
        try {
            executeCommand("list");
        } catch (CommandException | ParseException exception) {
            logger.warning("Could not clear client filter: " + exception.getMessage());
        }
    }

    @FXML
    private void handleDateToggle() {
        clockExpanded = !clockExpanded;
        currentTime.setVisible(clockExpanded);
        currentTime.setManaged(clockExpanded);
        if (clockExpanded) {
            clockTimeline.play();
        } else {
            clockTimeline.stop();
        }
        refreshDateTime();
    }

    private void refreshDateTime() {
        LocalDateTime now = LocalDateTime.now();
        currentDate.setText(now.toLocalDate().format(clockExpanded ? FULL_DATE_FORMAT : HEADER_DATE_FORMAT));
        currentTime.setText(now.format(CLOCK_FORMAT));
        String hint = clockExpanded ? "Click to collapse" : "Click for full date and live time";
        dateToggle.setAccessibleText(hint);
        dateTooltip.setText(hint);
    }

    private void revealInContent(Node node) {
        Platform.runLater(() -> {
            Bounds bounds = contentScroll.getContent().sceneToLocal(node.localToScene(node.getBoundsInLocal()));
            double overflow = contentScroll.getContent().getBoundsInLocal().getHeight()
                    - contentScroll.getViewportBounds().getHeight();
            if (overflow > 0) {
                contentScroll.setVvalue(Math.max(0, Math.min(1, bounds.getMinY() / overflow)));
            }
        });
    }

    /** Fills the FXML placeholders and wires selection between the three columns. */
    void fillInnerParts() {
        personListPanel = new PersonListPanel(logic.getFilteredPersonList());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        clientDetailPanel = new ClientDetailPanel();
        clientDetailPanelPlaceholder.getChildren().add(clientDetailPanel.getRoot());
        clientContextPanel = new ClientContextPanel();
        clientContextPanelPlaceholder.getChildren().add(clientContextPanel.getRoot());

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());
        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath, personListPanel.totalCountProperty());
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        personListPanel.selectedPersonProperty().addListener((unused, previous, selected) ->
                refreshSelectedClient());
        logic.getFilteredPersonList().addListener((javafx.collections.ListChangeListener<Person>) change -> {
            refreshSelectedClient();
            refreshHeaderCounts();
        });
        personListPanel.totalCountProperty().addListener((unused, previous, current) -> refreshHeaderCounts());

        personListPanel.setFilterQuery(null);
        refreshSelectedClient();
        refreshFilterPill();
    }

    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setWidth(Math.max(MIN_WIDTH, guiSettings.getWindowWidth()));
        primaryStage.setHeight(Math.max(MIN_HEIGHT, guiSettings.getWindowHeight()));
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /** Runs help from the header and opens the existing guide window. */
    @FXML
    public void handleHelp() {
        if (commandBox == null) {
            showHelpWindow();
            return;
        }
        try {
            executeCommand("help");
        } catch (CommandException | ParseException exception) {
            logger.warning("Could not run help: " + exception.getMessage());
        }
    }

    private void showHelpWindow() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    void show() {
        primaryStage.show();
    }

    /** Closes Fittix and persists the current window geometry. */
    @FXML
    private void handleExit() {
        if (isTerminalOpen()) {
            terminalHeight = clampTerminalHeight(terminalPanel.getHeight());
        }
        clockTimeline.stop();
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY(), terminalHeight);
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        openTerminal();
        String commandWord = commandWord(commandText);
        Person previousSelection = personListPanel.getSelectedPerson();
        selectCommandTarget(commandText, commandWord);
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.info("Result: " + commandResult.getFeedbackToUser());
            resultDisplay.appendResult(commandText, commandResult);
            updateUiAfterSuccessfulCommand(commandText, commandWord, commandResult);
            if (commandResult.isShowHelp()) {
                showHelpWindow();
            }
            if (commandResult.isExit()) {
                handleExit();
            }
            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.info("An error occurred while executing command: " + commandText);
            resultDisplay.appendError(commandText, e.getMessage());
            personListPanel.selectPerson(previousSelection);
            throw e;
        }
    }

    private void selectCommandTarget(String commandText, String commandWord) {
        if (!INDEX_COMMANDS.contains(commandWord)) {
            return;
        }
        String[] tokens = commandText.trim().split("\\s+");
        if (tokens.length < 2) {
            return;
        }
        try {
            int oneBasedIndex = Integer.parseInt(tokens[1]);
            personListPanel.selectDisplayedIndex(oneBasedIndex - 1);
        } catch (NumberFormatException ignored) {
            // The parser will provide the user-facing validation message.
        }
    }

    private void updateUiAfterSuccessfulCommand(String commandText, String commandWord, CommandResult result) {
        if (FindCommand.COMMAND_WORD.equals(commandWord)) {
            filterQuery = commandText.substring(commandWord.length()).trim();
        } else if (RESET_FILTER_COMMANDS.contains(commandWord)
                || (UndoCommand.COMMAND_WORD.equals(commandWord)
                    && UndoCommand.MESSAGE_SUCCESS.equals(result.getFeedbackToUser()))
                || (RedoCommand.COMMAND_WORD.equals(commandWord)
                    && RedoCommand.MESSAGE_SUCCESS.equals(result.getFeedbackToUser()))
                || ClearPlanCommand.COMMAND_WORD.equals(commandWord)
                && result.getFeedbackToUser().startsWith("Cleared workout plan")) {
            filterQuery = null;
        }

        personListPanel.setFilterQuery(filterQuery);
        if (AddCommand.COMMAND_WORD.equals(commandWord)) {
            personListPanel.selectLast();
        }
        result.getBodyProgressViewRequest().ifPresent(request -> personListPanel.selectPerson(request.person()));
        refreshSelectedClient();
        result.getBodyProgressViewRequest().ifPresent(request -> {
            clientDetailPanel.revealBodyProgress(request);
            revealInContent(clientDetailPanel.getBodyProgressNode(request));
        });
        refreshFilterPill();
    }

    private void refreshSelectedClient() {
        if (clientDetailPanel == null || clientContextPanel == null) {
            return;
        }
        Person selected = personListPanel.getSelectedPerson();
        int displayedIndex = personListPanel.getSelectedIndex() + 1;
        clientDetailPanel.showPerson(selected, displayedIndex);
        clientContextPanel.showPerson(selected, displayedIndex);
    }

    private void refreshFilterPill() {
        boolean isFiltered = filterQuery != null;
        filterStateText.setText(isFiltered ? "find: " + filterQuery : "");
        filterPill.setVisible(isFiltered);
        filterPill.setManaged(isFiltered);
        refreshHeaderCounts();
    }

    private void refreshHeaderCounts() {
        if (personListPanel == null) {
            return;
        }
        filterCountText.setText(personListPanel.getDisplayedCount() + " of "
                + personListPanel.totalCountProperty().get());
    }

    private static String commandWord(String commandText) {
        return commandText.trim().split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
    }
}
