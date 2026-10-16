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
 * Removes a client's membership assignment.
 * The whole assignment is discarded: the copied plan name, the remaining balance,
 * the plan reference, and any expiry date. All other client fields are preserved.
 */
public class RemoveMembershipPlanCommand extends Command implements MembershipCommand {

    public static final String COMMAND_WORD = "rmem";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Removes a client's membership plan.\n"
            + "Parameters: PERSON_INDEX\n"
            + "Example: " + COMMAND_WORD + " 1";

    public static final String MESSAGE_SUCCESS = "Removed %1$s plan from %2$s; %3$d remaining %4$s lost.";
    public static final String MESSAGE_NO_ASSIGNMENT =
            "This client has no membership plan to remove or adjust.";

    private final Index targetIndex;

    /**
     * Creates a RemoveMembershipPlanCommand to clear the assignment of the client
     * at {@code targetIndex}.
     */
    public RemoveMembershipPlanCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
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

        model.setPerson(client, client.withMembershipAssignment(Optional.empty()));
        String sessionWord = assignment.getRemainingSessions() == 1 ? "session" : "sessions";
        return new CommandResult(String.format(MESSAGE_SUCCESS, assignment.getPlanName(),
                client.getName(), assignment.getRemainingSessions(), sessionWord));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof RemoveMembershipPlanCommand otherRemoveMembershipPlanCommand)) {
            return false;
        }

        return targetIndex.equals(otherRemoveMembershipPlanCommand.targetIndex);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .toString();
    }
}
