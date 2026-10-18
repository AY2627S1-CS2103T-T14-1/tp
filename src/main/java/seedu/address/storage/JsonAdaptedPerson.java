package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.DietPlan;
import seedu.address.model.person.DietaryRestrictions;
import seedu.address.model.person.Email;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Remark;
import seedu.address.model.person.TrainingSession;
import seedu.address.model.person.WorkoutPlan;
import seedu.address.model.person.WorkoutSession;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final String remark;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedExercise> exercises = new ArrayList<>();
    private final JsonAdaptedDietPlan dietPlan;
    private final String goal;
    private final List<String> dietaryRestrictions = new ArrayList<>();
    private final List<JsonAdaptedTrainingSession> trainingSessions = new ArrayList<>();
    private final List<JsonAdaptedWorkoutSession> workoutSessions = new ArrayList<>();

    /**
     * Constructs an adapter without plans or workout sessions.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, remark, tags, null, null, null, null, null, null);
    }

    /**
     * Constructs an adapter with exercises, no diet plan, and no workout sessions.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags, List<JsonAdaptedExercise> exercises) {
        this(name, phone, email, address, remark, tags, exercises, null, null, null, null, null);
    }

    /**
     * Constructs an adapter with plans and no workout sessions.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags, List<JsonAdaptedExercise> exercises,
            JsonAdaptedDietPlan dietPlan) {
        this(name, phone, email, address, remark, tags, exercises, dietPlan, null, null, null, null);
    }

    /**
     * Constructs an adapter with plans and an optional goal, but no workout sessions.
     */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags, List<JsonAdaptedExercise> exercises,
            JsonAdaptedDietPlan dietPlan, String goal) {
        this(name, phone, email, address, remark, tags, exercises, dietPlan, null, goal, null, null);
    }

    /** Constructs an adapter with scheduled training and no recorded workouts. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags, List<JsonAdaptedExercise> exercises,
            JsonAdaptedDietPlan dietPlan, List<JsonAdaptedTrainingSession> trainingSessions, String goal) {
        this(name, phone, email, address, remark, tags, exercises, dietPlan, trainingSessions, goal, null, null);
    }

    /** Constructs an adapter with recorded workouts but no dietary restrictions. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, String remark,
            List<JsonAdaptedTag> tags, List<JsonAdaptedExercise> exercises,
            JsonAdaptedDietPlan dietPlan, List<JsonAdaptedTrainingSession> trainingSessions, String goal,
            List<JsonAdaptedWorkoutSession> workoutSessions) {
        this(name, phone, email, address, remark, tags, exercises, dietPlan, trainingSessions, goal,
                workoutSessions, null);
    }

    /**
     * Constructs an adapter from stored person details.
     * Older files may omit exercises, dietPlan, goal, trainingSessions, and workoutSessions.
     */
    @JsonCreator
    public JsonAdaptedPerson(
            @JsonProperty("name") String name,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("remark") String remark,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("exercises") List<JsonAdaptedExercise> exercises,
            @JsonProperty("dietPlan") JsonAdaptedDietPlan dietPlan,
            @JsonProperty("trainingSessions") List<JsonAdaptedTrainingSession> trainingSessions,
            @JsonProperty("goal") String goal,
            @JsonProperty("workoutSessions") List<JsonAdaptedWorkoutSession> workoutSessions,
            @JsonProperty("dietaryRestrictions") List<String> dietaryRestrictions) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (exercises != null) {
            this.exercises.addAll(exercises);
        }
        this.dietPlan = dietPlan;
        this.goal = goal;
        if (dietaryRestrictions != null) {
            this.dietaryRestrictions.addAll(dietaryRestrictions);
        }
        if (trainingSessions != null) {
            this.trainingSessions.addAll(trainingSessions);
        }
        if (workoutSessions != null) {
            this.workoutSessions.addAll(workoutSessions);
        }
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        remark = source.getRemark().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        exercises.addAll(source.getWorkoutPlan().getExercises().stream()
                .map(JsonAdaptedExercise::new)
                .collect(Collectors.toList()));
        dietPlan = source.getDietPlan().map(JsonAdaptedDietPlan::new).orElse(null);
        goal = source.getGoal().map(value -> value.value).orElse(null);
        dietaryRestrictions.addAll(source.getDietaryRestrictions().getValues());
        trainingSessions.addAll(source.getTrainingSessions().stream()
                .map(JsonAdaptedTrainingSession::new)
                .collect(Collectors.toList()));
        workoutSessions.addAll(source.getWorkoutSessions().stream()
                .map(JsonAdaptedWorkoutSession::new)
                .collect(Collectors.toList()));
    }

    /**
     * Converts this adapter into the model's {@code Person} object.
     *
     * @throws IllegalValueException if stored person data violates a constraint.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            personTags.add(tag.toModelType());
        }

        final List<Exercise> personExercises = new ArrayList<>();
        for (JsonAdaptedExercise exercise : exercises) {
            if (exercise == null) {
                throw new IllegalValueException("Workout plan must not contain null exercises");
            }
            personExercises.add(exercise.toModelType());
        }
        final WorkoutPlan modelWorkoutPlan = new WorkoutPlan(personExercises);

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        if (remark == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Remark.class.getSimpleName()));
        }
        final Remark modelRemark = new Remark(remark);

        final Set<Tag> modelTags = new HashSet<>(personTags);
        final Optional<DietPlan> modelDietPlan = dietPlan == null
                ? Optional.empty() : Optional.of(dietPlan.toModelType());

        if (goal != null && !Goal.isValidGoal(goal)) {
            throw new IllegalValueException(Goal.MESSAGE_CONSTRAINTS);
        }
        final Goal modelGoal = goal == null ? null : new Goal(goal);
        final DietaryRestrictions modelDietaryRestrictions;
        try {
            modelDietaryRestrictions = dietaryRestrictions.isEmpty()
                    ? new DietaryRestrictions() : new DietaryRestrictions(dietaryRestrictions);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new IllegalValueException(DietaryRestrictions.MESSAGE_CONSTRAINTS);
        }

        final List<TrainingSession> modelTrainingSessions = new ArrayList<>();
        for (JsonAdaptedTrainingSession session : trainingSessions) {
            if (session == null) {
                throw new IllegalValueException("Training sessions must not contain null entries");
            }
            modelTrainingSessions.add(session.toModelType());
        }

        final List<WorkoutSession> modelSessions = new ArrayList<>();
        for (JsonAdaptedWorkoutSession adaptedSession : workoutSessions) {
            if (adaptedSession == null) {
                throw new IllegalValueException("Workout sessions must not contain null sessions.");
            }

            WorkoutSession session = adaptedSession.toModelType();
            if (modelSessions.stream().anyMatch(existing -> existing.hasSameDateTime(session))) {
                throw new IllegalValueException(
                        "Workout sessions must not contain duplicate dates and times.");
            }
            modelSessions.add(session);
        }

        return new Person(modelName, modelPhone, modelEmail, modelAddress, modelRemark, modelTags,
                modelWorkoutPlan, modelDietPlan, modelGoal, modelTrainingSessions, modelSessions,
                modelDietaryRestrictions);
    }
}
