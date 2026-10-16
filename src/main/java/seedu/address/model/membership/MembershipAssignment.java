package seedu.address.model.membership;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents an immutable membership assignment on a client.
 * Each assignment references exactly one existing {@code MembershipPlan} via its canonical
 * plan key, carries a copy of the plan's display name taken at assignment time, and tracks
 * the client's independent remaining session balance plus an optional expiry date.
 * The expiry date may lie in the past after assignment; only assignment creation requires
 * a future date.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class MembershipAssignment {

    public static final String MESSAGE_SESSIONS_CONSTRAINTS =
            "Remaining sessions must be a whole number from 0 to 1000.";
    public static final String MESSAGE_EXPIRY_CONSTRAINTS =
            "Expiry date must be a valid future date in YYYY-MM-DD format.";

    public static final int MIN_SESSIONS = 0;
    public static final int MAX_SESSIONS = 1000;

    private final String planKey;
    private final String planName;
    private final int remainingSessions;
    private final Optional<LocalDate> expiryDate;

    /**
     * Constructs a {@code MembershipAssignment} with an explicit balance.
     *
     * @param planKey Canonical key of the referenced plan; must be non-blank.
     * @param planName Copy of the plan's display name; must be a valid membership plan name.
     * @param remainingSessions A whole number from 0 to 1000.
     * @param expiryDate Optional expiry date; may be past after assignment.
     */
    public MembershipAssignment(String planKey, String planName, int remainingSessions,
            Optional<LocalDate> expiryDate) {
        requireNonNull(planKey);
        requireNonNull(planName);
        requireNonNull(expiryDate);
        checkArgument(!planKey.isBlank(), "Plan key must not be blank.");
        checkArgument(MembershipName.isValidMembershipName(planName),
                MembershipName.MESSAGE_CONSTRAINTS);
        checkArgument(isValidRemainingSessions(remainingSessions), MESSAGE_SESSIONS_CONSTRAINTS);
        this.planKey = planKey;
        this.planName = MembershipName.normalize(planName);
        this.remainingSessions = remainingSessions;
        this.expiryDate = expiryDate;
    }

    /**
     * Constructs a {@code MembershipAssignment} for a freshly assigned plan.
     * Copies the plan's canonical key and display name, starts the balance at the
     * plan's starting sessions, and records the optional expiry date.
     */
    public MembershipAssignment(MembershipPlan plan, Optional<LocalDate> expiryDate) {
        this(plan.getKey(), plan.getName().value, plan.getStartingSessions(), expiryDate);
    }

    /**
     * Returns true if the given balance is a whole number from 0 to 1000.
     */
    public static boolean isValidRemainingSessions(int test) {
        return test >= MIN_SESSIONS && test <= MAX_SESSIONS;
    }

    public String getPlanKey() {
        return planKey;
    }

    public String getPlanName() {
        return planName;
    }

    public int getRemainingSessions() {
        return remainingSessions;
    }

    public Optional<LocalDate> getExpiryDate() {
        return expiryDate;
    }

    /**
     * Returns a copy of this assignment with the remaining balance replaced,
     * retaining the plan key, copied name, and expiry date.
     */
    public MembershipAssignment withRemainingSessions(int updatedSessions) {
        return new MembershipAssignment(planKey, planName, updatedSessions, expiryDate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof MembershipAssignment otherAssignment)) {
            return false;
        }

        return planKey.equals(otherAssignment.planKey)
                && planName.equals(otherAssignment.planName)
                && remainingSessions == otherAssignment.remainingSessions
                && expiryDate.equals(otherAssignment.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(planKey, planName, remainingSessions, expiryDate);
    }

    @Override
    public String toString() {
        String base = "Membership plan: " + planName + "; Sessions remaining: " + remainingSessions;
        return expiryDate.map(date -> base + "; Expiry: " + date).orElse(base);
    }
}
