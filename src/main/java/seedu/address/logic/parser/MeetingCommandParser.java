package seedu.address.logic.parser;

import seedu.address.logic.commands.MeetingCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.TrainingSession.Kind;

/** Parses a request to record a client meeting. */
public class MeetingCommandParser implements Parser<MeetingCommand> {
    @Override
    public MeetingCommand parse(String args) throws ParseException {
        AppointmentParser.Appointment parsed = AppointmentParser.parse(
                args, MeetingCommand.MESSAGE_USAGE, Kind.MEETING);
        return new MeetingCommand(parsed.index(), parsed.session());
    }
}
