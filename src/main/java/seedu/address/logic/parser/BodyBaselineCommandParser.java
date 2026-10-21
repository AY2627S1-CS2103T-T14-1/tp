package seedu.address.logic.parser;

import java.time.Clock;
import java.util.Objects;
import java.util.Set;

import seedu.address.logic.commands.BodyBaselineCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyMeasurements;

/** Parses {@code BodyBaselineCommand.COMMAND_WORD} using the shared strict body argument rules. */
public final class BodyBaselineCommandParser implements Parser<BodyBaselineCommand> {
    private final Clock clock;

    /** Uses the local clock in the running application. */
    public BodyBaselineCommandParser() {
        this(Clock.systemDefaultZone());
    }

    /** Uses an injectable clock for deterministic date checks. */
    public BodyBaselineCommandParser(Clock clock) {
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public BodyBaselineCommand parse(String args) throws ParseException {
        BodyParserUtil.Arguments parsed = BodyParserUtil.parse(args, Set.of("d", "w", "h"), Set.of(),
                BodyBaselineCommand.MESSAGE_USAGE);
        try {
            return new BodyBaselineCommand(parsed.index(), new BodyBaseline(
                    BodyParserUtil.date(parsed.value("d"), clock),
                    BodyMeasurements.parseDecimal(parsed.value("w"), false),
                    BodyMeasurements.parseDecimal(parsed.value("h"), true)), clock);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(exception.getMessage(), exception);
        }
    }
}
