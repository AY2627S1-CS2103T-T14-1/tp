package seedu.address.logic.commands;

/**
 * Marks commands that mutate membership plans or client assignments.
 * Used by {@code LogicManager} to map save failures to the membership-specific
 * error message and to restore the pre-command model state.
 */
public interface MembershipCommand {

    /**
     * Reported when a membership change cannot be saved.
     * No plan, assignment, or balance is changed in this case.
     */
    String MESSAGE_SAVE_FAILURE =
            "The membership change could not be saved. No plan, assignment, or balance was changed.";
}
