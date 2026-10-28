package seedu.address.storage;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
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
    private final JsonNode calories;
    private final JsonNode proteinGrams;
    private final JsonNode carbsGrams;
    private final JsonNode fatGrams;

    /**
     * Creates an adapter from stored JSON fields. Missing or null notes represent absent notes.
     */
    public JsonAdaptedDietPlan(@JsonProperty("target") JsonNode target, @JsonProperty("notes") JsonNode notes) {
        this(target, notes, null, null, null, null);
    }

    /** Creates an adapter including optional structured nutrition values. */
    @JsonCreator
    public JsonAdaptedDietPlan(@JsonProperty("target") JsonNode target, @JsonProperty("notes") JsonNode notes,
            @JsonProperty("calories") JsonNode calories, @JsonProperty("proteinGrams") JsonNode proteinGrams,
            @JsonProperty("carbsGrams") JsonNode carbsGrams, @JsonProperty("fatGrams") JsonNode fatGrams) {
        this.target = target;
        this.notes = notes;
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
    }

    /**
     * Creates an adapter from a validated model plan.
     */
    public JsonAdaptedDietPlan(DietPlan source) {
        target = TextNode.valueOf(source.getTarget());
        notes = source.getNotes().map(TextNode::valueOf).orElse(null);
        calories = source.getCalories().map(IntNode::valueOf).orElse(null);
        proteinGrams = source.getProteinGrams().map(IntNode::valueOf).orElse(null);
        carbsGrams = source.getCarbsGrams().map(IntNode::valueOf).orElse(null);
        fatGrams = source.getFatGrams().map(IntNode::valueOf).orElse(null);
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
        Optional<String> modelNotes = Optional.empty();
        if (notes != null && !notes.isNull()) {
            if (!notes.isTextual() || !DietPlan.isValidNotes(notes.textValue())) {
                throw new IllegalValueException(DietPlan.MESSAGE_CONSTRAINTS_NOTES);
            }
            modelNotes = Optional.of(notes.textValue());
        }
        if (calories != null && !calories.isNull()) {
            int calorieValue = checkedInteger(calories, true);
            Optional<Integer> protein = checkedOptionalMacro(proteinGrams);
            Optional<Integer> carbs = checkedOptionalMacro(carbsGrams);
            Optional<Integer> fat = checkedOptionalMacro(fatGrams);
            return DietPlan.structured(calorieValue, protein, carbs, fat, modelNotes);
        }
        if (proteinGrams != null && !proteinGrams.isNull() || carbsGrams != null && !carbsGrams.isNull()
                || fatGrams != null && !fatGrams.isNull()) {
            throw new IllegalValueException("Structured diet macros require calories.");
        }
        if (modelNotes.isEmpty()) {
            return new DietPlan(target.textValue());
        }
        return new DietPlan(target.textValue(), modelNotes.orElseThrow());
    }

    private static Optional<Integer> checkedOptionalMacro(JsonNode value) throws IllegalValueException {
        return value == null || value.isNull() ? Optional.empty() : Optional.of(checkedInteger(value, false));
    }

    private static int checkedInteger(JsonNode value, boolean positive) throws IllegalValueException {
        if (!value.isIntegralNumber() || !value.canConvertToInt()
                || (positive ? value.intValue() <= 0 : value.intValue() < 0)) {
            throw new IllegalValueException(positive
                    ? "Calorie target must be a positive whole number."
                    : "Macros must be whole non-negative numbers of grams.");
        }
        return value.intValue();
    }
}
