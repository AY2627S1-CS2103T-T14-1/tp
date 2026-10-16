package seedu.address.logic.parser;

/**
 * Contains Command Line Interface (CLI) syntax definitions common to multiple commands
 */
public class CliSyntax {

    /* Prefix definitions */
    public static final Prefix PREFIX_NAME = new Prefix("n/");
    public static final Prefix PREFIX_PHONE = new Prefix("p/");
    public static final Prefix PREFIX_EMAIL = new Prefix("e/");
    public static final Prefix PREFIX_ADDRESS = new Prefix("a/");
    public static final Prefix PREFIX_TAG = new Prefix("t/");
    public static final Prefix PREFIX_GOAL = new Prefix("g/");
    public static final Prefix PREFIX_REMARK = new Prefix("r/");
    public static final Prefix PREFIX_DATE = new Prefix("d/");
    public static final Prefix PREFIX_TIME = new Prefix("tm/");
    public static final Prefix PREFIX_LOCATION = new Prefix("l/");
    public static final Prefix PREFIX_SETS = new Prefix("s/");
    /* Reps share the "r/" marker per addex syntax; never mixed with PREFIX_REMARK in one parser. */
    public static final Prefix PREFIX_REPS = new Prefix("r/");
    public static final Prefix PREFIX_WEIGHT = new Prefix("w/");
    public static final Prefix PREFIX_DAY = new Prefix("day/");
    public static final Prefix PREFIX_EXERCISE_DAY = new Prefix("d/");
    public static final Prefix PREFIX_LOG_DATE = new Prefix("d/");
    public static final Prefix PREFIX_LOG_TIME = new Prefix("t/");
    public static final Prefix PREFIX_LOG_EXERCISE = new Prefix("ex/");
    public static final Prefix PREFIX_MEMBERSHIP_NAME = new Prefix("n/");
    public static final Prefix PREFIX_MEMBERSHIP_PRICE = new Prefix("p/");
    public static final Prefix PREFIX_MEMBERSHIP_SESSIONS = new Prefix("s/");
    public static final Prefix PREFIX_MEMBERSHIP_EXPIRY = new Prefix("e/");

}
