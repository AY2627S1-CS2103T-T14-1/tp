package seedu.address.logic.commands;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.WorkoutSession;

/** Lists clients without a recorded workout in the last number of calendar days. */
public class InactiveCommand extends Command {
    public static final String COMMAND_WORD = "inactive";
    public static final String MESSAGE_USAGE = "inactive DAYS\nExample: inactive 14";

    private final int days;

    /** Constructs a query for clients inactive for the specified number of calendar days. */
    public InactiveCommand(int days) {
        if (days < 1) {
            throw new IllegalArgumentException("Days must be positive");
        }
        this.days = days;
    }

    @Override
    public CommandResult execute(Model model) {
        LocalDate cutoff = LocalDate.now().minusDays(days - 1L);
        List<Person> inactive = model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getWorkoutSessions().stream()
                        .noneMatch(session -> !session.getDate().isBefore(cutoff)))
                .sorted(Comparator.comparing(person -> person.getName().toString()))
                .toList();
        if (inactive.isEmpty()) {
            return new CommandResult("All clients have a recorded workout in the last " + days + " days.");
        }
        StringBuilder result = new StringBuilder("Clients without a recorded workout in the last " + days + " days:");
        for (int i = 0; i < inactive.size(); i++) {
            Person person = inactive.get(i);
            Optional<LocalDate> latest = person.getWorkoutSessions().stream()
                    .map(WorkoutSession::getDate).max(LocalDate::compareTo);
            result.append("\n").append(i + 1).append(". ").append(person.getName())
                    .append(" — last workout: ").append(latest.map(LocalDate::toString).orElse("never recorded"));
        }
        return new CommandResult(result.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof InactiveCommand command && days == command.days;
    }
}
