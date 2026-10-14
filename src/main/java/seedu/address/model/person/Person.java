package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
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
    private final List<WorkoutSession> workoutSessions;

    /**
     * Constructs a person with no plans, fitness goal, or recorded workout sessions.
     * Every argument must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags) {
        this(name, phone, email, address, remark, tags, new WorkoutPlan(), Optional.empty(), null, List.of());
    }

    /**
     * Constructs a person with a workout plan, no diet plan, no goal, and no recorded sessions.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan) {
        this(name, phone, email, address, remark, tags, workoutPlan, Optional.empty(), null, List.of());
    }

    /**
     * Constructs a person with a diet plan, an empty workout plan, no goal, and no recorded sessions.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            Optional<DietPlan> dietPlan) {
        this(name, phone, email, address, remark, tags, new WorkoutPlan(), dietPlan, null, List.of());
    }

    /**
     * Constructs a person with both plans, no fitness goal, and no recorded workout sessions.
     * All arguments must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, null, List.of());
    }

    /**
     * Constructs a person with plans and an optional goal, but no recorded workout sessions.
     * All arguments except the goal must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, List.of());
    }

    /**
     * Constructs a person with plans, an optional goal, and recorded workout sessions.
     * All arguments except the goal, and all session entries, must be non-null.
     * The session list is defensively copied, preserving its order.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<WorkoutSession> workoutSessions) {
        requireAllNonNull(name, phone, email, address, remark, tags, workoutPlan, dietPlan, workoutSessions);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        this.tags.addAll(tags);
        this.workoutPlan = workoutPlan;
        this.dietPlan = dietPlan;
        this.goal = goal;
        this.workoutSessions = List.copyOf(workoutSessions);
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

    /**
     * Returns an unmodifiable list of recorded workout sessions.
     */
    public List<WorkoutSession> getWorkoutSessions() {
        return workoutSessions;
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
                && workoutSessions.equals(otherPerson.workoutSessions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email, address, remark, tags,
                workoutPlan, dietPlan, goal, workoutSessions);
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
                .add("workoutSessions", workoutSessions)
                .toString();
    }
}
