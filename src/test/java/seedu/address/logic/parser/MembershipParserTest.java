package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddMembershipPlanCommand;
import seedu.address.logic.commands.AdjustMembershipSessionsCommand;
import seedu.address.logic.commands.AssignMembershipPlanCommand;
import seedu.address.logic.commands.ClearmemCommand;
import seedu.address.logic.commands.DeleteMembershipPlanCommand;
import seedu.address.logic.commands.RemoveMembershipPlanCommand;
import seedu.address.logic.commands.ViewMembershipPlansCommand;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;

public class MembershipParserTest {

    private static final String ADD_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddMembershipPlanCommand.MESSAGE_USAGE);
    private static final String ASSIGN_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, AssignMembershipPlanCommand.MESSAGE_USAGE);
    private static final String DELETE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, DeleteMembershipPlanCommand.MESSAGE_USAGE);
    private static final String REMOVE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, RemoveMembershipPlanCommand.MESSAGE_USAGE);
    private static final String INCREASE_INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            AdjustMembershipSessionsCommand.MESSAGE_USAGE_INCREASE);
    private static final String DECREASE_INVALID_FORMAT = String.format(MESSAGE_INVALID_COMMAND_FORMAT,
            AdjustMembershipSessionsCommand.MESSAGE_USAGE_DECREASE);
    private static final String VIEW_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, ViewMembershipPlansCommand.MESSAGE_USAGE);

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-15T00:00:00Z"), ZoneId.of("UTC"));

    private static AddMembershipPlanCommand addCommand(String name, String price, int sessions) {
        return new AddMembershipPlanCommand(new MembershipPlan(
                new MembershipName(name), new BigDecimal(price), sessions));
    }

    @Test
    public void parse_addAllFieldsPresent_success() {
        AddMembershipPlanCommandParser parser = new AddMembershipPlanCommandParser();
        assertParseSuccess(parser, " n/Gold p/150 s/30", addCommand("Gold", "150", 30));
        assertParseSuccess(parser, " s/30 p/150 n/Gold", addCommand("Gold", "150", 30));
        assertParseSuccess(parser, "  n/Gold   p/150   s/30  ", addCommand("Gold", "150", 30));
    }

    @Test
    public void parse_addPrefixLetters_success() {
        AddMembershipPlanCommandParser parser = new AddMembershipPlanCommandParser();
        assertParseSuccess(parser, " N/Gold P/150 S/30", addCommand("Gold", "150", 30));
        assertParseSuccess(parser, " n/Gold P/150.50 s/12", addCommand("Gold", "150.50", 12));
    }

    @Test
    public void parse_addMissingPrefix_failure() {
        AddMembershipPlanCommandParser parser = new AddMembershipPlanCommandParser();
        assertParseFailure(parser, " n/Gold p/150", ADD_INVALID_FORMAT);
        assertParseFailure(parser, " n/Gold s/30", ADD_INVALID_FORMAT);
        assertParseFailure(parser, " p/150 s/30", ADD_INVALID_FORMAT);
        assertParseFailure(parser, "", ADD_INVALID_FORMAT);
        assertParseFailure(parser, " n/Gold n/Silver p/150 s/30",
                Messages.getErrorMessageForDuplicatePrefixes(
                        new Prefix("n/")));
        assertParseFailure(parser, " n/Gold p/150 p/80 s/30",
                Messages.getErrorMessageForDuplicatePrefixes(
                        new Prefix("p/")));
        assertParseFailure(parser, " n/Gold p/150 s/30 s/12",
                Messages.getErrorMessageForDuplicatePrefixes(
                        new Prefix("s/")));
    }

    @Test
    public void parse_addUnknownPrefix_failure() {
        AddMembershipPlanCommandParser parser = new AddMembershipPlanCommandParser();
        assertParseFailure(parser, " n/Gold p/150 s/30 x/1", ADD_INVALID_FORMAT);
        assertParseFailure(parser, " n/Gold p/150 s/30 e/2026-12-30", ADD_INVALID_FORMAT);
        assertParseFailure(parser, " extra n/Gold p/150 s/30", ADD_INVALID_FORMAT);
        assertParseFailure(parser, " n/Gold p/150 s/30 extra", ADD_INVALID_FORMAT);
    }

    @Test
    public void parse_addInvalidValues_failure() {
        AddMembershipPlanCommandParser parser = new AddMembershipPlanCommandParser();
        assertParseFailure(parser, " n/ p/150 s/30", MembershipName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold@ p/150 s/30", MembershipName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/0 s/30", MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/-5 s/30", MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/10.123 s/30", MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/abc s/30", MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/150 s/0", MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/150 s/1001", MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/150 s/abc", MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold p/15 0 s/30", ADD_INVALID_FORMAT);
    }

    @Test
    public void parse_assignBasicMultiWord_success() {
        AssignMembershipPlanCommandParser parser = new AssignMembershipPlanCommandParser(FIXED_CLOCK);
        assertParseSuccess(parser, " Gold 1",
                new AssignMembershipPlanCommand(new MembershipName("Gold"),
                        Index.fromOneBased(1), Optional.empty()));
        assertParseSuccess(parser, " Gold Plus 1",
                new AssignMembershipPlanCommand(new MembershipName("Gold Plus"),
                        Index.fromOneBased(1), Optional.empty()));
        assertParseSuccess(parser, " Gold Plus 2 e/2026-12-30",
                new AssignMembershipPlanCommand(new MembershipName("Gold Plus"),
                        Index.fromOneBased(2), Optional.of(LocalDate.of(2026, 12, 30))));
        assertParseSuccess(parser, " Gold 1 E/2026-12-30",
                new AssignMembershipPlanCommand(new MembershipName("Gold"),
                        Index.fromOneBased(1), Optional.of(LocalDate.of(2026, 12, 30))));
    }

    @Test
    public void parse_assignLastTokenIndex_success() {
        AssignMembershipPlanCommandParser parser = new AssignMembershipPlanCommandParser(FIXED_CLOCK);
        assertParseSuccess(parser, " Gold Plus Extra 3",
                new AssignMembershipPlanCommand(new MembershipName("Gold Plus Extra"),
                        Index.fromOneBased(3), Optional.empty()));
    }

    @Test
    public void parse_assignMissingText_failure() {
        AssignMembershipPlanCommandParser parser = new AssignMembershipPlanCommandParser(FIXED_CLOCK);
        assertParseFailure(parser, "", ASSIGN_INVALID_FORMAT);
        assertParseFailure(parser, " Gold", ASSIGN_INVALID_FORMAT);
        assertParseFailure(parser, " 1", ASSIGN_INVALID_FORMAT);
        assertParseFailure(parser, " Gold 1 e/2026-12-30 extra", ASSIGN_INVALID_FORMAT);
        assertParseFailure(parser, " Gold 1 e/2026-12-30 e/2027-01-01",
                Messages.getErrorMessageForDuplicatePrefixes(
                        new Prefix("e/")));
        assertParseFailure(parser, " Gold 1 x/unexpected", ASSIGN_INVALID_FORMAT);
        assertParseFailure(parser, " Gold 0", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_assignInvalidName_failure() {
        AssignMembershipPlanCommandParser parser = new AssignMembershipPlanCommandParser(FIXED_CLOCK);
        assertParseFailure(parser, " Gold@ 1", MembershipName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_assignExpiryBoundaries() {
        AssignMembershipPlanCommandParser parser = new AssignMembershipPlanCommandParser(FIXED_CLOCK);
        assertParseFailure(parser, " Gold 1 e/2026-06-15",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        assertParseFailure(parser, " Gold 1 e/2026-06-14",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        assertParseSuccess(parser, " Gold 1 e/2026-06-16",
                new AssignMembershipPlanCommand(new MembershipName("Gold"),
                        Index.fromOneBased(1), Optional.of(LocalDate.of(2026, 6, 16))));
        assertParseFailure(parser, " Gold 1 e/15-06-2026",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        assertParseFailure(parser, " Gold 1 e/2026-02-30",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        assertParseFailure(parser, " Gold 1 e/2026-13-01",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        assertParseFailure(parser, " Gold 1 e/not-a-date",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
    }

    @Test
    public void parse_assignLeapDate() {
        AssignMembershipPlanCommandParser leapParser = new AssignMembershipPlanCommandParser(Clock.fixed(
                Instant.parse("2024-02-28T00:00:00Z"), ZoneId.of("UTC")));
        assertParseSuccess(leapParser, " Gold 1 e/2024-02-29",
                new AssignMembershipPlanCommand(new MembershipName("Gold"),
                        Index.fromOneBased(1), Optional.of(LocalDate.of(2024, 2, 29))));
        assertParseFailure(leapParser, " Gold 1 e/2023-02-29",
                MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
    }

    @Test
    public void parse_adjustValidInvalid() {
        AdjustMembershipSessionsCommandParser increase = new AdjustMembershipSessionsCommandParser(true);
        AdjustMembershipSessionsCommandParser decrease = new AdjustMembershipSessionsCommandParser(false);
        assertParseSuccess(increase, " 1 3",
                new AdjustMembershipSessionsCommand(Index.fromOneBased(1), 3, true));
        assertParseSuccess(decrease, " 2 2",
                new AdjustMembershipSessionsCommand(Index.fromOneBased(2), 2, false));
        assertParseFailure(increase, " 1 0", MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        assertParseFailure(increase, " 1 1001", MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        assertParseFailure(decrease, " 1 abc", MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        assertParseFailure(increase, " 1", INCREASE_INVALID_FORMAT);
        assertParseFailure(increase, " 1 2 3", INCREASE_INVALID_FORMAT);
        assertParseFailure(increase, " 1 2 extra", INCREASE_INVALID_FORMAT);
        assertParseFailure(increase, " 1 n/2", INCREASE_INVALID_FORMAT);
        assertParseFailure(decrease, " 0 2", ParserUtil.MESSAGE_INVALID_INDEX);
        assertParseFailure(increase, " x 2", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_removeStrictForm() {
        RemoveMembershipPlanCommandParser parser = new RemoveMembershipPlanCommandParser();
        assertParseSuccess(parser, " 1", new RemoveMembershipPlanCommand(Index.fromOneBased(1)));
        assertParseFailure(parser, "", REMOVE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 2", REMOVE_INVALID_FORMAT);
        assertParseFailure(parser, " n/1", REMOVE_INVALID_FORMAT);
        assertParseFailure(parser, " 1 extra", REMOVE_INVALID_FORMAT);
        assertParseFailure(parser, " 0", ParserUtil.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_deleteStrictForm() {
        DeleteMembershipPlanCommandParser parser = new DeleteMembershipPlanCommandParser();
        assertParseSuccess(parser, " n/Gold", new DeleteMembershipPlanCommand(new MembershipName("Gold")));
        assertParseSuccess(parser, " N/Gold", new DeleteMembershipPlanCommand(new MembershipName("Gold")));
        assertParseFailure(parser, "", DELETE_INVALID_FORMAT);
        assertParseFailure(parser, " n/Gold n/Silver",
                Messages.getErrorMessageForDuplicatePrefixes(
                        new Prefix("n/")));
        assertParseFailure(parser, " n/Gold p/1", DELETE_INVALID_FORMAT);
        assertParseFailure(parser, " Gold", DELETE_INVALID_FORMAT);
        assertParseFailure(parser, " n/", MembershipName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, " n/Gold@ ", MembershipName.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_viewAliases() throws Exception {
        ViewMembershipPlansCommandParser parser = new ViewMembershipPlansCommandParser();
        assertParseSuccess(parser, " mem", new ViewMembershipPlansCommand());
        assertParseSuccess(parser, " membership", new ViewMembershipPlansCommand());
        assertParseSuccess(parser, "  mem  ", new ViewMembershipPlansCommand());
        assertParseFailure(parser, "", VIEW_INVALID_FORMAT);
        assertParseFailure(parser, " Mem", VIEW_INVALID_FORMAT);
        assertParseFailure(parser, " MEM", VIEW_INVALID_FORMAT);
        assertParseFailure(parser, " plans", VIEW_INVALID_FORMAT);
        assertParseFailure(parser, " mem extra", VIEW_INVALID_FORMAT);
    }

    @Test
    public void parse_parserDispatch() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        org.junit.jupiter.api.Assertions.assertEquals(addCommand("Gold", "150", 30),
                parser.parseCommand("addmem n/Gold p/150 s/30"));
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("delmem n/Gold") instanceof DeleteMembershipPlanCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("rmem 1") instanceof RemoveMembershipPlanCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("imem 1 3") instanceof AdjustMembershipSessionsCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("dmem 1 3") instanceof AdjustMembershipSessionsCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("view mem") instanceof ViewMembershipPlansCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("view membership") instanceof ViewMembershipPlansCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("clearmem") instanceof ClearmemCommand);
    }

    @Test
    public void parse_clearmemIgnoresExtraneousArguments() throws Exception {
        AddressBookParser parser = new AddressBookParser();
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("clearmem") instanceof ClearmemCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("clearmem   ") instanceof ClearmemCommand);
        org.junit.jupiter.api.Assertions.assertTrue(
                parser.parseCommand("clearmem extra text") instanceof ClearmemCommand);
    }

    @Test
    public void parse_parserCaseSensitive() {
        AddressBookParser parser = new AddressBookParser();
        Class<seedu.address.logic.parser.exceptions.ParseException> parseException =
                seedu.address.logic.parser.exceptions.ParseException.class;
        org.junit.jupiter.api.Assertions.assertThrows(parseException, () -> parser.parseCommand(
                "Addmem n/Gold p/150 s/30"));
        org.junit.jupiter.api.Assertions.assertThrows(parseException, () -> parser.parseCommand(
                "ADDMEM n/Gold p/150 s/30"));
        org.junit.jupiter.api.Assertions.assertThrows(parseException, () -> parser.parseCommand(
                "view Mem"));
    }
}
