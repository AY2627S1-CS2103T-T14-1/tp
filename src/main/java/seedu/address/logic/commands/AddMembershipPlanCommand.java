package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_PRICE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_SESSIONS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.membership.MembershipPlan;

/**
 * Adds a membership plan to the address book.
 */
public class AddMembershipPlanCommand extends Command implements MembershipCommand {

    public static final String COMMAND_WORD = "addmem";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a membership plan to the address book. "
            + "Parameters: "
            + PREFIX_MEMBERSHIP_NAME + "NAME "
            + PREFIX_MEMBERSHIP_PRICE + "PRICE "
            + PREFIX_MEMBERSHIP_SESSIONS + "NUMBER_OF_SESSIONS\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_MEMBERSHIP_NAME + "Gold "
            + PREFIX_MEMBERSHIP_PRICE + "150 "
            + PREFIX_MEMBERSHIP_SESSIONS + "30";

    public static final String MESSAGE_SUCCESS = "Added membership plan: %1$s; Price: %2$s; Sessions: %3$d.";
    public static final String MESSAGE_DUPLICATE_PLAN = "A membership plan named %1$s already exists.";

    private final MembershipPlan toAdd;

    /**
     * Creates an AddMembershipPlanCommand to add the specified {@code MembershipPlan}
     */
    public AddMembershipPlanCommand(MembershipPlan plan) {
        requireNonNull(plan);
        toAdd = plan;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasMembershipPlan(toAdd)) {
            String existingName = model.findMembershipPlan(toAdd.getName())
                    .map(plan -> plan.getName().value).orElse(toAdd.getName().value);
            throw new CommandException(String.format(MESSAGE_DUPLICATE_PLAN, existingName));
        }

        model.addMembershipPlan(toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd.getName().value,
                toAdd.getPrice().toPlainString(), toAdd.getStartingSessions()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddMembershipPlanCommand otherAddMembershipPlanCommand)) {
            return false;
        }

        return toAdd.equals(otherAddMembershipPlanCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
