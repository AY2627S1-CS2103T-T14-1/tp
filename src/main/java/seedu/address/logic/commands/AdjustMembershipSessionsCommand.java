package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.person.Person;

/**
 * Increases or decreases a client's remaining membership sessions.
 * Only the selected client's balance changes: the shared plan definition, the copied
 * plan name, the expiry date, and every other client are untouched.
 */
public class AdjustMembershipSessionsCommand extends Command {

    public static final String COMMAND_WORD_INCREASE = "imem";
    public static final String COMMAND_WORD_DECREASE = "dmem";

    public static final String MESSAGE_USAGE_INCREASE = COMMAND_WORD_INCREASE
            + ": Increases a client's remaining membership sessions.\n"
            + "Parameters: PERSON_INDEX NUMBER_TO_INCREMENT (a whole number from 1 to 1000)\n"
            + "Example: " + COMMAND_WORD_INCREASE + " 1 3";
    public static final String MESSAGE_USAGE_DECREASE = COMMAND_WORD_DECREASE
            + ": Decreases a client's remaining membership sessions.\n"
            + "Parameters: PERSON_INDEX NUMBER_TO_DECREMENT (a whole number from 1 to 1000)\n"
            + "Example: " + COMMAND_WORD_DECREASE + " 2 2";

    public static final String MESSAGE_INCREASE_SUCCESS =
            "Increased sessions for %1$s by %2$d; Sessions remaining: %3$d.";
    public static final String MESSAGE_DECREASE_SUCCESS =
            "Decreased sessions for %1$s by %2$d; Sessions remaining: %3$d.";
    public static final String MESSAGE_NO_ASSIGNMENT =
            "This client has no membership plan to remove or adjust.";
    public static final String MESSAGE_INSUFFICIENT_SESSIONS =
            "Cannot decrease sessions below zero. The balance remains unchanged.";
    public static final String MESSAGE_SESSION_LIMIT =
            "Cannot increase sessions above 1000. The balance remains unchanged.";

    private final Index targetIndex;
    private final int amount;
    private final boolean isIncrement;

    /**
     * Creates an AdjustMembershipSessionsCommand for the client at {@code targetIndex}.
     * When {@code isIncrement} is true the balance grows by {@code amount}, otherwise it shrinks.
     */
    public AdjustMembershipSessionsCommand(Index targetIndex, int amount, boolean isIncrement) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        this.amount = amount;
        this.isIncrement = isIncrement;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person client = displayedClients.get(targetIndex.getZeroBased());
        MembershipAssignment assignment = client.getMembershipAssignment()
                .orElseThrow(() -> new CommandException(MESSAGE_NO_ASSIGNMENT));

        int updatedBalance = isIncrement
                ? assignment.getRemainingSessions() + amount
                : assignment.getRemainingSessions() - amount;
        if (isIncrement && updatedBalance > MembershipAssignment.MAX_SESSIONS) {
            throw new CommandException(MESSAGE_SESSION_LIMIT);
        }
        if (!isIncrement && updatedBalance < MembershipAssignment.MIN_SESSIONS) {
            throw new CommandException(MESSAGE_INSUFFICIENT_SESSIONS);
        }

        model.setPerson(client,
                client.withMembershipAssignment(Optional.of(assignment.withRemainingSessions(updatedBalance))));
        String feedback = isIncrement
                ? String.format(MESSAGE_INCREASE_SUCCESS, client.getName(), amount, updatedBalance)
                : String.format(MESSAGE_DECREASE_SUCCESS, client.getName(), amount, updatedBalance);
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AdjustMembershipSessionsCommand otherAdjustMembershipSessionsCommand)) {
            return false;
        }

        return targetIndex.equals(otherAdjustMembershipSessionsCommand.targetIndex)
                && amount == otherAdjustMembershipSessionsCommand.amount
                && isIncrement == otherAdjustMembershipSessionsCommand.isIncrement;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("amount", amount)
                .add("isIncrement", isIncrement)
                .toString();
    }
}
