package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.storage.JsonAdaptedPerson.MISSING_FIELD_MESSAGE_FORMAT;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.Address;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.Email;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.testutil.PersonBuilder;

public class JsonAdaptedPersonTest {
    private static final String INVALID_NAME = "R@chel";
    private static final String INVALID_PHONE = "+651234";
    private static final String INVALID_ADDRESS = " ";
    private static final String INVALID_EMAIL = "example.com";
    private static final String INVALID_TAG = "#friend";

    private static final String VALID_NAME = BENSON.getName().toString();
    private static final String VALID_PHONE = BENSON.getPhone().toString();
    private static final String VALID_EMAIL = BENSON.getEmail().toString();
    private static final String VALID_ADDRESS = BENSON.getAddress().toString();
    private static final String VALID_REMARK = BENSON.getRemark().toString();
    private static final List<JsonAdaptedTag> VALID_TAGS = BENSON.getTags().stream()
            .map(JsonAdaptedTag::new)
            .collect(Collectors.toList());
    private static final List<JsonAdaptedExercise> VALID_EXERCISES = BENSON.getWorkoutPlan().getExercises().stream()
            .map(JsonAdaptedExercise::new)
            .collect(Collectors.toList());

    @Test
    public void jsonRoundTrip_healthAndDietaryRestrictions_preservesBoth() throws Exception {
        Person person = new PersonBuilder().withDietaryRestrictions("Peanut allergy")
                .withHealthProfile("Avoid jumping").build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(person));
        assertEquals(person, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_validPersonDetails_returnsPerson() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(BENSON);
        assertEquals(BENSON, person.toModelType());
    }

    @Test
    public void toModelType_validPersonWithExercises_returnsPerson() throws Exception {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.5);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        Person personWithExercises = new PersonBuilder(BENSON)
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress, squat)))
                .build();
        JsonAdaptedPerson adaptedPerson = new JsonAdaptedPerson(personWithExercises);
        assertEquals(personWithExercises, adaptedPerson.toModelType());
    }

    @Test
    public void jsonRoundTrip_personWithAllFields_preservesData() throws Exception {
        Exercise benchPress = new Exercise("Bench Press", 3, 10, 60.5);
        Exercise squat = new Exercise("Squat", 4, 8, 80.0);
        Person personWithAllFields = new PersonBuilder(BENSON)
                .withRemark("Likes rowing.")
                .withWorkoutPlan(new WorkoutPlan(Arrays.asList(benchPress, squat)))
                .withDietPlan(new DietPlan("1800 kcal", "Lean protein"))
                .withGoal("Build strength")
                .build();
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(personWithAllFields));
        assertEquals(personWithAllFields,
                JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void jsonRoundTrip_personWithMinimalFields_preservesData() throws Exception {
        // BENSON has no exercises and no diet plan
        String json = JsonUtil.toJsonString(new JsonAdaptedPerson(BENSON));
        assertEquals(BENSON, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void jsonRoundTrip_missingExercisesAndDietPlan_returnsPersonWithEmptyPlanAndNoDiet() throws Exception {
        String json = String.format("{\"name\":\"%s\",\"phone\":\"%s\",\"email\":\"%s\",\"address\":\"%s\","
                + "\"remark\":\"%s\",\"tags\":[]}",
                VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK);
        Person expectedPerson = new PersonBuilder(BENSON).withWorkoutPlan(new WorkoutPlan()).withTags().build();
        assertEquals(expectedPerson, JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType());
    }

    @Test
    public void toModelType_nullExercises_returnsPersonWithEmptyWorkoutPlan() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_REMARK, VALID_TAGS, null);
        Person expectedPerson = new PersonBuilder(BENSON).withWorkoutPlan(new WorkoutPlan()).build();
        assertEquals(expectedPerson, person.toModelType());
    }

    @Test
    public void toModelType_invalidName_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(INVALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK,
                        VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = Name.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullName_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(null, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK,
                VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, INVALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK,
                        VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = Phone.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullPhone_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, null, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK,
                VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, INVALID_EMAIL, VALID_ADDRESS, VALID_REMARK,
                        VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = Email.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullEmail_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, null, VALID_ADDRESS, VALID_REMARK,
                VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, INVALID_ADDRESS, VALID_REMARK,
                        VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = Address.MESSAGE_CONSTRAINTS;
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_nullAddress_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, null, VALID_REMARK,
                VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<JsonAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new JsonAdaptedTag(INVALID_TAG));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK, invalidTags,
                        VALID_EXERCISES);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_nullRemark_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, null,
                VALID_TAGS, VALID_EXERCISES);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Remark.class.getSimpleName());
        assertThrows(IllegalValueException.class, expectedMessage, person::toModelType);
    }

    @Test
    public void toModelType_invalidExercises_throwsIllegalValueException() {
        List<JsonAdaptedExercise> invalidExercises = new ArrayList<>(VALID_EXERCISES);
        invalidExercises.add(new JsonAdaptedExercise(" ", 3, 10, 60.5));
        JsonAdaptedPerson person =
                new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS, VALID_REMARK, VALID_TAGS,
                        invalidExercises);
        assertThrows(IllegalValueException.class, person::toModelType);
    }

    @Test
    public void toModelType_invalidGoal_throwsIllegalValueException() {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_REMARK, VALID_TAGS, VALID_EXERCISES, null, null, "!!!");
        assertThrows(IllegalValueException.class, Goal.MESSAGE_CONSTRAINTS, person::toModelType);
    }

    @Test
    public void toModelType_nullGoal_returnsPersonWithoutGoal() throws Exception {
        JsonAdaptedPerson person = new JsonAdaptedPerson(VALID_NAME, VALID_PHONE, VALID_EMAIL, VALID_ADDRESS,
                VALID_REMARK, VALID_TAGS, VALID_EXERCISES, null, null, null);
        assertEquals(new PersonBuilder(BENSON).build(), person.toModelType());
    }

}
