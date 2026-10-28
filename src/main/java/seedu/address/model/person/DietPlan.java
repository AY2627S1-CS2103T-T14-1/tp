package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

import seedu.address.commons.util.ToStringBuilder;

/**
 * An immutable diet plan with a required nutrition target and optional coaching notes.
 * Text is trimmed while internal spacing and display case are preserved.
 */
public final class DietPlan {
    public static final String MESSAGE_CONSTRAINTS_TARGET =
            "Diet target must contain 1 to 200 printable characters.";
    public static final String MESSAGE_CONSTRAINTS_NOTES =
            "Diet-plan notes must contain 1 to 300 printable characters.";

    private static final Pattern OUTER_WHITESPACE =
            Pattern.compile("^[\\p{javaWhitespace}\\p{Zs}]+|[\\p{javaWhitespace}\\p{Zs}]+$");

    private final String target;
    private final Optional<String> notes;
    private final Optional<Integer> calories;
    private final Optional<Integer> proteinGrams;
    private final Optional<Integer> carbsGrams;
    private final Optional<Integer> fatGrams;

    /**
     * Creates a plan without notes.
     */
    public DietPlan(String target) {
        this(target, Optional.empty());
    }

    /**
     * Creates a plan with supplied, nonblank notes.
     */
    public DietPlan(String target, String notes) {
        this(target, Optional.of(notes));
    }

    private DietPlan(String target, Optional<String> notes) {
        this(target, notes, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    private DietPlan(String target, Optional<String> notes, Optional<Integer> calories,
            Optional<Integer> proteinGrams, Optional<Integer> carbsGrams, Optional<Integer> fatGrams) {
        requireNonNull(target);
        checkArgument(isValidTarget(target), MESSAGE_CONSTRAINTS_TARGET);
        notes.ifPresent(value -> checkArgument(isValidNotes(value), MESSAGE_CONSTRAINTS_NOTES));
        calories.ifPresent(value -> checkArgument(value > 0, "Calories must be positive."));
        proteinGrams.ifPresent(value -> checkArgument(value >= 0, "Protein grams cannot be negative."));
        carbsGrams.ifPresent(value -> checkArgument(value >= 0, "Carb grams cannot be negative."));
        fatGrams.ifPresent(value -> checkArgument(value >= 0, "Fat grams cannot be negative."));
        this.target = trimText(target);
        this.notes = notes.map(DietPlan::trimText);
        this.calories = calories;
        this.proteinGrams = proteinGrams;
        this.carbsGrams = carbsGrams;
        this.fatGrams = fatGrams;
    }

    /** Creates a structured calorie target while retaining a readable target string. */
    public static DietPlan structured(int calories, Optional<Integer> proteinGrams,
            Optional<Integer> carbsGrams, Optional<Integer> fatGrams, Optional<String> notes) {
        return new DietPlan(calories + " kcal per day", notes, Optional.of(calories),
                proteinGrams, carbsGrams, fatGrams);
    }

    /**
     * Returns whether the target is printable, has 1-200 characters after trimming,
     * and contains at least one letter or digit.
     */
    public static boolean isValidTarget(String target) {
        requireNonNull(target);
        return isValidText(target, 200) && target.codePoints().anyMatch(Character::isLetterOrDigit);
    }

    /**
     * Returns whether supplied notes contain 1-300 printable characters after trimming.
     */
    public static boolean isValidNotes(String notes) {
        requireNonNull(notes);
        return isValidText(notes, 300);
    }

    private static boolean isValidText(String text, int maximumLength) {
        String trimmed = trimText(text);
        int length = trimmed.codePointCount(0, trimmed.length());
        return length >= 1 && length <= maximumLength && text.codePoints().allMatch(DietPlan::isPrintable);
    }

    private static String trimText(String text) {
        return OUTER_WHITESPACE.matcher(text).replaceAll("");
    }

    private static boolean isPrintable(int codePoint) {
        int type = Character.getType(codePoint);
        return !Character.isISOControl(codePoint)
                && type != Character.FORMAT
                && type != Character.LINE_SEPARATOR
                && type != Character.PARAGRAPH_SEPARATOR
                && type != Character.SURROGATE
                && type != Character.PRIVATE_USE
                && type != Character.UNASSIGNED;
    }

    public String getTarget() {
        return target;
    }

    public Optional<String> getNotes() {
        return notes;
    }

    public Optional<Integer> getCalories() {
        return calories;
    }

    public Optional<Integer> getProteinGrams() {
        return proteinGrams;
    }

    public Optional<Integer> getCarbsGrams() {
        return carbsGrams;
    }

    public Optional<Integer> getFatGrams() {
        return fatGrams;
    }

    /**
     * Returns whether targets and optional notes match ignoring case, for duplicate-plan detection.
     * Value equality remains case-sensitive to preserve the stored display text.
     */
    public boolean isSameDietPlan(DietPlan other) {
        return other != null && target.equalsIgnoreCase(other.target)
                && calories.equals(other.calories)
                && proteinGrams.equals(other.proteinGrams)
                && carbsGrams.equals(other.carbsGrams)
                && fatGrams.equals(other.fatGrams)
                && notes.map(value -> other.notes.map(value::equalsIgnoreCase).orElse(false))
                        .orElse(other.notes.isEmpty());
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        return other instanceof DietPlan otherPlan
                && target.equals(otherPlan.target) && notes.equals(otherPlan.notes)
                && calories.equals(otherPlan.calories) && proteinGrams.equals(otherPlan.proteinGrams)
                && carbsGrams.equals(otherPlan.carbsGrams) && fatGrams.equals(otherPlan.fatGrams);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target, notes, calories, proteinGrams, carbsGrams, fatGrams);
    }

    @Override
    public String toString() {
        ToStringBuilder result = new ToStringBuilder(this).add("target", target).add("notes", notes);
        if (calories.isPresent()) {
            result.add("calories", calories).add("proteinGrams", proteinGrams)
                    .add("carbsGrams", carbsGrams).add("fatGrams", fatGrams);
        }
        return result.toString();
    }
}
