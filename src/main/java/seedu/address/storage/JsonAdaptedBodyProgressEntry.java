package seedu.address.storage;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.BodyMeasurements;
import seedu.address.model.person.BodyProgressEntry;

/** Strict JSON adapter for {@link BodyProgressEntry}; scalar coercion is deliberately rejected. */
public final class JsonAdaptedBodyProgressEntry {
    private final JsonNode node;

    /** Retains raw JSON for strict validation during model conversion. */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public JsonAdaptedBodyProgressEntry(JsonNode node) {
        this.node = node;
    }

    /** Serializes validated model values using canonical strings. */
    public JsonAdaptedBodyProgressEntry(BodyProgressEntry source) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        value.put("date", source.date().toString());
        value.put("weightKg", source.weightKg().toPlainString());

        source.notes().ifPresent(notes -> value.put("notes", notes));
        node = value;
    }

    @JsonValue
    public JsonNode toJson() {
        return node;
    }

    /** Rejects missing, null and wrong-type fields through the normal loading-error path. */
    public BodyProgressEntry toModelType() throws IllegalValueException {
        if (node == null || !node.isObject()) {
            throw new IllegalValueException("BodyProgressEntry must be a JSON object.");
        }
        try {
            return new BodyProgressEntry(BodyMeasurements.parseDate(text(node, "date")),
                    BodyMeasurements.parseDecimal(text(node, "weightKg"), false),
                    node.get("notes") == null || node.get("notes").isNull()
                            ? Optional.empty() : Optional.of(text(node, "notes")));
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }

    private static String text(JsonNode object, String field) throws IllegalValueException {
        JsonNode value = object.get(field);
        if (value == null || !value.isTextual()) {
            throw new IllegalValueException("BodyProgressEntry " + field + " must be a JSON string.");
        }
        return value.textValue();
    }
}
