package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MEMBERSHIP_EXPIRY;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;

/**
 * Assigns a membership plan to a client.
 * The client must not already have an assigned plan. The assignment copies the plan's
 * display name and starting session balance and records an optional expiry date.
 */
public class AssignMembershipPlanCommand extends Command {

    public static final String COMMAND_WORD = "amem";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Assigns a membership plan to a client. "
            + "Parameters: MEMBERSHIP_NAME PERSON_INDEX [" + PREFIX_MEMBERSHIP_EXPIRY + "YYYY-MM-DD]\n"
            + "Example: " + COMMAND_WORD + " Gold 1 " + PREFIX_MEMBERSHIP_EXPIRY + "2026-12-30";

    public static final String MESSAGE_SUCCESS = "Assigned %1$s to %2$s; Sessions remaining: %3$d.";
    public static final String MESSAGE_SUCCESS_WITH_EXPIRY =
            "Assigned %1$s to %2$s; Sessions remaining: %3$d; Expiry: %4$s.";
    public static final String MESSAGE_PLAN_NOT_FOUND = "No membership plan named %1$s exists.";
    public static final String MESSAGE_ALREADY_ASSIGNED = "%1$s already has an assigned membership plan.";

    private final MembershipName planName;
    private final Index targetIndex;
    private final Optional<LocalDate> expiryDate;

    /**
     * Creates an AssignMembershipPlanCommand to assign the named plan to the client
     * at {@code targetIndex} with the given optional expiry date.
     */
    public AssignMembershipPlanCommand(MembershipName planName, Index targetIndex,
            Optional<LocalDate> expiryDate) {
        requireNonNull(planName);
        requireNonNull(targetIndex);
        requireNonNull(expiryDate);
        this.planName = planName;
        this.targetIndex = targetIndex;
        this.expiryDate = expiryDate;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> displayedClients = model.getFilteredPersonList();
        if (targetIndex.getZeroBased() >= displayedClients.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        MembershipPlan plan = model.findMembershipPlan(planName)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_PLAN_NOT_FOUND, planName.value)));

        Person client = displayedClients.get(targetIndex.getZeroBased());
        if (client.getMembershipAssignment().isPresent()) {
            throw new CommandException(String.format(MESSAGE_ALREADY_ASSIGNED, client.getName()));
        }

        MembershipAssignment assignment = new MembershipAssignment(plan, expiryDate);
        model.setPerson(client, client.withMembershipAssignment(Optional.of(assignment)));

        String feedback = expiryDate
                .map(date -> String.format(MESSAGE_SUCCESS_WITH_EXPIRY,
                        plan.getName().value, client.getName(), assignment.getRemainingSessions(), date))
                .orElseGet(() -> String.format(MESSAGE_SUCCESS,
                        plan.getName().value, client.getName(), assignment.getRemainingSessions()));
        return new CommandResult(feedback);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AssignMembershipPlanCommand otherAssignMembershipPlanCommand)) {
            return false;
        }

        return planName.equals(otherAssignMembershipPlanCommand.planName)
                && targetIndex.equals(otherAssignMembershipPlanCommand.targetIndex)
                && expiryDate.equals(otherAssignMembershipPlanCommand.expiryDate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("planName", planName)
                .add("targetIndex", targetIndex)
                .add("expiryDate", expiryDate)
                .toString();
    }
}
