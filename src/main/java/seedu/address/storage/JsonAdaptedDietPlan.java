package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.DietPlan;

/**
 * A Jackson-friendly diet plan. Raw nodes prevent numbers and booleans being coerced into text.
 */
class JsonAdaptedDietPlan {
    public static final String MESSAGE_MISSING_TARGET = "Diet plan's target field is missing!";

    private final JsonNode target;
    private final JsonNode notes;

    /**
     * Creates an adapter from stored JSON fields. Missing or null notes represent absent notes.
     */
    @JsonCreator
    public JsonAdaptedDietPlan(@JsonProperty("target") JsonNode target, @JsonProperty("notes") JsonNode notes) {
        this.target = target;
        this.notes = notes;
    }

    /**
     * Creates an adapter from a validated model plan.
     */
    public JsonAdaptedDietPlan(DietPlan source) {
        target = TextNode.valueOf(source.getTarget());
        notes = source.getNotes().map(TextNode::valueOf).orElse(null);
    }

    /**
     * Converts stored fields into a validated plan.
     *
     * @throws IllegalValueException if the target is missing or a supplied field is invalid.
     */
    public DietPlan toModelType() throws IllegalValueException {
        if (target == null || target.isNull()) {
            throw new IllegalValueException(MESSAGE_MISSING_TARGET);
        }
        if (!target.isTextual() || !DietPlan.isValidTarget(target.textValue())) {
            throw new IllegalValueException(DietPlan.MESSAGE_CONSTRAINTS_TARGET);
        }
        if (notes == null || notes.isNull()) {
            return new DietPlan(target.textValue());
        }
        if (!notes.isTextual() || !DietPlan.isValidNotes(notes.textValue())) {
            throw new IllegalValueException(DietPlan.MESSAGE_CONSTRAINTS_NOTES);
        }
        return new DietPlan(target.textValue(), notes.textValue());
    }
}
