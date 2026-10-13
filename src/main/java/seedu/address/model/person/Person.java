package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
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

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags) {
        this(name, phone, email, address, remark, tags, new WorkoutPlan(), Optional.empty(), null);
    }

    /**
     * Constructs a person with a workout plan and no diet plan.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan) {
        this(name, phone, email, address, remark, tags, workoutPlan, Optional.empty(), null);
    }

    /**
     * Constructs a person with a diet plan and an empty workout plan.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            Optional<DietPlan> dietPlan) {
        this(name, phone, email, address, remark, tags, new WorkoutPlan(), dietPlan, null);
    }

    /**
     * Constructs a person with both plans. All arguments must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, null);
    }

    /**
     * Constructs a person with optional diet plan and fitness goal. All non-goal arguments must be non-null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal) {
        requireAllNonNull(name, phone, email, address, remark, tags, workoutPlan, dietPlan);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        this.tags.addAll(tags);
        this.workoutPlan = workoutPlan;
        this.dietPlan = dietPlan;
        this.goal = goal;
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
                && Objects.equals(goal, otherPerson.goal);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal);
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
                .toString();
    }

}
