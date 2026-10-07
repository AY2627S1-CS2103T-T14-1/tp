package seedu.address.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

public class SampleDataUtilTest {

    @Test
    public void getSamplePersons_nonEmptyAndHaveWorkoutPlan() {
        Person[] samplePersons = SampleDataUtil.getSamplePersons();
        assertNotNull(samplePersons);
        assertTrue(samplePersons.length > 0);
        for (Person person : samplePersons) {
            assertNotNull(person.getWorkoutPlan());
            assertTrue(person.getWorkoutPlan().isEmpty());
        }
    }

    @Test
    public void getSampleAddressBook_matchesSamplePersons() {
        ReadOnlyAddressBook sampleAb = SampleDataUtil.getSampleAddressBook();
        assertEquals(SampleDataUtil.getSamplePersons().length, sampleAb.getPersonList().size());
    }

    @Test
    public void getTagSet() {
        assertEquals(2, SampleDataUtil.getTagSet("friends", "colleagues").size());
        assertEquals(0, SampleDataUtil.getTagSet().size());
    }
}
