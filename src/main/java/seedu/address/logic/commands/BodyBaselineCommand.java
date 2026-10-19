package seedu.address.logic.commands;

import java.time.Clock;
import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyMeasurements;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.Person;

/** Creates or corrects a client's baseline while retaining every progress entry. */
public final class BodyBaselineCommand extends BodyMutationCommand {
    public static final String COMMAND_WORD = "bodybase";
    public static final String MESSAGE_USAGE = "bodybase INDEX d/DATE w/WEIGHT_KG h/HEIGHT_CM";
    private final BodyBaseline baseline;

    /** Creates a validated BodyBaselineCommand instance. */
    public BodyBaselineCommand(Index index, BodyBaseline baseline, Clock clock) {
        super(index, clock);
        this.baseline = Objects.requireNonNull(baseline);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Person person = resolveClient(model);
        try {
            BodyMeasurements.requireNotFuture(baseline.date(), clock);
            BodyProgress current = person.getBodyProgress();
            if (current.getBaseline().filter(baseline::equals).isPresent()) {
                throw new CommandException("This is already the current body baseline for " + person.getName() + ".");
            }
            Person updated = person.withBodyProgress(current.withBaseline(baseline));
            model.setPerson(person, updated);
            String action = current.getBaseline().isEmpty() ? "Created" : "Replaced";
            return result(action + " body baseline for " + person.getName() + ": " + baseline.date()
                    + "; Weight: " + baseline.weightKg().toPlainString() + " kg; Height: "
                    + baseline.heightCm().toPlainString() + " cm.", updated, BodyProgressViewRequest.Section.HISTORY);
        } catch (IllegalArgumentException exception) {
            throw new CommandException(exception.getMessage(), exception);
        }
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof BodyBaselineCommand command
                && index.equals(command.index) && baseline.equals(command.baseline) && clock.equals(command.clock);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index.getZeroBased(), baseline, clock);
    }
}
