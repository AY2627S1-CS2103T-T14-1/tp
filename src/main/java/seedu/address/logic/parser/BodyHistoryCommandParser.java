package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;

import seedu.address.logic.commands.BodyHistoryCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code BodyHistoryCommand.COMMAND_WORD} using the shared strict body argument rules. */
public final class BodyHistoryCommandParser implements Parser<BodyHistoryCommand> {
    private final Clock clock;

    /** Uses the local clock in the running application. */
    public BodyHistoryCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an injectable clock for deterministic date checks. */
    public BodyHistoryCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public BodyHistoryCommand parse(String args) throws ParseException {
        BodyParserUtil.Arguments parsed = BodyParserUtil.parse(args, Set.of(), Set.of(),
                BodyHistoryCommand.MESSAGE_USAGE);
        try {
            return new BodyHistoryCommand(parsed.index(), clock);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
