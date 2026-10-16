package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Remark remark;
    private final Set<Tag> tags = new HashSet<>();
    private final WorkoutPlan workoutPlan;
    private final Optional<DietPlan> dietPlan;
    private final Goal goal;
    private final List<TrainingSession> trainingSessions;
    private final List<WorkoutSession> workoutSessions;
    private final Optional<MembershipAssignment> membershipAssignment;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags) {
        this(name, phone, email, address, remark, tags, new WorkoutPlan(), Optional.empty());
    }

    /**
     * Constructs a person with a workout plan and no diet plan.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan) {
        this(name, phone, email, address, remark, tags, workoutPlan, Optional.empty());
    }

    /**
     * Constructs a person with a diet plan and an empty workout plan.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            Optional<DietPlan> dietPlan) {
        this(name, phone, email, address, remark, tags, new WorkoutPlan(), dietPlan);
    }

    /**
     * Constructs a person with both plans. All arguments must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, List.of());
    }

    /** Constructs a person with saved training sessions. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, List<TrainingSession> trainingSessions) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, null, trainingSessions);
    }

    /** Constructs a person with a fitness goal and no training sessions. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, List.of());
    }

    /** Constructs a person with a fitness goal and saved training sessions. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal, List<TrainingSession> trainingSessions) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions, List.of());
    }

    /**
     * Constructs a person with plans, an optional goal, scheduled training, and recorded workouts.
     * All arguments except the goal, and all session entries, must be non-null.
     * Both session lists are defensively copied, preserving their order.
     * The client starts with no membership assignment.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, Optional.empty());
    }

    /**
     * Constructs a person with plans, an optional goal, scheduled training, recorded workouts,
     * and an optional membership assignment.
     * All arguments except the goal, and all session entries, must be non-null.
     * Both session lists are defensively copied, preserving their order.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            Optional<MembershipAssignment> membershipAssignment) {
        requireAllNonNull(name, phone, email, address, remark, tags, workoutPlan, dietPlan,
                trainingSessions, workoutSessions, membershipAssignment);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        this.tags.addAll(tags);
        this.workoutPlan = workoutPlan;
        this.dietPlan = dietPlan;
        this.goal = goal;
        this.trainingSessions = List.copyOf(trainingSessions);
        this.workoutSessions = List.copyOf(workoutSessions);
        this.membershipAssignment = membershipAssignment;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public Remark getRemark() {
        return remark;
    }

    public WorkoutPlan getWorkoutPlan() {
        return workoutPlan;
    }

    public Optional<DietPlan> getDietPlan() {
        return dietPlan;
    }

    public Optional<Goal> getGoal() {
        return Optional.ofNullable(goal);
    }

    /** Returns an unmodifiable list of recorded workout sessions. */
    public List<WorkoutSession> getWorkoutSessions() {
        return workoutSessions;
    }

    public List<TrainingSession> getTrainingSessions() {
        return trainingSessions;
    }

    /** Returns the client's optional membership assignment. */
    public Optional<MembershipAssignment> getMembershipAssignment() {
        return membershipAssignment;
    }

    /** Returns a copy of this person with the membership assignment replaced. */
    public Person withMembershipAssignment(Optional<MembershipAssignment> membershipAssignment) {
        Objects.requireNonNull(membershipAssignment);
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, membershipAssignment);
    }

    /** Returns a copy with one additional session. */
    public Person withTrainingSession(TrainingSession session) {
        Objects.requireNonNull(session);
        List<TrainingSession> updated = new ArrayList<>(trainingSessions);
        updated.add(session);
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, updated,
                workoutSessions);
    }

    /**
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && remark.equals(otherPerson.remark)
                && tags.equals(otherPerson.tags)
                && workoutPlan.equals(otherPerson.workoutPlan)
                && dietPlan.equals(otherPerson.dietPlan)
                && Objects.equals(goal, otherPerson.goal)
                && trainingSessions.equals(otherPerson.trainingSessions)
                && workoutSessions.equals(otherPerson.workoutSessions)
                && membershipAssignment.equals(otherPerson.membershipAssignment);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, membershipAssignment);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("remark", remark)
                .add("tags", tags)
                .add("workoutPlan", workoutPlan)
                .add("dietPlan", dietPlan)
                .add("goal", goal)
                .add("trainingSessions", trainingSessions)
                .add("workoutSessions", workoutSessions)
                .add("membershipAssignment", membershipAssignment)
                .toString();
    }

}
