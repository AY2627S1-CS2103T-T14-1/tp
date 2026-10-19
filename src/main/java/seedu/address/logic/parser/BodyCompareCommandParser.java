package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;

import seedu.address.logic.commands.BodyCompareCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code BodyCompareCommand.COMMAND_WORD} using the shared strict body argument rules. */
public final class BodyCompareCommandParser implements Parser<BodyCompareCommand> {
    private final Clock clock;

    /** Uses the local clock in the running application. */
    public BodyCompareCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an injectable clock for deterministic date checks. */
    public BodyCompareCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public BodyCompareCommand parse(String args) throws ParseException {
        BodyParserUtil.Arguments parsed = BodyParserUtil.parse(args, Set.of(), Set.of(),
                BodyCompareCommand.MESSAGE_USAGE);
        try {
            return new BodyCompareCommand(parsed.index(), clock);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
