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
    private final DietaryRestrictions dietaryRestrictions;
    private final HealthProfile healthProfile;
    private final List<TrainingSession> trainingSessions;
    private final List<WorkoutSession> workoutSessions;
    private final Optional<MembershipAssignment> membershipAssignment;
    private final BodyProgress bodyProgress;
    private final List<PaymentRecord> paymentRecords;

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
     * The client starts with no dietary restrictions, health profile, or membership assignment.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, new DietaryRestrictions(), null, Optional.empty());
    }

    /** Constructs a person with dietary restrictions and no health profile or membership assignment. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            DietaryRestrictions dietaryRestrictions) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, dietaryRestrictions, null, Optional.empty());
    }

    /** Constructs a person with health considerations and no dietary restrictions or membership assignment. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            HealthProfile healthProfile) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, new DietaryRestrictions(), healthProfile, Optional.empty());
    }

    /** Constructs a person with an optional membership assignment and no dietary restrictions or health profile. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            Optional<MembershipAssignment> membershipAssignment) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, new DietaryRestrictions(), null, membershipAssignment);
    }

    /** Constructs a person with dietary restrictions, a health profile, and no membership assignment. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            DietaryRestrictions dietaryRestrictions, HealthProfile healthProfile) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, dietaryRestrictions, healthProfile, Optional.empty());
    }

    /**
     * Constructs a person with all supported client information.
     * All arguments except the goal, health profile, and membership assignment, and all session entries,
     * must be non-null. Both session lists are defensively copied, preserving their order.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            DietaryRestrictions dietaryRestrictions, HealthProfile healthProfile,
            Optional<MembershipAssignment> membershipAssignment) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, dietaryRestrictions, healthProfile, membershipAssignment, new BodyProgress());
    }

    /** Constructs a person with every supported field, including their immutable body tracker. */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            DietaryRestrictions dietaryRestrictions, HealthProfile healthProfile,
            Optional<MembershipAssignment> membershipAssignment, BodyProgress bodyProgress) {
        this(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, trainingSessions,
                workoutSessions, dietaryRestrictions, healthProfile, membershipAssignment, bodyProgress, List.of());
    }

    /**
     * Constructs a person with every supported field, including the body tracker and payment history.
     * The payment list is defensively copied in entry order.
     */
    public Person(Name name, Phone phone, Email email, Address address, Remark remark, Set<Tag> tags,
            WorkoutPlan workoutPlan, Optional<DietPlan> dietPlan, Goal goal,
            List<TrainingSession> trainingSessions, List<WorkoutSession> workoutSessions,
            DietaryRestrictions dietaryRestrictions, HealthProfile healthProfile,
            Optional<MembershipAssignment> membershipAssignment, BodyProgress bodyProgress,
            List<PaymentRecord> paymentRecords) {
        requireAllNonNull(name, phone, email, address, remark, tags, workoutPlan, dietPlan,
                trainingSessions, workoutSessions, dietaryRestrictions, membershipAssignment, bodyProgress,
                paymentRecords);
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.remark = remark;
        this.tags.addAll(tags);
        this.workoutPlan = workoutPlan;
        this.dietPlan = dietPlan;
        this.goal = goal;
        this.dietaryRestrictions = dietaryRestrictions;
        this.healthProfile = healthProfile;
        this.trainingSessions = List.copyOf(trainingSessions);
        this.workoutSessions = List.copyOf(workoutSessions);
        this.membershipAssignment = membershipAssignment;
        this.bodyProgress = bodyProgress;
        this.paymentRecords = List.copyOf(paymentRecords);
    }

    public BodyProgress getBodyProgress() {
        return bodyProgress;
    }

    /** Returns an immutable list of payment records in entry order. */
    public List<PaymentRecord> getPaymentRecords() {
        return paymentRecords;
    }

    /** Returns a copy with one payment record appended, retaining every other field. */
    public Person withAddedPayment(PaymentRecord paymentRecord) {
        Objects.requireNonNull(paymentRecord);
        List<PaymentRecord> updatedPayments = new ArrayList<>(paymentRecords);
        updatedPayments.add(paymentRecord);
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, dietaryRestrictions, healthProfile, membershipAssignment,
                bodyProgress, updatedPayments);
    }

    /** Returns a copy with body data replaced and all unrelated fields retained. */
    public Person withBodyProgress(BodyProgress progress) {
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, dietaryRestrictions, healthProfile, membershipAssignment, progress,
                paymentRecords);
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

    /** Returns the client's current dietary restrictions. */
    public DietaryRestrictions getDietaryRestrictions() {
        return dietaryRestrictions;
    }

    /** Returns the client's current health considerations, if recorded. */
    public Optional<HealthProfile> getHealthProfile() {
        return Optional.ofNullable(healthProfile);
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
                trainingSessions, workoutSessions, dietaryRestrictions, healthProfile, membershipAssignment,
                bodyProgress, paymentRecords);
    }

    /** Returns a copy with one additional session. */
    public Person withTrainingSession(TrainingSession session) {
        Objects.requireNonNull(session);
        List<TrainingSession> updated = new ArrayList<>(trainingSessions);
        updated.add(session);
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal, updated,
                workoutSessions, dietaryRestrictions, healthProfile, membershipAssignment, bodyProgress,
                paymentRecords);
    }

    /** Returns a copy with the supplied current dietary restrictions. */
    public Person withDietaryRestrictions(DietaryRestrictions restrictions) {
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, restrictions, healthProfile, membershipAssignment, bodyProgress,
                paymentRecords);
    }

    /** Returns a copy with the supplied current health profile, or none when {@code profile} is null. */
    public Person withHealthProfile(HealthProfile profile) {
        return new Person(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                trainingSessions, workoutSessions, dietaryRestrictions, profile, membershipAssignment, bodyProgress,
                paymentRecords);
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
                && dietaryRestrictions.equals(otherPerson.dietaryRestrictions)
                && Objects.equals(healthProfile, otherPerson.healthProfile)
                && trainingSessions.equals(otherPerson.trainingSessions)
                && workoutSessions.equals(otherPerson.workoutSessions)
                && membershipAssignment.equals(otherPerson.membershipAssignment)
                && bodyProgress.equals(otherPerson.bodyProgress)
                && paymentRecords.equals(otherPerson.paymentRecords);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, remark, tags, workoutPlan, dietPlan, goal,
                dietaryRestrictions, healthProfile, trainingSessions, workoutSessions, membershipAssignment,
                bodyProgress, paymentRecords);
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
                .add("dietaryRestrictions", dietaryRestrictions)
                .add("goal", goal)
                .add("healthProfile", healthProfile)
                .add("trainingSessions", trainingSessions)
                .add("workoutSessions", workoutSessions)
                .add("membershipAssignment", membershipAssignment)
                .add("bodyProgress", bodyProgress)
                .add("paymentRecords", paymentRecords)
                .toString();
    }

}
