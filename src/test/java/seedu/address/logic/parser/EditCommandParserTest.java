package seedu.address.logic.parser;

import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_ADDRESS_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_TAG_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.TAG_DESC_HUSBAND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_FRIEND;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.logic.parser.CliSyntax.PREFIX_GOAL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {

    private static final String TAG_EMPTY = " " + PREFIX_TAG;
    private static final String GOAL_DESC = " g/Build strength";
    private static final String GOAL_EMPTY = " " + PREFIX_GOAL;

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_documentedNameCharacters_success() {
        for (String name : new String[] {"Anne-Marie Tan", "D'Angelo Lim", "Dr. Ahmad Rahman", "\u674e\u5c0f\u9f99"}) {
            EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(name).build();
            assertParseSuccess(parser, "1 n/" + name, new EditCommand(INDEX_FIRST_PERSON, descriptor));
        }
    }

    @Test
    public void parse_missingParts_failure() {
        // no index specified
        assertParseFailure(parser, NAME_DESC_AMY, EditCommand.MESSAGE_INDEX_REQUIRED);

        // no field specified
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);

        // no index and no field specified
        assertParseFailure(parser, "", EditCommand.MESSAGE_INDEX_REQUIRED);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        // negative index
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, EditCommand.MESSAGE_INVALID_INDEX);

        // zero index
        assertParseFailure(parser, "0" + NAME_DESC_AMY, EditCommand.MESSAGE_INVALID_INDEX);

        // invalid arguments being parsed as preamble
        assertParseFailure(parser, "1 some random string", EditCommand.MESSAGE_INVALID_INDEX);

        // invalid prefix being parsed as preamble
        assertParseFailure(parser, "1 i/ string", EditCommand.MESSAGE_INVALID_INDEX);
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS); // invalid name
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS); // invalid phone
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS); // invalid email
        assertParseFailure(parser, "1" + INVALID_ADDRESS_DESC, Address.MESSAGE_CONSTRAINTS); // invalid address
        assertParseFailure(parser, "1" + INVALID_TAG_DESC, Tag.MESSAGE_CONSTRAINTS); // invalid tag
        assertParseFailure(parser, "1 g/!!!", Goal.MESSAGE_CONSTRAINTS); // invalid goal
        assertParseFailure(parser, "1 g/\fRun", Goal.MESSAGE_CONSTRAINTS); // leading control character
        assertParseFailure(parser, "1 g/\f", Goal.MESSAGE_CONSTRAINTS); // must not clear the goal
        assertParseFailure(parser, "1 n/", Name.MESSAGE_CONSTRAINTS); // empty name
        assertParseFailure(parser, "1 p/", Phone.MESSAGE_CONSTRAINTS); // empty phone
        assertParseFailure(parser, "1 p/1234567890123456", Phone.MESSAGE_CONSTRAINTS); // 16 digits
        assertParseFailure(parser, "1 e/", Email.MESSAGE_CONSTRAINTS); // empty email

        // invalid phone followed by valid email
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);

        // while parsing {@code PREFIX_TAG} alone will reset the tags of the {@code Person} being edited,
        // parsing it together with a valid tag results in error
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_DESC_HUSBAND + TAG_EMPTY, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_DESC_FRIEND + TAG_EMPTY + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + TAG_EMPTY + TAG_DESC_FRIEND + TAG_DESC_HUSBAND, Tag.MESSAGE_CONSTRAINTS);

        // multiple invalid values, but only the first invalid value is captured
        assertParseFailure(parser, "1" + INVALID_NAME_DESC + INVALID_EMAIL_DESC + VALID_ADDRESS_AMY + VALID_PHONE_AMY,
                Name.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + TAG_DESC_HUSBAND
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY + NAME_DESC_AMY + TAG_DESC_FRIEND;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withAddress(VALID_ADDRESS_AMY)
                .withTags(VALID_TAG_HUSBAND, VALID_TAG_FRIEND).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        // name
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // goal
        userInput = targetIndex.getOneBased() + GOAL_DESC;
        descriptor = new EditPersonDescriptorBuilder().withGoal("Build strength").build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // phone
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // email
        userInput = targetIndex.getOneBased() + EMAIL_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // address
        userInput = targetIndex.getOneBased() + ADDRESS_DESC_AMY;
        descriptor = new EditPersonDescriptorBuilder().withAddress(VALID_ADDRESS_AMY).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);

        // tags
        userInput = targetIndex.getOneBased() + TAG_DESC_FRIEND;
        descriptor = new EditPersonDescriptorBuilder().withTags(VALID_TAG_FRIEND).build();
        expectedCommand = new EditCommand(targetIndex, descriptor);
        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_moreThanMaxDistinctTags_failure() {
        String index = Integer.toString(INDEX_FIRST_PERSON.getOneBased());
        assertParseFailure(parser, index
                + " t/one t/two t/three t/four t/five t/six t/seven t/eight t/nine t/ten t/eleven",
                Tag.MESSAGE_TOO_MANY_TAGS);
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        // More extensive testing of duplicate parameter detections is done in
        // AddCommandParserTest#parse_repeatedNonTagValue_failure()

        // valid followed by invalid
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + PHONE_DESC_BOB;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PREFIX);

        // invalid followed by valid
        userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + INVALID_PHONE_DESC;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PREFIX);

        // multiple valid fields repeated
        userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY
                + TAG_DESC_FRIEND + PHONE_DESC_AMY + ADDRESS_DESC_AMY + EMAIL_DESC_AMY + TAG_DESC_FRIEND
                + PHONE_DESC_BOB + ADDRESS_DESC_BOB + EMAIL_DESC_BOB + TAG_DESC_HUSBAND;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PREFIX);

        // multiple invalid values
        userInput = targetIndex.getOneBased() + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC
                + INVALID_PHONE_DESC + INVALID_ADDRESS_DESC + INVALID_EMAIL_DESC;

        assertParseFailure(parser, userInput, EditCommand.MESSAGE_REPEATED_PREFIX);
    }

    @Test
    public void parse_resetTags_success() {
        Index targetIndex = INDEX_THIRD_PERSON;
        String userInput = targetIndex.getOneBased() + TAG_EMPTY;

        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        EditCommand expectedCommand = new EditCommand(targetIndex, descriptor);

        assertParseSuccess(parser, userInput, expectedCommand);
    }

    @Test
    public void parse_clearGoal_success() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.clearGoal();
        assertParseSuccess(parser, INDEX_THIRD_PERSON.getOneBased() + GOAL_EMPTY,
                new EditCommand(INDEX_THIRD_PERSON, descriptor));
    }

    @Test
    public void parse_whitespaceOnlyGoal_clearsGoal() {
        EditPersonDescriptor descriptor = new EditPersonDescriptor();
        descriptor.clearGoal();
        assertParseSuccess(parser, INDEX_THIRD_PERSON.getOneBased() + " g/   ",
                new EditCommand(INDEX_THIRD_PERSON, descriptor));
    }

    @Test
    public void parse_whitespaceOnlyTags_clearsTags() {
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withTags().build();
        assertParseSuccess(parser, INDEX_THIRD_PERSON.getOneBased() + " t/   ",
                new EditCommand(INDEX_THIRD_PERSON, descriptor));
    }

    @Test
    public void parse_repeatedGoal_failure() {
        assertParseFailure(parser, INDEX_FIRST_PERSON.getOneBased() + GOAL_DESC + " g/Run 5 km",
                EditCommand.MESSAGE_REPEATED_PREFIX);
    }

    @Test
    public void parse_repeatedPersonalPrefixes_failure() {
        String index = Integer.toString(INDEX_FIRST_PERSON.getOneBased());

        assertParseFailure(parser, index + NAME_DESC_AMY + " n/Amy Tan", EditCommand.MESSAGE_REPEATED_PREFIX);
        assertParseFailure(parser, index + EMAIL_DESC_AMY + EMAIL_DESC_BOB, EditCommand.MESSAGE_REPEATED_PREFIX);
        assertParseFailure(parser, index + ADDRESS_DESC_AMY + ADDRESS_DESC_BOB,
                EditCommand.MESSAGE_REPEATED_PREFIX);
    }

    @Test
    public void parse_mistypedPrefixAbsorbedIntoGoal_rejected() {
        // Reproduces issue #253: a mistyped "ad/" address token is swallowed by the goal value.
        String userInput = INDEX_FIRST_PERSON.getOneBased() + " g/Run 10 km ad/18 Clementi Road";

        String expectedMessage = String.format(EditCommand.MESSAGE_UNRECOGNIZED_PREFIX, "ad/")
                + " " + String.format(EditCommand.MESSAGE_DID_YOU_MEAN, "a/ (address)");
        assertParseFailure(parser, userInput, expectedMessage);
    }

    @Test
    public void parse_distantUnknownPrefixInsideGoal_notFlagged() {
        // A letter-prefixed token that is not a near miss of any recognized prefix is ordinary text.
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + " g/Build strength rr/5";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withGoal("Build strength rr/5").build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_slashInOrdinaryText_notFlagged() {
        // Ordinary text containing a slash must not be mistaken for a mistyped prefix.
        Index targetIndex = INDEX_FIRST_PERSON;

        String address = targetIndex.getOneBased() + " a/Blk 30 c/o John Tan";
        assertParseSuccess(parser, address, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withAddress("Blk 30 c/o John Tan").build()));

        String andOr = targetIndex.getOneBased() + " g/Run 5km and/or swim";
        assertParseSuccess(parser, andOr, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Run 5km and/or swim").build()));

        String unit = targetIndex.getOneBased() + " g/Cycle at 25 km/h";
        assertParseSuccess(parser, unit, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Cycle at 25 km/h").build()));

        String with = targetIndex.getOneBased() + " g/Lose 5kg w/ diet";
        assertParseSuccess(parser, with, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Lose 5kg w/ diet").build()));
    }

    @Test
    public void parse_twoLetterSlashTokenNearSingleLetterPrefix_accepted() {
        // A two-letter <letters>/<letters> token such as on/off, in/out, am/pm or up/down joins two
        // ordinary words with a slash. It looks like a near miss of a single-letter prefix but is
        // ordinary goal text, not a mistyped prefix, so it must be accepted. The genuine typo
        // "ad/" in "ad/18 Clementi Road" is still rejected (see parse_mistypedPrefixAbsorbedIntoGoal).
        Index targetIndex = INDEX_FIRST_PERSON;

        String onOff = targetIndex.getOneBased() + " g/Track on/off trends";
        assertParseSuccess(parser, onOff, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Track on/off trends").build()));

        String inOut = targetIndex.getOneBased() + " g/Track in/out values";
        assertParseSuccess(parser, inOut, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Track in/out values").build()));

        String ampm = targetIndex.getOneBased() + " g/Take pill am/pm schedule";
        assertParseSuccess(parser, ampm, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Take pill am/pm schedule").build()));

        String upDown = targetIndex.getOneBased() + " g/Track up/down trends";
        assertParseSuccess(parser, upDown, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Track up/down trends").build()));

        String punctuated = targetIndex.getOneBased() + " g/Track on/off, in/out.";
        assertParseSuccess(parser, punctuated, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Track on/off, in/out.").build()));

        String unit = targetIndex.getOneBased() + " g/Lose 0.5 kg/week";
        assertParseSuccess(parser, unit, new EditCommand(targetIndex,
                new EditPersonDescriptorBuilder().withGoal("Lose 0.5 kg/week").build()));
    }

    @Test
    public void parse_mistypedPrefixWithLowercaseValue_rejected() {
        // A mistyped prefix is still reported even when the value it attaches to begins with a
        // lowercase word. "ad/" in "ad/orchard Road" is a genuine typo of "a/" (address) and is
        // rejected because "orchard" exceeds the short-word exemption. A numeric value such as
        // "ad/18 Clementi Road" is also examined for a near miss.
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + " g/Run ad/orchard Road";

        String expectedMessage = String.format(EditCommand.MESSAGE_UNRECOGNIZED_PREFIX, "ad/")
                + " " + String.format(EditCommand.MESSAGE_DID_YOU_MEAN, "a/ (address)");
        assertParseFailure(parser, userInput, expectedMessage);
    }

    @Test
    public void parse_multiWordGoalWithoutPrefixShape_success() {
        // A normal multi-word goal must still parse successfully.
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + " g/Run 10 km three times a week";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withGoal("Run 10 km three times a week").build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_addressWithNumberSlashNotFlagged_success() {
        // An address value containing a digit/slash token is not treated as a parameter prefix.
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + " a/Blk 45/10 Geylor Road";
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder()
                .withAddress("Blk 45/10 Geylor Road").build();
        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }
}
