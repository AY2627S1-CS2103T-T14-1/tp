package seedu.address.storage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.membership.MembershipAssignment;

/**
 * Jackson-friendly version of {@link MembershipAssignment}.
 * The expiry date is stored as an ISO {@code YYYY-MM-DD} string when present.
 * No future-date check applies on load: an assignment stays loadable after its date passes.
 */
class JsonAdaptedMembershipAssignment {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Membership assignment's %s field is missing!";
    public static final String MESSAGE_INVALID_EXPIRY =
            "Membership assignment expiry date must be in YYYY-MM-DD format.";

    private final String planKey;
    private final String planName;
    private final Integer remainingSessions;
    private final String expiryDate;

    /**
     * Constructs a {@code JsonAdaptedMembershipAssignment} with the given assignment details.
     * A missing {@code expiryDate} means the assignment has no specified expiry date.
     */
    @JsonCreator
    public JsonAdaptedMembershipAssignment(@JsonProperty("planKey") String planKey,
            @JsonProperty("planName") String planName,
            @JsonProperty("remainingSessions") Integer remainingSessions,
            @JsonProperty("expiryDate") String expiryDate) {
        this.planKey = planKey;
        this.planName = planName;
        this.remainingSessions = remainingSessions;
        this.expiryDate = expiryDate;
    }

    /**
     * Converts a given {@code MembershipAssignment} into this class for Jackson use.
     */
    public JsonAdaptedMembershipAssignment(MembershipAssignment source) {
        planKey = source.getPlanKey();
        planName = source.getPlanName();
        remainingSessions = source.getRemainingSessions();
        expiryDate = source.getExpiryDate().map(LocalDate::toString).orElse(null);
    }

    /**
     * Converts this Jackson-friendly adapted assignment into the model's
     * {@code MembershipAssignment} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public MembershipAssignment toModelType() throws IllegalValueException {
        if (planKey == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "planKey"));
        }
        if (planName == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "planName"));
        }
        if (remainingSessions == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "remainingSessions"));
        }
        final Optional<LocalDate> modelExpiryDate;
        if (expiryDate == null) {
            modelExpiryDate = Optional.empty();
        } else {
            try {
                modelExpiryDate = Optional.of(LocalDate.parse(expiryDate));
            } catch (DateTimeParseException e) {
                throw new IllegalValueException(MESSAGE_INVALID_EXPIRY);
            }
        }
        try {
            return new MembershipAssignment(planKey, planName, remainingSessions, modelExpiryDate);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
