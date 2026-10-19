package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /**
     * Returns an unmodifiable view of the membership plan list in insertion order.
     * Plan names are unique after case-insensitive normalization.
     */
    ObservableList<MembershipPlan> getMembershipPlanList();

}
