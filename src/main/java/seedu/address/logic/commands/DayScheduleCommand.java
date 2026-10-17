package seedu.address.logic.commands;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.person.TrainingSession;

/** Lists all client appointments on a given day. */
public class DayScheduleCommand extends Command {
    public static final String COMMAND_WORD = "dayschedule";
    public static final String MESSAGE_USAGE = "dayschedule d/YYYY-MM-DD\nExample: dayschedule d/2099-03-15";
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate date;

    public DayScheduleCommand(LocalDate date) {
        this.date = java.util.Objects.requireNonNull(date);
    }

    @Override
    public CommandResult execute(Model model) {
        List<Appointment> appointments = model.getAddressBook().getPersonList().stream()
                .flatMap(person -> person.getTrainingSessions().stream()
                        .filter(session -> session.getDateTime().toLocalDate().equals(date))
                        .map(session -> new Appointment(person, session)))
                .sorted(Comparator.comparing((Appointment appointment) -> appointment.session().getDateTime())
                        .thenComparing(appointment -> appointment.person().getName().toString()))
                .toList();
        if (appointments.isEmpty()) {
            return new CommandResult("No client appointments on " + date + ".");
        }
        StringBuilder result = new StringBuilder("Client appointments on " + date + ":");
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appointment = appointments.get(i);
            String type = appointment.session().getKind() == TrainingSession.Kind.MEETING ? "meeting" : "training";
            result.append("\n").append(i + 1).append(". ")
                    .append(appointment.session().getDateTime().format(TIME_FORMAT))
                    .append(" ").append(appointment.person().getName())
                    .append(" (").append(type).append(") at ").append(appointment.session().getLocation());
        }
        return new CommandResult(result.toString());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DayScheduleCommand command && date.equals(command.date);
    }

    private record Appointment(Person person, TrainingSession session) {
    }
}
