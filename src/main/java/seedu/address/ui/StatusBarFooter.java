package seedu.address.ui;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.beans.value.ObservableIntegerValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

/** Footer showing the save location and current saved-client count. */
public class StatusBarFooter extends UiPart<Region> {

    private static final String FXML = "StatusBarFooter.fxml";

    @FXML
    private Label saveLocationStatus;
    @FXML
    private Label saveStateStatus;

    /** Creates a status footer bound to the total client count. */
    public StatusBarFooter(Path saveLocation, ObservableIntegerValue totalClientCount) {
        super(FXML);
        saveLocationStatus.setText(Paths.get(".").resolve(saveLocation).toString());
        saveStateStatus.setText("Saved");
    }
}
