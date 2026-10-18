package seedu.address.storage;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;

/**
 * Jackson-friendly version of {@link MembershipPlan}.
 * The price is stored as an exact decimal string to preserve scale.
 */
class JsonAdaptedMembershipPlan {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Membership plan's %s field is missing!";

    private final String name;
    private final String price;
    private final Integer startingSessions;

    /**
     * Constructs a {@code JsonAdaptedMembershipPlan} with the given plan details.
     * Older files may omit the whole plan list, but a present entry must define every field.
     */
    @JsonCreator
    public JsonAdaptedMembershipPlan(@JsonProperty("name") String name,
            @JsonProperty("price") String price,
            @JsonProperty("startingSessions") Integer startingSessions) {
        this.name = name;
        this.price = price;
        this.startingSessions = startingSessions;
    }

    /**
     * Converts a given {@code MembershipPlan} into this class for Jackson use.
     */
    public JsonAdaptedMembershipPlan(MembershipPlan source) {
        name = source.getName().value;
        price = source.getPrice().toPlainString();
        startingSessions = source.getStartingSessions();
    }

    /**
     * Converts this Jackson-friendly adapted plan into the model's {@code MembershipPlan} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public MembershipPlan toModelType() throws IllegalValueException {
        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "name"));
        }
        if (!MembershipName.isValidMembershipName(name)) {
            throw new IllegalValueException(MembershipName.MESSAGE_CONSTRAINTS);
        }
        if (price == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "price"));
        }
        final BigDecimal modelPrice;
        try {
            modelPrice = new BigDecimal(price);
        } catch (NumberFormatException e) {
            throw new IllegalValueException(MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        }
        if (!MembershipPlan.isValidPrice(modelPrice)) {
            throw new IllegalValueException(MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        }
        if (startingSessions == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, "startingSessions"));
        }
        if (!MembershipPlan.isValidStartingSessions(startingSessions)) {
            throw new IllegalValueException(MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        }
        return new MembershipPlan(new MembershipName(name), modelPrice, startingSessions);
    }
}
