package seedu.address.storage;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.membership.MembershipAssignment;
import seedu.address.model.membership.MembershipName;
import seedu.address.model.membership.MembershipPlan;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_PLAN = "Membership plans list contains duplicate plan(s).";
    public static final String MESSAGE_UNKNOWN_PLAN_REFERENCE =
            "Membership assignment references an unknown membership plan.";
    public static final String MESSAGE_INCONSISTENT_PLAN_NAME =
            "Membership assignment's plan name does not match the referenced membership plan.";
    public static final String MESSAGE_NULL_PLAN = "Membership plans list contains a null entry.";
    public static final String MESSAGE_NULL_PERSON = "Persons list contains a null entry.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedMembershipPlan> membershipPlans = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons and membership plans.
     * Older files may omit {@code membershipPlans}, which loads as an empty catalog.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("membershipPlans") List<JsonAdaptedMembershipPlan> membershipPlans) {
        this.persons.addAll(persons);
        if (membershipPlans != null) {
            this.membershipPlans.addAll(membershipPlans);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        membershipPlans.addAll(source.getMembershipPlanList().stream()
                .map(JsonAdaptedMembershipPlan::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        return toModelType(Clock.systemDefaultZone());
    }

    /** Loads all client body trackers against a single injectable local clock. */
    public AddressBook toModelType(Clock clock) throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedMembershipPlan jsonAdaptedPlan : membershipPlans) {
            if (jsonAdaptedPlan == null) {
                throw new IllegalValueException(MESSAGE_NULL_PLAN);
            }
            MembershipPlan plan = jsonAdaptedPlan.toModelType();
            if (addressBook.hasMembershipPlan(plan)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PLAN);
            }
            addressBook.addMembershipPlan(plan);
        }
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            if (jsonAdaptedPerson == null) {
                throw new IllegalValueException(MESSAGE_NULL_PERSON);
            }
            Person person = jsonAdaptedPerson.toModelType(clock);
            validateMembershipAssignment(addressBook, person);
            if (addressBook.hasPerson(person) && !isLegacyNameVariant(addressBook, person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (addressBook.hasPerson(person)) {
                addressBook.addLegacyPerson(person);
            } else {
                addressBook.addPerson(person);
            }
        }
        return addressBook;
    }

    /** Returns whether the matching identity differs only in a legacy unnormalized name representation. */
    private boolean isLegacyNameVariant(AddressBook addressBook, Person person) {
        return addressBook.getPersonList().stream()
                .anyMatch(existing -> existing.isSamePerson(person) && !existing.getName().equals(person.getName()));
    }

    /**
     * Checks that a client's stored assignment references an existing plan and that its copied
     * display name is consistent with that plan. Rejecting inconsistent saved data keeps the
     * displayed plan name and the plan used for deletion protection in agreement.
     *
     * @throws IllegalValueException if the assignment references no plan or names a different plan.
     */
    private void validateMembershipAssignment(AddressBook addressBook, Person person)
            throws IllegalValueException {
        if (person.getMembershipAssignment().isEmpty()) {
            return;
        }
        MembershipAssignment assignment = person.getMembershipAssignment().get();
        Optional<MembershipPlan> referencedPlan =
                addressBook.findMembershipPlanByKey(assignment.getPlanKey());
        if (referencedPlan.isEmpty()) {
            throw new IllegalValueException(MESSAGE_UNKNOWN_PLAN_REFERENCE);
        }
        if (!referencedPlan.get().getName().getKey()
                .equals(new MembershipName(assignment.getPlanName()).getKey())) {
            throw new IllegalValueException(MESSAGE_INCONSISTENT_PLAN_NAME);
        }
    }

}
