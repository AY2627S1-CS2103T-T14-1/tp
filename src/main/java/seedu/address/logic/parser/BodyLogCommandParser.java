package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.BodyLogCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.BodyMeasurements;
import seedu.address.model.person.BodyProgressEntry;

/** Parses {@code BodyLogCommand.COMMAND_WORD} using the shared strict body argument rules. */
public final class BodyLogCommandParser implements Parser<BodyLogCommand> {
    private final Clock clock;

    /** Uses the local clock in the running application. */
    public BodyLogCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an injectable clock for deterministic date checks. */
    public BodyLogCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public BodyLogCommand parse(String args) throws ParseException {
        BodyParserUtil.Arguments parsed = BodyParserUtil.parse(args, Set.of("d", "w"), Set.of("n"),
                BodyLogCommand.MESSAGE_USAGE);
        try {
            return new BodyLogCommand(parsed.index(), new BodyProgressEntry(
                    BodyParserUtil.date(parsed.value("d"), clock),
                    BodyMeasurements.parseDecimal(parsed.value("w"), false),
                    Optional.ofNullable(parsed.value("n"))), clock);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
