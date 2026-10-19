package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_EXERCISE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOG_TIME;

import java.util.Objects;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditLogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.WorkoutSession;

/** Parses a client index, displayed session index, and complete replacement session. */
public class EditLogCommandParser implements Parser<EditLogCommand> {

    @Override
    public EditLogCommand parse(String args) throws ParseException {
        Objects.requireNonNull(args);
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                " " + args, PREFIX_LOG_DATE, PREFIX_LOG_TIME, PREFIX_LOG_EXERCISE);
        String[] indexes = arguments.getPreamble().trim().split("\\s+");
        if (indexes.length != 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditLogCommand.MESSAGE_USAGE));
        }
        Index clientIndex = ParserUtil.parseIndex(indexes[0]);
        Index sessionIndex = ParserUtil.parseIndex(indexes[1]);
        WorkoutSession session = LogCommandParser.parseSession(arguments, EditLogCommand.MESSAGE_USAGE);
        return new EditLogCommand(clientIndex, sessionIndex, session);
    }
}
