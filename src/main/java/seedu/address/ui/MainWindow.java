package seedu.address.ui;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
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
import seedu.address.logic.commands.RemarkCommand;
import seedu.address.logic.commands.SetExerciseDayCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/** Main Fittix desktop workspace. */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final double MIN_WIDTH = 1280;
    private static final double MIN_HEIGHT = 800;
    private static final PseudoClass FILTERED = PseudoClass.getPseudoClass("filtered");
    private static final DateTimeFormatter HEADER_DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH);
    private static final Set<String> INDEX_COMMANDS = Set.of(
            "edit", "delete", "diet", "remark", "schedule", "viewschedule",
            "addex", "delex", "clearplan", "dayex", "log", "editlog", "history",
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

    @FXML
    private MenuItem helpMenuItem;
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

    /** Creates the main window. */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;
        setWindowDefaultSize(logic.getGuiSettings());
        setAccelerators();
        currentDate.setText(LocalDate.now().format(HEADER_DATE_FORMAT));
        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
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
        Platform.runLater(commandBox::requestFocus);
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

    /** Opens the help window from the menu and records the action in result history. */
    @FXML
    public void handleHelp() {
        if (resultDisplay != null) {
            resultDisplay.appendInfo("Help menu", "Opened help window.", CommandCatalog.helpDetails());
        }
        showHelpWindow();
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
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
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
        result.getBodyProgressViewRequest().ifPresent(clientDetailPanel::revealBodyProgress);
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
        filterStateText.setText(isFiltered ? "find: " + filterQuery : "All clients");
        filterPill.pseudoClassStateChanged(FILTERED, isFiltered);
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
