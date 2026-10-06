package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.node.TextNode;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.person.DietPlan;

public class JsonAdaptedDietPlanTest {
    @Test
    public void jsonRoundTrip_preservesTargetAndOptionalNotes() throws Exception {
        DietPlan[] plans = {new DietPlan("1800 kcal/day"), new DietPlan("1800 kcal", "Lean protein")};
        for (DietPlan plan : plans) {
            String json = JsonUtil.toJsonString(new JsonAdaptedDietPlan(plan));
            assertEquals(plan, JsonUtil.fromJsonString(json, JsonAdaptedDietPlan.class).toModelType());
        }
    }

    @Test
    public void toModelType_missingOrNullTarget_throwsIllegalValueException() throws Exception {
        for (String json : new String[] {"{}", "{\"target\":null}"}) {
            JsonAdaptedDietPlan adapted = JsonUtil.fromJsonString(json, JsonAdaptedDietPlan.class);
            assertThrows(IllegalValueException.class, JsonAdaptedDietPlan.MESSAGE_MISSING_TARGET, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_missingOrNullNotes_returnsTargetOnlyPlan() throws Exception {
        for (String json : new String[] {"{\"target\":\"1800 kcal\"}",
            "{\"target\":\"1800 kcal\",\"notes\":null}"}) {
            assertEquals(new DietPlan("1800 kcal"),
                    JsonUtil.fromJsonString(json, JsonAdaptedDietPlan.class).toModelType());
        }
    }

    @Test
    public void toModelType_invalidTarget_throwsIllegalValueException() {
        for (String target : new String[] {"", " ", "!!!", "a".repeat(201), "1800 kcal\n"}) {
            JsonAdaptedDietPlan adapted = new JsonAdaptedDietPlan(TextNode.valueOf(target), null);
            assertThrows(IllegalValueException.class, DietPlan.MESSAGE_CONSTRAINTS_TARGET, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_invalidNotes_throwsIllegalValueException() {
        for (String notes : new String[] {"", " ", "a".repeat(301), "Lean\nprotein"}) {
            JsonAdaptedDietPlan adapted = new JsonAdaptedDietPlan(
                    TextNode.valueOf("1800 kcal"), TextNode.valueOf(notes));
            assertThrows(IllegalValueException.class, DietPlan.MESSAGE_CONSTRAINTS_NOTES, adapted::toModelType);
        }
    }

    @Test
    public void toModelType_nonTextFields_throwsIllegalValueException() throws Exception {
        for (String value : new String[] {"1800", "true", "[]", "{}"}) {
            JsonAdaptedDietPlan target = JsonUtil.fromJsonString("{\"target\":" + value + "}",
                    JsonAdaptedDietPlan.class);
            assertThrows(IllegalValueException.class, DietPlan.MESSAGE_CONSTRAINTS_TARGET, target::toModelType);
            JsonAdaptedDietPlan notes = JsonUtil.fromJsonString("{\"target\":\"1800 kcal\",\"notes\":" + value + "}",
                    JsonAdaptedDietPlan.class);
            assertThrows(IllegalValueException.class, DietPlan.MESSAGE_CONSTRAINTS_NOTES, notes::toModelType);
        }
    }
}
