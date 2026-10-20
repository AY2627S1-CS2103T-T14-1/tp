package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.PaymentRecord;
import seedu.address.model.person.Person;

/**
 * Records a payment for the client at the displayed index.
 */
public class PayCommand extends Command {

    public static final String COMMAND_WORD = "pay";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Records a payment for the client identified by the index number used in the displayed client list.\n"
            + "Parameters: INDEX (must be a positive integer) a/AMOUNT d/YYYY-MM-DD [r/REMARKS]\n"
            + "Example: " + COMMAND_WORD + " 1 a/150 d/2026-10-20";

    public static final String MESSAGE_SUCCESS = "Recorded payment for %1$s: %2$s on %3$s.";
    public static final String MESSAGE_SAVE_FAILURE =
            "Payment could not be saved. No payment was recorded.";

    private final Index targetIndex;
    private final PaymentRecord paymentRecord;

    /**
     * Creates a command that records {@code paymentRecord} for the client at {@code targetIndex}.
     */
    public PayCommand(Index targetIndex, PaymentRecord paymentRecord) {
        requireAllNonNull(targetIndex, paymentRecord);
        this.targetIndex = targetIndex;
        this.paymentRecord = paymentRecord;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person target = lastShownList.get(targetIndex.getZeroBased());
        Person updated = target.withAddedPayment(paymentRecord);
        model.setPerson(target, updated);

        return new CommandResult(String.format(MESSAGE_SUCCESS, updated.getName(),
                paymentRecord.getFormattedAmount(), paymentRecord.getDate()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PayCommand otherPayCommand)) {
            return false;
        }

        return targetIndex.equals(otherPayCommand.targetIndex)
                && paymentRecord.equals(otherPayCommand.paymentRecord);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("paymentRecord", paymentRecord)
                .toString();
    }
}
