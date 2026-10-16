package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
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
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedMembershipPlan jsonAdaptedPlan : membershipPlans) {
            MembershipPlan plan = jsonAdaptedPlan.toModelType();
            if (addressBook.hasMembershipPlan(plan)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PLAN);
            }
            addressBook.addMembershipPlan(plan);
        }
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            if (person.getMembershipAssignment().isPresent()
                    && addressBook.findMembershipPlanByKey(
                            person.getMembershipAssignment().get().getPlanKey()).isEmpty()) {
                throw new IllegalValueException(MESSAGE_UNKNOWN_PLAN_REFERENCE);
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

}
