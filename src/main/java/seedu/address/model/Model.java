package seedu.address.model;

import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Removes every person from the address book, leaving the membership plan catalog untouched.
     */
    void clearPersons();

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /** Returns the predicate currently filtering the person list, so that it can be restored later. */
    Predicate<Person> getFilteredPersonListPredicate();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Returns true if a plan with the same normalized name as {@code plan} exists in the address book.
     */
    boolean hasMembershipPlan(MembershipPlan plan);

    /**
     * Adds the given plan.
     * {@code plan} must not already exist in the address book.
     */
    void addMembershipPlan(MembershipPlan plan);

    /**
     * Removes the given plan.
     * {@code plan} must exist in the address book and must not be referenced
     * by any client assignment.
     */
    void removeMembershipPlan(MembershipPlan plan);

    /**
     * Returns true if any client assignment references the given plan.
     */
    boolean isMembershipPlanInUse(MembershipPlan plan);

    /**
     * Returns true if any stored plan is referenced by a client assignment.
     */
    boolean isAnyMembershipPlanInUse();

    /**
     * Removes every membership plan from the catalog, leaving the person list untouched.
     */
    void clearMembershipPlans();

    /**
     * Returns the plan with the same normalized name as {@code name}, if present.
     */
    Optional<MembershipPlan> findMembershipPlan(MembershipName name);

    /**
     * Returns an unmodifiable view of the membership plan list in insertion order.
     * The returned list is a snapshot; it does not reflect later catalog changes.
     */
    ObservableList<MembershipPlan> getMembershipPlanList();
}
