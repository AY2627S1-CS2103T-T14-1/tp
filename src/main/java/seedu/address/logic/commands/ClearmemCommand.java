package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Clears the entire membership plan catalog.
 * Rejected while any plan is referenced by a client assignment, so it can never
 * leave an assignment pointing at a deleted plan. The trainer must remove those
 * assignments with {@code rmem} first.
 */
public class ClearmemCommand extends Command implements MembershipCommand {

    public static final String COMMAND_WORD = "clearmem";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Clears all membership plans. "
            + "Parameters: (none)\n"
            + "Example: " + COMMAND_WORD;

    public static final String MESSAGE_SUCCESS = "Cleared all membership plans.";
    public static final String MESSAGE_PLANS_IN_USE = "Membership plans are in use and cannot be cleared. "
            + "Remove all client assignments with rmem first.";

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        if (model.isAnyMembershipPlanInUse()) {
            throw new CommandException(MESSAGE_PLANS_IN_USE);
        }
        model.clearMembershipPlans();
        return new CommandResult(MESSAGE_SUCCESS);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        return other instanceof ClearmemCommand;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).toString();
    }
}
