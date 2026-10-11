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
    public static final Prefix PREFIX_REMARK = new Prefix("r/");
    public static final Prefix PREFIX_SETS = new Prefix("s/");
    /* Reps share the "r/" marker per addex syntax; never mixed with PREFIX_REMARK in one parser. */
    public static final Prefix PREFIX_REPS = new Prefix("r/");
    public static final Prefix PREFIX_WEIGHT = new Prefix("w/");

}
