package seedu.address.ui;

import java.util.List;

/** A single entry in the command result history shown by the UI. */
public record CommandFeedback(String command, Kind kind, String message, List<String> details) {

    /** Visual status of a command result. */
    public enum Kind {
        SUCCESS,
        ERROR,
        INFO,
        WARNING
    }

    /** Creates an immutable feedback entry. */
    public CommandFeedback {
        details = List.copyOf(details);
    }
}
