package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 * Membership plan names are unique after case-insensitive normalization.
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final Map<String, MembershipPlan> membershipPlans = new LinkedHashMap<>();

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        persons.setPersonsAllowingLegacyVariants(newData.getPersonList());
        setMembershipPlans(newData.getMembershipPlanList());
    }

    //// person-level operations

    /**
     * Removes every person from the address book, leaving the membership plan catalog untouched.
     */
    public void clearPersons() {
        persons.setPersons(new ArrayList<>());
    }

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Preserves a case or whitespace name variant written by an older release during data loading.
     * New clients must be added through {@link #addPerson(Person)}.
     */
    public void addLegacyPerson(Person p) {
        persons.addLegacyPerson(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// membership plan-level operations

    /**
     * Returns true if a plan with the same normalized name as {@code plan} exists in the address book.
     */
    public boolean hasMembershipPlan(MembershipPlan plan) {
        requireNonNull(plan);
        return membershipPlans.containsKey(plan.getKey());
    }

    /**
     * Returns the plan with the same normalized name as {@code name}, if present.
     */
    public Optional<MembershipPlan> findMembershipPlan(MembershipName name) {
        requireNonNull(name);
        return findMembershipPlanByKey(name.getKey());
    }

    /**
     * Returns the plan stored under the given canonical {@code planKey}, if present.
     */
    public Optional<MembershipPlan> findMembershipPlanByKey(String planKey) {
        requireNonNull(planKey);
        return Optional.ofNullable(membershipPlans.get(planKey));
    }

    /**
     * Adds a membership plan to the address book in insertion order.
     * No plan with the same normalized name must already exist in the address book.
     */
    public void addMembershipPlan(MembershipPlan plan) {
        requireNonNull(plan);
        checkArgument(!hasMembershipPlan(plan), "Duplicate membership plan.");
        membershipPlans.put(plan.getKey(), plan);
    }

    /**
     * Removes the plan with the same normalized name as {@code plan} from the address book.
     * The plan must exist and must not be referenced by any client assignment.
     */
    public void removeMembershipPlan(MembershipPlan plan) {
        requireNonNull(plan);
        checkArgument(hasMembershipPlan(plan), "Membership plan does not exist.");
        checkArgument(!isMembershipPlanInUse(plan), "Membership plan is in use.");
        membershipPlans.remove(plan.getKey());
    }

    /**
     * Removes every membership plan from the catalog, leaving the person list untouched.
     */
    public void clearMembershipPlans() {
        membershipPlans.clear();
    }

    /**
     * Returns true if any stored plan is referenced by a client assignment.
     */
    public boolean isAnyMembershipPlanInUse() {
        for (MembershipPlan plan : membershipPlans.values()) {
            if (isMembershipPlanInUse(plan)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if any client assignment references the same normalized plan name as {@code plan}.
     */
    public boolean isMembershipPlanInUse(MembershipPlan plan) {
        requireNonNull(plan);
        String planKey = plan.getKey();
        for (Person person : persons) {
            if (person.getMembershipAssignment().isPresent()
                    && person.getMembershipAssignment().get().getPlanKey().equals(planKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces the contents of the plan list with {@code plans}, preserving their order.
     * {@code plans} must not contain duplicate normalized names.
     */
    public void setMembershipPlans(List<MembershipPlan> plans) {
        requireNonNull(plans);
        Map<String, MembershipPlan> copy = new LinkedHashMap<>();
        for (MembershipPlan plan : plans) {
            requireNonNull(plan);
            checkArgument(!copy.containsKey(plan.getKey()), "List contains duplicate membership plan.");
            copy.put(plan.getKey(), plan);
        }
        membershipPlans.clear();
        membershipPlans.putAll(copy);
    }

    //// util methods

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("persons", persons)
                .add("membershipPlans", getMembershipPlanList())
                .toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<MembershipPlan> getMembershipPlanList() {
        return FXCollections.unmodifiableObservableList(
                FXCollections.observableArrayList(membershipPlans.values()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        return persons.equals(otherAddressBook.persons)
                && new ArrayList<>(membershipPlans.values())
                        .equals(new ArrayList<>(otherAddressBook.membershipPlans.values()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(persons, new ArrayList<>(membershipPlans.values()));
    }
}
