package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Exercise;
import seedu.address.model.person.Goal;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Name.isValidName(trimmedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(trimmedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String address} into an {@code Address}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code address} is invalid.
     */
    public static Address parseAddress(String address) throws ParseException {
        requireNonNull(address);
        String trimmedAddress = address.trim();
        if (!Address.isValidAddress(trimmedAddress)) {
            throw new ParseException(Address.MESSAGE_CONSTRAINTS);
        }
        return new Address(trimmedAddress);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /** Parses a {@code String goal} into a {@code Goal}. */
    public static Goal parseGoal(String goal) throws ParseException {
        requireNonNull(goal);
        if (!Goal.isValidGoal(goal)) {
            throw new ParseException(Goal.MESSAGE_CONSTRAINTS);
        }
        return new Goal(goal);
    }

    /**
     * Parses a {@code String tag} into a {@code Tag}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code tag} is invalid.
     */
    public static Tag parseTag(String tag) throws ParseException {
        requireNonNull(tag);
        String trimmedTag = tag.trim();
        if (!Tag.isValidTagName(trimmedTag)) {
            throw new ParseException(Tag.MESSAGE_CONSTRAINTS);
        }
        return new Tag(trimmedTag);
    }

    /**
     * Parses {@code Collection<String> tags} into a {@code Set<Tag>}.
     */
    public static Set<Tag> parseTags(Collection<String> tags) throws ParseException {
        requireNonNull(tags);
        final Set<Tag> tagSet = new HashSet<>();
        for (String tagName : tags) {
            tagSet.add(parseTag(tagName));
        }
        return tagSet;
    }

    /**
     * Parses a {@code String name} into a validated exercise name.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static String parseExerciseName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!Exercise.isValidName(trimmedName)) {
            throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_NAME);
        }
        return trimmedName;
    }

    /**
     * Parses a {@code String sets} into a validated sets count.
     *
     * @throws ParseException if the given {@code sets} is invalid.
     */
    public static int parseExerciseSets(String sets) throws ParseException {
        requireNonNull(sets);
        String trimmedSets = sets.trim();
        try {
            int value = Integer.parseInt(trimmedSets);
            if (!Exercise.isValidSets(value)) {
                throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_SETS);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_SETS);
        }
    }

    /**
     * Parses a {@code String reps} into a validated reps count.
     *
     * @throws ParseException if the given {@code reps} is invalid.
     */
    public static int parseExerciseReps(String reps) throws ParseException {
        requireNonNull(reps);
        String trimmedReps = reps.trim();
        try {
            int value = Integer.parseInt(trimmedReps);
            if (!Exercise.isValidReps(value)) {
                throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_REPS);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_REPS);
        }
    }

    /**
     * Parses a {@code String weight} into a validated weight in kg.
     *
     * @throws ParseException if the given {@code weight} is invalid.
     */
    public static double parseExerciseWeight(String weight) throws ParseException {
        requireNonNull(weight);
        String trimmedWeight = weight.trim();
        try {
            double value = Double.parseDouble(trimmedWeight);
            if (!Exercise.isValidWeight(value)) {
                throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_WEIGHT);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ParseException(Exercise.MESSAGE_CONSTRAINTS_WEIGHT);
        }
    }

    /**
     * Parses a {@code String name} into a {@code MembershipName}.
     * Leading and trailing whitespaces are trimmed and internal whitespace is collapsed.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static MembershipName parseMembershipName(String name) throws ParseException {
        requireNonNull(name);
        String trimmedName = name.trim();
        if (!MembershipName.isValidMembershipName(trimmedName)) {
            throw new ParseException(MembershipName.MESSAGE_CONSTRAINTS);
        }
        return new MembershipName(trimmedName);
    }

    /**
     * Parses a {@code String price} into a validated membership plan price.
     * The price must be a positive amount with at most two decimal places,
     * written without a currency symbol or thousands separators.
     *
     * @throws ParseException if the given {@code price} is invalid.
     */
    public static BigDecimal parseMembershipPrice(String price) throws ParseException {
        requireNonNull(price);
        String trimmedPrice = price.trim();
        if (!trimmedPrice.matches("\\d+(\\.\\d{1,2})?")) {
            throw new ParseException(MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        }
        BigDecimal value = new BigDecimal(trimmedPrice);
        if (!MembershipPlan.isValidPrice(value)) {
            throw new ParseException(MembershipPlan.MESSAGE_PRICE_CONSTRAINTS);
        }
        return value;
    }

    /**
     * Parses a {@code String sessions} into a validated membership plan starting session count.
     *
     * @throws ParseException if the given {@code sessions} is not a whole number from 1 to 1000.
     */
    public static int parseMembershipSessions(String sessions) throws ParseException {
        requireNonNull(sessions);
        String trimmedSessions = sessions.trim();
        try {
            int value = Integer.parseInt(trimmedSessions);
            if (!MembershipPlan.isValidStartingSessions(value)) {
                throw new ParseException(MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ParseException(MembershipPlan.MESSAGE_SESSIONS_CONSTRAINTS);
        }
    }

    /**
     * Parses a {@code String expiry} into a validated membership assignment expiry date.
     * The date must use the {@code YYYY-MM-DD} format and lie after the current local date.
     *
     * @throws ParseException if the given {@code expiry} is not a valid future date.
     */
    public static LocalDate parseMembershipExpiry(String expiry) throws ParseException {
        return parseMembershipExpiry(expiry, Clock.systemDefaultZone());
    }

    /**
     * Parses a {@code String expiry} into a validated membership assignment expiry date,
     * comparing against the local date of the given {@code clock}.
     *
     * @throws ParseException if the given {@code expiry} is not a valid future date.
     */
    public static LocalDate parseMembershipExpiry(String expiry, Clock clock) throws ParseException {
        requireNonNull(expiry);
        requireNonNull(clock);
        String trimmedExpiry = expiry.trim();
        if (!trimmedExpiry.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ParseException(MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        }
        try {
            LocalDate date = LocalDate.parse(trimmedExpiry);
            if (!date.isAfter(LocalDate.now(clock))) {
                throw new ParseException(MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
            }
            return date;
        } catch (DateTimeParseException e) {
            throw new ParseException(MembershipAssignment.MESSAGE_EXPIRY_CONSTRAINTS);
        }
    }
}
