package seedu.address.commons.core;

import java.awt.Point;
import java.io.Serializable;
import java.util.Objects;

import seedu.address.commons.util.ToStringBuilder;

/**
 * A Serializable class that contains the GUI settings.
 * Guarantees: immutable.
 */
public class GuiSettings implements Serializable {

    private static final double DEFAULT_HEIGHT = 600;
    private static final double DEFAULT_WIDTH = 740;
    private static final double DEFAULT_TERMINAL_HEIGHT = 320;

    private final double windowWidth;
    private final double windowHeight;
    private final Point windowCoordinates;
    private final double terminalHeight;

    /**
     * Constructs a {@code GuiSettings} with the default height, width and position.
     */
    public GuiSettings() {
        windowWidth = DEFAULT_WIDTH;
        windowHeight = DEFAULT_HEIGHT;
        windowCoordinates = null; // null represent no coordinates
        terminalHeight = DEFAULT_TERMINAL_HEIGHT;
    }

    /**
     * Constructs a {@code GuiSettings} with the specified height, width and position.
     */
    public GuiSettings(double windowWidth, double windowHeight, int xPosition, int yPosition) {
        this(windowWidth, windowHeight, xPosition, yPosition, DEFAULT_TERMINAL_HEIGHT);
    }

    /** Constructs settings with a remembered terminal height. */
    public GuiSettings(double windowWidth, double windowHeight, int xPosition, int yPosition,
            double terminalHeight) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        windowCoordinates = new Point(xPosition, yPosition);
        this.terminalHeight = terminalHeight;
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public Point getWindowCoordinates() {
        return windowCoordinates != null ? new Point(windowCoordinates) : null;
    }

    public double getTerminalHeight() {
        return terminalHeight == 0 ? DEFAULT_TERMINAL_HEIGHT : terminalHeight;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof GuiSettings otherGuiSettings)) {
            return false;
        }

        return windowWidth == otherGuiSettings.windowWidth
                && windowHeight == otherGuiSettings.windowHeight
                && Objects.equals(windowCoordinates, otherGuiSettings.windowCoordinates)
                && getTerminalHeight() == otherGuiSettings.getTerminalHeight();
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowWidth, windowHeight, windowCoordinates, getTerminalHeight());
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this)
                .add("windowWidth", windowWidth)
                .add("windowHeight", windowHeight)
                .add("windowCoordinates", windowCoordinates);
        if (getTerminalHeight() != DEFAULT_TERMINAL_HEIGHT) {
            result.add("terminalHeight", getTerminalHeight());
        }
        return result.toString();
    }
}
