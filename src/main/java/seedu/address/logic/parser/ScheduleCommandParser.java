package seedu.address.logic.parser;

import seedu.address.logic.commands.ScheduleCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TrainingSession.Kind;

/** Parses a request to add a client's training session. */
public class ScheduleCommandParser implements Parser<ScheduleCommand> {
    @Override
    public ScheduleCommand parse(String args) throws ParseException {
        AppointmentParser.Appointment parsed = AppointmentParser.parse(
                args, ScheduleCommand.MESSAGE_USAGE, Kind.TRAINING);
        return new ScheduleCommand(parsed.index(), parsed.session());
    }
}
