package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.parser.ParserUtil.MESSAGE_INVALID_INDEX;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

public class ParserUtilTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = "Rachel Walker";
    private static final String VALID_PHONE = "123456";
    private static final String VALID_ADDRESS = "123 Main Street #0505";
    private static final String VALID_EMAIL = "rachel@example.com";
    private static final String VALID_TAG_1 = "friend";
    private static final String VALID_TAG_2 = "neighbour";

    private static final String INVALID_EXERCISE_NAME = " ";
    private static final String VALID_EXERCISE_NAME = "Bench Press";

    private static final String VALID_EXERCISE_SETS = "3";
    private static final String VALID_EXERCISE_REPS = "10";
    private static final String VALID_EXERCISE_WEIGHT = "60";

    private static final String WHITESPACE = " \t\r\n";

    @Test
    public void parseIndex_invalidInput_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseIndex("10 a"));
    }

    @Test
    public void parseIndex_outOfRangeInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_INVALID_INDEX, ()
            -> ParserUtil.parseIndex(Long.toString(Integer.MAX_VALUE + 1)));
    }

    @Test
    public void parseIndex_validInput_success() throws Exception {
        // No whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("1"));

        // Leading and trailing whitespaces
        assertEquals(INDEX_FIRST_PERSON, ParserUtil.parseIndex("  1  "));
    }

    @Test
    public void parseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseName((String) null));
    }

    @Test
    public void parseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseName(INVALID_NAME));
    }

    @Test
    public void parseName_validValueWithoutWhitespace_returnsName() throws Exception {
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(VALID_NAME));
    }

    @Test
    public void parseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_NAME + WHITESPACE;
        Name expectedName = new Name(VALID_NAME);
        assertEquals(expectedName, ParserUtil.parseName(nameWithWhitespace));
    }

    @Test
    public void parsePhone_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parsePhone((String) null));
    }

    @Test
    public void parsePhone_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parsePhone(INVALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithoutWhitespace_returnsPhone() throws Exception {
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(VALID_PHONE));
    }

    @Test
    public void parsePhone_validValueWithWhitespace_returnsTrimmedPhone() throws Exception {
        String phoneWithWhitespace = WHITESPACE + VALID_PHONE + WHITESPACE;
        Phone expectedPhone = new Phone(VALID_PHONE);
        assertEquals(expectedPhone, ParserUtil.parsePhone(phoneWithWhitespace));
    }

    @Test
    public void parseAddress_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddress((String) null));
    }

    @Test
    public void parseAddress_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseAddress(INVALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithoutWhitespace_returnsAddress() throws Exception {
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(VALID_ADDRESS));
    }

    @Test
    public void parseAddress_validValueWithWhitespace_returnsTrimmedAddress() throws Exception {
        String addressWithWhitespace = WHITESPACE + VALID_ADDRESS + WHITESPACE;
        Address expectedAddress = new Address(VALID_ADDRESS);
        assertEquals(expectedAddress, ParserUtil.parseAddress(addressWithWhitespace));
    }

    @Test
    public void parseAddressForAdd_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseAddressForAdd(null));
    }

    @Test
    public void parseAddressForAdd_blankAddress_retainsExistingValidation() {
        for (String address : List.of("", " ", WHITESPACE)) {
            assertThrows(ParseException.class, Address.MESSAGE_CONSTRAINTS, () ->
                    ParserUtil.parseAddressForAdd(address));
        }
    }

    @Test
    public void parseAddressForAdd_lengthBoundaries_returnsAddressOrRejectsOverflow() throws Exception {
        for (int length : List.of(1, 199, 200)) {
            String address = "A".repeat(length);
            assertEquals(new Address(address), ParserUtil.parseAddressForAdd(address));
        }
        assertThrows(ParseException.class, ParserUtil.MESSAGE_ADD_ADDRESS_CONSTRAINTS, () ->
                ParserUtil.parseAddressForAdd("A".repeat(201)));
    }

    @Test
    public void parseAddressForAdd_outerWhitespaceExcluded_preservesInternalSpacingAndCase() throws Exception {
        String address = "Mixed  Case " + "A".repeat(188);
        assertEquals(200, address.length());
        assertEquals(new Address(address), ParserUtil.parseAddressForAdd(WHITESPACE + address + WHITESPACE));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_ADD_ADDRESS_CONSTRAINTS, () ->
                ParserUtil.parseAddressForAdd("  " + address + "A  "));
    }

    @Test
    public void parseAddressForAdd_supplementaryCharacters_countsUnicodeCodePoints() throws Exception {
        String character = "\uD83C\uDFE0";
        String address = character.repeat(200);
        assertEquals(new Address(address), ParserUtil.parseAddressForAdd(address));
        assertThrows(ParseException.class, ParserUtil.MESSAGE_ADD_ADDRESS_CONSTRAINTS, () ->
                ParserUtil.parseAddressForAdd(address + character));
    }

    @Test
    public void parseAddress_legacyOversizedAddress_remainsAccepted() throws Exception {
        String address = "A".repeat(201);
        assertEquals(new Address(address), ParserUtil.parseAddress(address));
    }

    @Test
    public void parseEmail_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseEmail((String) null));
    }

    @Test
    public void parseEmail_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseEmail(INVALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithoutWhitespace_returnsEmail() throws Exception {
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(VALID_EMAIL));
    }

    @Test
    public void parseEmail_validValueWithWhitespace_returnsTrimmedEmail() throws Exception {
        String emailWithWhitespace = WHITESPACE + VALID_EMAIL + WHITESPACE;
        Email expectedEmail = new Email(VALID_EMAIL);
        assertEquals(expectedEmail, ParserUtil.parseEmail(emailWithWhitespace));
    }

    @Test
    public void parseGoal_validAndInvalidValues() throws Exception {
        assertEquals(new Goal("Build strength"), ParserUtil.parseGoal(" Build strength "));
        assertThrows(ParseException.class, Goal.MESSAGE_CONSTRAINTS, () -> ParserUtil.parseGoal("!!!"));
    }

    @Test
    public void parseTag_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTag(null));
    }

    @Test
    public void parseTag_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTag(INVALID_TAG));
    }

    @Test
    public void parseTag_validValueWithoutWhitespace_returnsTag() throws Exception {
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(VALID_TAG_1));
    }

    @Test
    public void parseTag_validValueWithWhitespace_returnsTrimmedTag() throws Exception {
        String tagWithWhitespace = WHITESPACE + VALID_TAG_1 + WHITESPACE;
        Tag expectedTag = new Tag(VALID_TAG_1);
        assertEquals(expectedTag, ParserUtil.parseTag(tagWithWhitespace));
    }

    @Test
    public void parseTags_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseTags(null));
    }

    @Test
    public void parseTags_collectionWithInvalidTags_throwsParseException() {
        assertThrows(ParseException.class, () -> ParserUtil.parseTags(List.of(VALID_TAG_1, INVALID_TAG)));
    }

    @Test
    public void parseTags_maxDistinctTags_returnsTagSet() throws Exception {
        List<String> tags = List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten");
        assertEquals(Tag.MAX_TAGS, ParserUtil.parseTags(tags).size());
    }

    @Test
    public void parseTags_moreThanMaxDistinctTags_throwsParseException() {
        List<String> tags = List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "eleven");
        assertThrows(ParseException.class, Tag.MESSAGE_TOO_MANY_TAGS, () -> ParserUtil.parseTags(tags));
    }

    @Test
    public void parseTags_duplicatesCollapseToMax_returnsTagSet() throws Exception {
        List<String> tags = List.of("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "ten", "one");
        assertEquals(Tag.MAX_TAGS, ParserUtil.parseTags(tags).size());
    }

    @Test
    public void parseTags_emptyCollection_returnsEmptySet() throws Exception {
        assertTrue(ParserUtil.parseTags(List.of()).isEmpty());
    }

    @Test
    public void parseTags_collectionWithValidTags_returnsTagSet() throws Exception {
        Set<Tag> actualTagSet = ParserUtil.parseTags(List.of(VALID_TAG_1, VALID_TAG_2));
        Set<Tag> expectedTagSet = Set.of(new Tag(VALID_TAG_1), new Tag(VALID_TAG_2));

        assertEquals(expectedTagSet, actualTagSet);
    }

    @Test
    public void parseExerciseName_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseExerciseName((String) null));
    }

    @Test
    public void parseExerciseName_invalidValue_throwsParseException() {
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_NAME, ()
            -> ParserUtil.parseExerciseName(INVALID_EXERCISE_NAME));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_NAME, ()
            -> ParserUtil.parseExerciseName(""));
    }

    @Test
    public void parseExerciseName_validValueWithoutWhitespace_returnsTrimmedName() throws Exception {
        assertEquals(VALID_EXERCISE_NAME, ParserUtil.parseExerciseName(VALID_EXERCISE_NAME));
    }

    @Test
    public void parseExerciseName_validValueWithWhitespace_returnsTrimmedName() throws Exception {
        String nameWithWhitespace = WHITESPACE + VALID_EXERCISE_NAME + WHITESPACE;
        assertEquals(VALID_EXERCISE_NAME, ParserUtil.parseExerciseName(nameWithWhitespace));
    }

    @Test
    public void parseExerciseSets_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseExerciseSets((String) null));
    }

    @Test
    public void parseExerciseSets_invalidValue_throwsParseException() {
        // zero and negative values violate Exercise#isValidSets
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_SETS, ()
            -> ParserUtil.parseExerciseSets("0"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_SETS, ()
            -> ParserUtil.parseExerciseSets("-1"));
        // non-numeric values cannot be parsed
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_SETS, ()
            -> ParserUtil.parseExerciseSets("abc"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_SETS, ()
            -> ParserUtil.parseExerciseSets("1.5"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_SETS, ()
            -> ParserUtil.parseExerciseSets(""));
    }

    @Test
    public void parseExerciseSets_validValueWithoutWhitespace_returnsSets() throws Exception {
        assertEquals(3, ParserUtil.parseExerciseSets(VALID_EXERCISE_SETS));
    }

    @Test
    public void parseExerciseSets_validValueWithWhitespace_returnsTrimmedSets() throws Exception {
        String setsWithWhitespace = WHITESPACE + VALID_EXERCISE_SETS + WHITESPACE;
        assertEquals(3, ParserUtil.parseExerciseSets(setsWithWhitespace));
    }

    @Test
    public void parseExerciseReps_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseExerciseReps((String) null));
    }

    @Test
    public void parseExerciseReps_invalidValue_throwsParseException() {
        // zero and negative values violate Exercise#isValidReps
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_REPS, ()
            -> ParserUtil.parseExerciseReps("0"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_REPS, ()
            -> ParserUtil.parseExerciseReps("-5"));
        // non-numeric values cannot be parsed
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_REPS, ()
            -> ParserUtil.parseExerciseReps("abc"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_REPS, ()
            -> ParserUtil.parseExerciseReps("1.5"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_REPS, ()
            -> ParserUtil.parseExerciseReps(""));
    }

    @Test
    public void parseExerciseReps_validValueWithoutWhitespace_returnsReps() throws Exception {
        assertEquals(10, ParserUtil.parseExerciseReps(VALID_EXERCISE_REPS));
    }

    @Test
    public void parseExerciseReps_validValueWithWhitespace_returnsTrimmedReps() throws Exception {
        String repsWithWhitespace = WHITESPACE + VALID_EXERCISE_REPS + WHITESPACE;
        assertEquals(10, ParserUtil.parseExerciseReps(repsWithWhitespace));
    }

    @Test
    public void parseExerciseWeight_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> ParserUtil.parseExerciseWeight((String) null));
    }

    @Test
    public void parseExerciseWeight_invalidValue_throwsParseException() {
        // negative values violate Exercise#isValidWeight
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, ()
            -> ParserUtil.parseExerciseWeight("-1"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, ()
            -> ParserUtil.parseExerciseWeight("-0.1"));
        // non-numeric values cannot be parsed
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, ()
            -> ParserUtil.parseExerciseWeight("abc"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, ()
            -> ParserUtil.parseExerciseWeight(""));
        // non-finite values violate Exercise#isValidWeight
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, ()
            -> ParserUtil.parseExerciseWeight("Infinity"));
        assertThrows(ParseException.class, Exercise.MESSAGE_CONSTRAINTS_WEIGHT, ()
            -> ParserUtil.parseExerciseWeight("NaN"));
    }

    @Test
    public void parseExerciseWeight_validValueWithoutWhitespace_returnsWeight() throws Exception {
        assertEquals(60.0, ParserUtil.parseExerciseWeight(VALID_EXERCISE_WEIGHT));
        // zero is valid for bodyweight exercises
        assertEquals(0.0, ParserUtil.parseExerciseWeight("0"));
        // decimal weights are valid
        assertEquals(20.5, ParserUtil.parseExerciseWeight("20.5"));
    }

    @Test
    public void parseExerciseWeight_validValueWithWhitespace_returnsTrimmedWeight() throws Exception {
        String weightWithWhitespace = WHITESPACE + VALID_EXERCISE_WEIGHT + WHITESPACE;
        assertEquals(60.0, ParserUtil.parseExerciseWeight(weightWithWhitespace));
    }
}
