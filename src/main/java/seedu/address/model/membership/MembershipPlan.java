package seedu.address.model.membership;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents an immutable membership plan definition in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class MembershipPlan {

    public static final String MESSAGE_PRICE_CONSTRAINTS =
            "Price must be greater than zero and have at most two decimal places.";
    public static final String MESSAGE_SESSIONS_CONSTRAINTS =
            "Number of sessions must be a whole number from 1 to 1000.";

    public static final int MIN_SESSIONS = 1;
    public static final int MAX_SESSIONS = 1000;
    public static final int PRICE_SCALE = 2;

    private final MembershipName name;
    private final BigDecimal price;
    private final int startingSessions;

    /**
     * Constructs a {@code MembershipPlan}. The price is stored at scale two;
     * {@code startingSessions} is the balance each assigned client starts with.
     *
     * @param name A valid membership plan name.
     * @param price A positive amount with at most two decimal places.
     * @param startingSessions A whole number from 1 to 1000.
     */
    public MembershipPlan(MembershipName name, BigDecimal price, int startingSessions) {
        requireNonNull(name);
        requireNonNull(price);
        checkArgument(isValidPrice(price), MESSAGE_PRICE_CONSTRAINTS);
        checkArgument(isValidStartingSessions(startingSessions), MESSAGE_SESSIONS_CONSTRAINTS);
        this.name = name;
        this.price = price.setScale(PRICE_SCALE);
        this.startingSessions = startingSessions;
    }

    /**
     * Returns true if the given price is positive and has at most two decimal places.
     */
    public static boolean isValidPrice(BigDecimal test) {
        requireNonNull(test);
        return test.compareTo(BigDecimal.ZERO) > 0 && test.scale() <= PRICE_SCALE;
    }

    /**
     * Returns true if the given session count is a whole number from 1 to 1000.
     */
    public static boolean isValidStartingSessions(int test) {
        return test >= MIN_SESSIONS && test <= MAX_SESSIONS;
    }

    public MembershipName getName() {
        return name;
    }

    /**
     * Returns the stable canonical key of this plan's name, used for uniqueness checks
     * and client assignment references.
     */
    public String getKey() {
        return name.getKey();
    }

    /**
     * Returns the price stored at scale two.
     */
    public BigDecimal getPrice() {
        return price;
    }

    public int getStartingSessions() {
        return startingSessions;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof MembershipPlan otherPlan)) {
            return false;
        }

        return name.equals(otherPlan.name)
                && price.equals(otherPlan.price)
                && startingSessions == otherPlan.startingSessions;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, startingSessions);
    }

    @Override
    public String toString() {
        return name + "; Price: " + price.toPlainString() + "; Sessions: " + startingSessions;
    }
}
