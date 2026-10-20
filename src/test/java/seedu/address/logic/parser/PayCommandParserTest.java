package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.PayCommand;
import seedu.address.model.person.PaymentRecord;

public class PayCommandParserTest {

    private final PayCommandParser parser = new PayCommandParser();
    private final String formatError = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PayCommand.MESSAGE_USAGE);

    @Test
    public void parse_allPrefixesAnyOrder_success() {
        PaymentRecord payment = payment("150", "2026-10-20", "October membership payment");
        PayCommand expected = new PayCommand(INDEX_FIRST_PERSON, payment);

        assertParseSuccess(parser, "1 a/150 d/2026-10-20 r/October membership payment", expected);
        assertParseSuccess(parser, "1 d/2026-10-20 a/150 r/October membership payment", expected);
        assertParseSuccess(parser, "1 r/October membership payment a/150 d/2026-10-20", expected);
    }

    @Test
    public void parse_optionalRemarksOmitted_success() {
        assertParseSuccess(parser, "1 a/150 d/2026-10-20",
                new PayCommand(INDEX_FIRST_PERSON, payment("150.00", "2026-10-20", null)));
    }

    @Test
    public void parse_equivalentAmountForms_normalizesToTwoDecimals() {
        assertParseSuccess(parser, "1 a/150 d/2026-10-20",
                new PayCommand(INDEX_FIRST_PERSON, payment("150.00", "2026-10-20", null)));
        assertParseSuccess(parser, "1 a/150.5 d/2026-10-20",
                new PayCommand(INDEX_FIRST_PERSON, payment("150.50", "2026-10-20", null)));
        assertParseSuccess(parser, "1 a/150.50 d/2026-10-20",
                new PayCommand(INDEX_FIRST_PERSON, payment("150.50", "2026-10-20", null)));
    }

    @Test
    public void parse_validDateBoundaries_success() {
        assertParseSuccess(parser, "1 a/1 d/0001-01-01",
                new PayCommand(INDEX_FIRST_PERSON, payment("1", "0001-01-01", null)));
        assertParseSuccess(parser, "1 a/1 d/9999-12-31",
                new PayCommand(INDEX_FIRST_PERSON, payment("1", "9999-12-31", null)));
    }

    @Test
    public void parse_singlePrefixPermutation_success() {
        assertParseSuccess(parser, "1 d/2026-10-20 a/75.50",
                new PayCommand(INDEX_FIRST_PERSON, payment("75.50", "2026-10-20", null)));
    }

    @Test
    public void parse_invalidAmount_failure() {
        String[] invalidAmounts = {"0", "-5", "1.234", "S$150", ".5", "150.", "1e3", "1,500"};
        for (String amount : invalidAmounts) {
            assertParseFailure(parser, "1 a/" + amount + " d/2026-10-20",
                    PaymentRecord.MESSAGE_AMOUNT_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidDate_failure() {
        String[] invalidDates = {"2026-02-30", "2026-2-3", "0000-01-01", "10000-01-01", "2026/10/20"};
        for (String date : invalidDates) {
            assertParseFailure(parser, "1 a/1 d/" + date, PaymentRecord.MESSAGE_DATE_CONSTRAINTS);
        }
    }

    @Test
    public void parse_invalidRemarks_failure() {
        assertParseFailure(parser, "1 a/1 d/2026-10-20 r/" + "a".repeat(301),
                PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 r/a\tb", PaymentRecord.MESSAGE_REMARKS_CONSTRAINTS);
    }

    @Test
    public void parse_remarksAtLengthBoundary_success() {
        String remarks = "a".repeat(300);
        assertParseSuccess(parser, "1 a/1 d/2026-10-20 r/" + remarks,
                new PayCommand(INDEX_FIRST_PERSON, payment("1", "2026-10-20", remarks)));
    }

    @Test
    public void parse_remarksWithOrdinaryPunctuation_success() {
        String remarks = "paid, 50% (1/2) and/or";
        assertParseSuccess(parser, "1 a/1 d/2026-10-20 r/" + remarks,
                new PayCommand(INDEX_FIRST_PERSON, payment("1", "2026-10-20", remarks)));
    }

    @Test
    public void parse_remarksWithMidTokenSlash_success() {
        assertParseSuccess(parser, "1 a/1 d/2026-10-20 r/foo/a/bar",
                new PayCommand(INDEX_FIRST_PERSON, payment("1", "2026-10-20", "foo/a/bar")));
    }

    @Test
    public void parse_missingRequiredPrefixes_failure() {
        assertParseFailure(parser, "1 a/150", formatError);
        assertParseFailure(parser, "1 d/2026-10-20", formatError);
        assertParseFailure(parser, "1 r/notes", formatError);
    }

    @Test
    public void parse_emptyPrefixValues_failure() {
        assertParseFailure(parser, "1 a/ d/2026-10-20", formatError);
        assertParseFailure(parser, "1 a/1 d/", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 r/", formatError);
    }

    @Test
    public void parse_repeatedPrefixes_failure() {
        assertParseFailure(parser, "1 a/1 a/2 d/2026-10-20", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 d/2026-10-21", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 r/one r/two", formatError);
    }

    @Test
    public void parse_unknownOrUppercasePrefixes_failure() {
        assertParseFailure(parser, "1 x/1 a/1 d/2026-10-20", formatError);
        assertParseFailure(parser, "1 A/1 d/2026-10-20", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 R/notes", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 r/note x/2", formatError);
    }

    @Test
    public void parse_extraUnprefixedText_failure() {
        assertParseFailure(parser, "1 extra a/1 d/2026-10-20", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 extra", formatError);
        assertParseFailure(parser, "1 a/1 2 d/2026-10-20", formatError);
    }

    @Test
    public void parse_invalidIndex_failure() {
        assertParseFailure(parser, "0 a/1 d/2026-10-20", formatError);
        assertParseFailure(parser, "-1 a/1 d/2026-10-20", formatError);
        assertParseFailure(parser, "one a/1 d/2026-10-20", formatError);
        assertParseFailure(parser, "a/1 d/2026-10-20", formatError);
    }

    @Test
    public void parse_emptyOrBlankArguments_failure() {
        assertParseFailure(parser, "", formatError);
        assertParseFailure(parser, "   ", formatError);
        assertParseFailure(parser, "\t", formatError);
    }

    @Test
    public void parse_indexOnly_failure() {
        assertParseFailure(parser, "1", formatError);
        assertParseFailure(parser, "  1  ", formatError);
    }

    @Test
    public void parse_missingIndexArgumentSeparator_failure() {
        assertParseFailure(parser, "1a/150 d/2026-10-20", formatError);
        assertParseFailure(parser, "1d/2026-10-20 a/150", formatError);
    }

    @Test
    public void parse_repeatedInterPrefixWhitespace_success() {
        assertParseSuccess(parser, "1   a/150    d/2026-10-20     r/October membership payment",
                new PayCommand(INDEX_FIRST_PERSON,
                        payment("150", "2026-10-20", "October membership payment")));
        assertParseSuccess(parser, "1\ta/150\t\td/2026-10-20",
                new PayCommand(INDEX_FIRST_PERSON, payment("150", "2026-10-20", null)));
    }

    @Test
    public void parse_lineBreaks_failure() {
        assertParseFailure(parser, "1 a/1 d/2026-10-20\n", formatError);
        assertParseFailure(parser, "1 a/1 d/2026-10-20 r/line1\nline2", formatError);
        assertParseFailure(parser, "1 a/1\r d/2026-10-20", formatError);
    }

    private static PaymentRecord payment(String amount, String date, String remarks) {
        return PaymentRecord.fromText(amount, date,
                remarks == null ? Optional.empty() : Optional.of(remarks));
    }
}
