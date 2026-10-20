package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyMeasurements;

/** Strict JSON adapter for {@link BodyBaseline}; scalar coercion is deliberately rejected. */
public final class JsonAdaptedBodyBaseline {
    private final JsonNode node;

    /** Retains raw JSON for strict validation during model conversion. */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public JsonAdaptedBodyBaseline(JsonNode node) {
        this.node = node;
    }

    /** Serializes validated model values using canonical strings. */
    public JsonAdaptedBodyBaseline(BodyBaseline source) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.put("date", source.date().toString());
        value.put("weightKg", source.weightKg().toPlainString());
        value.put("heightCm", source.heightCm().toPlainString());

        node = value;
    }

    @JsonValue
    public JsonNode toJson() {
        return node;
    }

    /** Rejects missing, null and wrong-type fields through the normal loading-error path. */
    public BodyBaseline toModelType() throws IllegalValueException {
        if (node == null || !node.isObject()) {
            throw new IllegalValueException("BodyBaseline must be a JSON object.");
        }
        try {
            return new BodyBaseline(BodyMeasurements.parseDate(text(node, "date")),
                    BodyMeasurements.parseDecimal(text(node, "weightKg"), false),
                    BodyMeasurements.parseDecimal(text(node, "heightCm"), true));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }

    private static String text(JsonNode object, String field) throws IllegalValueException {
        JsonNode value = object.get(field);
        if (value == null || !value.isTextual()) {
            throw new IllegalValueException("BodyBaseline " + field + " must be a JSON string.");
        }
        return value.textValue();
    }
}
