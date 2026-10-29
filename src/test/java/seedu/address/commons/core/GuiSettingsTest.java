package seedu.address.commons.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class GuiSettingsTest {
    @Test
    public void toStringMethod() {
        GuiSettings guiSettings = new GuiSettings();
        String expected = GuiSettings.class.getCanonicalName() + "{windowWidth=" + guiSettings.getWindowWidth()
                + ", windowHeight=" + guiSettings.getWindowHeight() + ", windowCoordinates="
                + guiSettings.getWindowCoordinates() + "}";
        assertEquals(expected, guiSettings.toString());
    }

    @Test
    public void toStringMethod_customTerminalHeight_includesTerminalHeight() {
        GuiSettings guiSettings = new GuiSettings(1280, 800, 10, 20, 400);
        assertTrue(guiSettings.toString().endsWith(", terminalHeight=400.0}"));
    }

    @Test
    public void defaultSettings_haveNoCoordinatesAndDefaultTerminalHeight() {
        GuiSettings guiSettings = new GuiSettings();
        assertNull(guiSettings.getWindowCoordinates());
        assertEquals(320, guiSettings.getTerminalHeight());
    }

    @Test
    public void getTerminalHeight_fourArgumentConstructor_usesDefault() {
        assertEquals(320, new GuiSettings(1280, 800, 10, 20).getTerminalHeight());
    }

    @Test
    public void getTerminalHeight_rememberedHeight_isReturned() {
        assertEquals(450, new GuiSettings(1280, 800, 10, 20, 450).getTerminalHeight());
    }

    @Test
    public void getTerminalHeight_unsetHeight_fallsBackToDefault() {
        assertEquals(320, new GuiSettings(1280, 800, 10, 20, 0).getTerminalHeight());
    }

    @Test
    public void equals() {
        GuiSettings settings = new GuiSettings(1280, 800, 10, 20, 400);

        assertTrue(settings.equals(settings));
        assertTrue(settings.equals(new GuiSettings(1280, 800, 10, 20, 400)));

        assertFalse(settings.equals(null));
        assertFalse(settings.equals(1));
        assertFalse(settings.equals(new GuiSettings(1300, 800, 10, 20, 400)));
        assertFalse(settings.equals(new GuiSettings(1280, 900, 10, 20, 400)));
        assertFalse(settings.equals(new GuiSettings(1280, 800, 11, 20, 400)));
        assertFalse(settings.equals(new GuiSettings(1280, 800, 10, 20, 500)));
        assertFalse(new GuiSettings().equals(new GuiSettings(740, 600, 0, 0)));
    }

    @Test
    public void equals_unsetTerminalHeightMatchesDefault() {
        assertEquals(new GuiSettings(1280, 800, 10, 20), new GuiSettings(1280, 800, 10, 20, 0));
    }

    @Test
    public void hashCode_followsEquals() {
        assertEquals(new GuiSettings(1280, 800, 10, 20, 400).hashCode(),
                new GuiSettings(1280, 800, 10, 20, 400).hashCode());
        assertNotEquals(new GuiSettings(1280, 800, 10, 20, 400).hashCode(),
                new GuiSettings(1280, 800, 10, 20, 500).hashCode());
    }
}
