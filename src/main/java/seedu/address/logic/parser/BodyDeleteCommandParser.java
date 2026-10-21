package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;

import seedu.address.logic.commands.BodyDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code BodyDeleteCommand.COMMAND_WORD} using the shared strict body argument rules. */
public final class BodyDeleteCommandParser implements Parser<BodyDeleteCommand> {
    private final Clock clock;

    /** Uses the local clock in the running application. */
    public BodyDeleteCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an injectable clock for deterministic date checks. */
    public BodyDeleteCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public BodyDeleteCommand parse(String args) throws ParseException {
        BodyParserUtil.Arguments parsed = BodyParserUtil.parse(args, Set.of("d"), Set.of(),
                BodyDeleteCommand.MESSAGE_USAGE);
        try {
            return new BodyDeleteCommand(parsed.index(), BodyParserUtil.date(parsed.value("d"), clock), clock);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
