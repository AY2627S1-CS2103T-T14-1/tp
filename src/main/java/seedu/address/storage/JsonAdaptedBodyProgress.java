package seedu.address.storage;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.BodyBaseline;
import seedu.address.model.person.BodyProgress;
import seedu.address.model.person.BodyProgressEntry;

/** Strict, backward-compatible JSON adapter for a client's complete body tracker. */
public final class JsonAdaptedBodyProgress {
    private final JsonNode node;

    /** Retains raw JSON for strict validation during model conversion. */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public JsonAdaptedBodyProgress(JsonNode node) {
        this.node = node;
    }

    /** Serializes validated model values using canonical strings. */
    public JsonAdaptedBodyProgress(BodyProgress source) {
        ObjectNode value = JsonNodeFactory.instance.objectNode();
        source.getBaseline().ifPresent(baseline ->
                value.set("baseline", new JsonAdaptedBodyBaseline(baseline).toJson()));
        ArrayNode entries = value.putArray("entries");
        source.getEntries().forEach(entry -> entries.add(new JsonAdaptedBodyProgressEntry(entry).toJson()));
        node = value;
    }

    @JsonValue
    public JsonNode toJson() {
        return node;
    }

    public BodyProgress toModelType() throws IllegalValueException {
        return toModelType(Clock.systemDefaultZone());
    }

    /** Validates nested types, all invariants and future dates using the loading clock. */
    public BodyProgress toModelType(Clock clock) throws IllegalValueException {
        if (node == null || node.isNull()) {
            return new BodyProgress();
        }
        if (!node.isObject()) {
            throw new IllegalValueException("Body progress must be a JSON object.");
        }
        JsonNode baselineNode = node.get("baseline");
        Optional<BodyBaseline> baseline = baselineNode == null || baselineNode.isNull()
                ? Optional.empty() : Optional.of(new JsonAdaptedBodyBaseline(baselineNode).toModelType());
        JsonNode entriesNode = node.get("entries");
        List<BodyProgressEntry> entries = new ArrayList<>();
        if (entriesNode != null && !entriesNode.isNull()) {
            if (!entriesNode.isArray()) {
                throw new IllegalValueException("Body progress entries must be a JSON array.");
            }
            for (JsonNode entry : entriesNode) {
                entries.add(new JsonAdaptedBodyProgressEntry(entry).toModelType());
            }
        }
        try {
            BodyProgress progress = new BodyProgress(baseline, entries);
            progress.validateDates(clock);
            return progress;
        } catch (IllegalArgumentException exception) {
            throw new IllegalValueException(exception.getMessage());
        }
    }
}
