---
layout: page
title: User Guide
---

AddressBook Level 3 (AB3) is a **desktop application for managing contacts, optimized for use through a Command Line Interface (CLI)** while retaining the benefits of a Graphical User Interface (GUI). If you type quickly, AB3 can help you manage contacts faster than traditional GUI applications.

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Quick start

1. Ensure that Java `25` or later is installed on your computer.<br>
   **Mac users:** Ensure you have the precise JDK version prescribed [here](https://se-education.org/guides/tutorials/javaInstallationMac.html).

1. Download the latest `.jar` file from [here](https://github.com/se-edu/addressbook-level3/releases).

1. Copy the file to the folder you want to use as the _home folder_ for your AddressBook.

1. Open a terminal, `cd` to the folder containing the JAR file, and run `java -jar addressbook.jar`.<br>
   A GUI similar to the one below should appear in a few seconds. Note how the app contains some sample data.<br>
   ![Ui](images/Ui.png)

1. Type a command in the command box and press Enter to execute it. For example, type **`help`** and press Enter to open the help window.<br>
   Some example commands you can try:

   * `list` : Lists all contacts.

   * `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01` : Adds a contact named `John Doe` to the Address Book.

   * `delete 3` : Deletes the 3rd contact shown in the current list.

   * `clear` : Deletes all contacts.

   * `exit` : Exits the app.

1. Refer to the [Features](#features) section below for details of each command.

--------------------------------------------------------------------------------------------------------------------

## Features

<div markdown="block" class="alert alert-info">

**:information_source: Notes about the command format:**<br>

* Words in `UPPER_CASE` are the parameters to be supplied by the user.<br>
  For example, in `add n/NAME`, replace `NAME` with a value such as `John Doe`.

* Items in square brackets are optional.<br>
  For example, `n/NAME [t/TAG]` can be used as `n/John Doe t/friend` or as `n/John Doe`.

* Items followed by `…`​ can appear zero or more times.<br>
  For example, `[t/TAG]…​` may be omitted, or written as `t/friend` or `t/friend t/family`.

* Prefixed parameters can be in any order.<br>
  For example, if the command specifies `n/NAME p/PHONE_NUMBER`, `p/PHONE_NUMBER n/NAME` is also acceptable. Positional parameters, such as the indices and day in `dayex PERSON_INDEX EXERCISE_INDEX DAY`, must follow the order shown.

* Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored.<br>
  For example, `help 123` is interpreted as `help`.

* If you are using a PDF version of this document, be careful when copying and pasting commands that span multiple lines as space characters surrounding line-breaks may be omitted when copied over to the application.
</div>

### Viewing help: `help`

Shows a message explaining how to access the help page.

![help message](images/helpMessage.png)

Format: `help`


### Adding a person: `add`

Adds a person to the address book.

Format: `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​`

<div markdown="span" class="alert alert-primary">:bulb: **Tip:**
A person can have any number of tags, including zero.
</div>

Examples:
* `add n/John Doe p/98765432 e/johnd@example.com a/John street, block 123, #01-01`
* `add n/Betsy Crowe t/friend e/betsycrowe@example.com a/Newgate Prison p/1234567 t/criminal`

### Listing all persons: `list`

Shows a list of all persons in the address book.

Format: `list`

### Editing a person: `edit`

Edits an existing person in the address book.

Format: `edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`

* Edits the person at the specified `INDEX`. The index refers to the index number shown in the displayed person list. The index **must be a positive integer** 1, 2, 3, …​
* At least one of the optional fields must be provided.
* Existing values will be updated to the input values.
* If the values you enter are identical to the client's current details, Fittix tells you that no changes were made.
* When editing tags, all of the person's existing tags are removed; adding tags is not cumulative.
* To remove all of a person's tags, enter `t/` without a tag after it.

Examples:
*  `edit 1 p/91234567 e/johndoe@example.com` Edits the phone number and email address of the 1st person to be `91234567` and `johndoe@example.com` respectively.
*  `edit 2 n/Betsy Crower t/` Edits the name of the 2nd person to be `Betsy Crower` and clears all existing tags.

### Locating persons by name: `find`

Finds persons whose names contain any of the given keywords.

Format: `find KEYWORD [MORE_KEYWORDS]`

* The search is case-insensitive; for example, `hans` matches `Hans`.
* Keyword order does not matter; for example, `Hans Bo` matches `Bo Hans`.
* The search considers only names.
* Partial words are matched; for example, `Han` matches `Hans`.
* Persons matching at least one keyword are returned (an `OR` search); for example, `Hans Bo` returns `Hans Gruber` and `Bo Yang`.

Examples:
* `find John` returns `john` and `John Doe`
* `find alex david` returns `Alex Yeoh`, `David Li`<br>
  ![result for 'find alex david'](images/findAlexDavidResult.png)

### Deleting a person: `delete`

Deletes the specified person from the address book.

Format: `delete INDEX`

* Deletes the person at the specified `INDEX`.
* The index refers to the index number shown in the displayed person list.
* The index **must be a positive integer** 1, 2, 3, …​

Examples:
* `list` followed by `delete 2` deletes the 2nd person in the address book.
* `find Betsy` followed by `delete 1` deletes the 1st person in the results of the `find` command.

### Clearing all entries: `clear`

Clears all entries from the address book.

Format: `clear`

### Adding an exercise to a client's workout plan: `addex`

Adds an exercise to the workout plan of the person identified by the index number used in the last person listing.

Format: `addex INDEX n/NAME [s/SETS] [r/REPS] [w/WEIGHT_KG] [d/DAY]`

`INDEX` and `n/NAME` are required. All other fields are optional:

| Field | Accepted value | Default when omitted |
|-------|----------------|----------------------|
| `n/NAME` | A non-blank exercise name | Required |
| `s/SETS` | A positive integer greater than 0 | `1` |
| `r/REPS` | A positive integer greater than 0 | `1` |
| `w/WEIGHT_KG` | A finite, non-negative number in kg; decimals are allowed | `0` |
| `d/DAY` | A weekday name or accepted prefix; see [Weekday values](#weekday-values) | Grouped under **Day not specified** |

* `INDEX` refers to the index number shown in the displayed person list. It **must be a positive integer** 1, 2, 3, …​
* Each prefix, including `n/`, may appear at most once.
* To use a default, omit the whole field. An empty field such as `s/` or `d/` is invalid.
* The same exercise name can appear on different days in a client's plan, for example Monday and Friday. Names are compared without regard to letter case: adding the same name twice on one day shows `This exercise already exists on this day in the workout plan.` and leaves the plan unchanged, even if sets, reps, or weight differ. Exercises under **Day not specified** share one group, so the same name can appear there only once.
* Each client can have at most **7 exercises per weekday**. Adding an eighth exercise to the same day shows `A client cannot have more than 7 exercises on the same day.` and leaves the plan unchanged. Other weekdays and clients have separate limits. Exercises under **Day not specified** are not capped.
* If `INDEX` is out of range, Fittix shows `The person index provided is invalid.`
* Missing required fields, invalid values, or repeated prefixes cause `Invalid command format!` followed by the `addex` usage:

  ```text
  addex: Adds an exercise to the workout plan of the person identified by the index number used in the last person listing.
  Parameters: INDEX (must be a positive integer) n/EXERCISE_NAME [s/SETS] [r/REPS] [w/WEIGHT_KG] [d/DAY]
  Example: addex 1 n/Bench Press s/3 r/10 w/60 d/M
  ```

* On success, Fittix shows `Added exercise to Person: ` followed by the updated person's details and workout plan, grouped by day.

Examples:

* `addex 1 n/Stretch` adds Stretch with 1 set, 1 rep, 0kg, and no assigned day.
* `addex 1 n/Shoulder Press r/8 w/25.5` adds Shoulder Press with 1 set of 8 reps at 25.5kg and no assigned day.
* `addex 1 n/Bench Press s/3 r/10 w/60 d/w` adds Bench Press with 3 sets of 10 reps at 60kg for Wednesday.
* `addex 1 n/Bench Press s/3 r/10 w/60 d/f` also adds Bench Press for Friday, even if it is already assigned to Wednesday.
* `addex 2 n/Squat s/4 d/s` adds Squat with 4 sets of 1 rep at 0kg for Saturday to the 2nd person's plan.

#### Reading the workout plan

Each person card shows `Workout Plan (N):`, where `N` is the total number of exercises. Exercises appear under full weekday headings in Monday-to-Sunday order. All exercises without an assigned day appear together under **Day not specified**, after the weekday groups. Only groups containing exercises are shown, with space between groups.

Each exercise row uses `NUMBER. NAME SETSxREPS @WEIGHTkg`. Numbering runs continuously from `1` across the entire plan; it does not restart for each day. For example:

```text
Workout Plan (4):
Monday
1. Squat 4x8 @80kg

Wednesday
2. Bench Press 3x10 @60kg

Day not specified
3. Stretch 1x1 @0kg
4. Shoulder Press 1x8 @25.5kg
```

Adding an exercise, changing its day, or deleting an exercise can change the numbers shown. Always use the **current displayed exercise number** for `dayex` and `delex`. Exercises already in the same day group keep their relative order when the plan is sorted.

An empty plan shows `No workout plan yet` on the person card. Command feedback also shows the plan in weekday order with the same exercise numbers.

### Removing an exercise from a client's workout plan: `delex`

Removes the exercise at the specified exercise index from the workout plan of the person identified by the index number used in the last person listing.

Format: `delex PERSON_INDEX EXERCISE_INDEX`

* Both `PERSON_INDEX` and `EXERCISE_INDEX` **must be positive integers** 1, 2, 3, …​
* `PERSON_INDEX` refers to the person in the current displayed person list.
* `EXERCISE_INDEX` matches the current numbering across all day groups on that person's card. For example, `delex 1 2` removes Bench Press if the 1st person's plan matches the example above.
* The remaining exercises are renumbered after deletion. A day heading disappears when its last exercise is removed.
* If `PERSON_INDEX` is out of range, Fittix shows `The person index provided is invalid.`
* If `EXERCISE_INDEX` is out of range (including when the plan is empty), Fittix shows `The exercise index provided is invalid.`
* If the format is wrong, Fittix shows `Invalid command format!` followed by the `delex` usage:

  ```text
  delex: Removes the exercise at the specified exercise index from the workout plan of the person identified by the index number used in the last person listing.
  Parameters: PERSON_INDEX EXERCISE_INDEX (both must be positive integers)
  Example: delex 1 2
  ```

* On success, Fittix shows `Removed exercise from Person: ` followed by the updated person including the remaining workout plan.

Examples:

* `delex 1 2` removes the currently numbered 2nd exercise from the 1st person's workout plan.
* `list` followed by `delex 1 1` removes the currently numbered 1st exercise from the 1st person's workout plan.

### Assigning a day tag to an exercise: `dayex`

Assigns or changes the weekday of an existing exercise in a person's workout plan.

Format: `dayex PERSON_INDEX EXERCISE_INDEX DAY`

* Both indices **must be positive integers**. `PERSON_INDEX` refers to the current displayed person list; `EXERCISE_INDEX` matches the current workout plan numbering on that person's card.
* Supply all three arguments in the order shown. Enter the day directly, for example `dayex 1 2 Tuesday`.
* `DAY` accepts the weekday names and prefixes listed below, without regard to letter case.
* Each exercise has one assigned day. Assigning a new day replaces the previous day while keeping the exercise name, sets, reps, and weight.
* Moving an exercise to a day that already contains the same name (case-insensitive) shows `This exercise already exists on this day in the workout plan.` and leaves the plan unchanged. Assigning an exercise to its current day is allowed.
* A day can contain at most **7 exercises for each client**. Moving an exercise into a day that already has 7 shows `A client cannot have more than 7 exercises on the same day.` and leaves the plan unchanged. Assigning an exercise to its current day when that day has 7 exercises is allowed, as is moving an exercise away from a full day.
* The plan is immediately sorted by weekday and renumbered. Check the updated card before entering another command that uses an exercise index.
* If `PERSON_INDEX` is out of range, Fittix shows `The person index provided is invalid.`
* If `EXERCISE_INDEX` is out of range (including when the plan is empty), Fittix shows `The exercise index provided is invalid.`
* Missing arguments, non-positive or non-integer indices, or an invalid day cause `Invalid command format!` followed by the `dayex` usage:

  ```text
  dayex: Assigns a day tag to an exercise in a person's workout plan.
  Parameters: PERSON_INDEX EXERCISE_INDEX DAY
  Example: dayex 1 2 Tuesday
  ```

* On success, Fittix shows `Assigned day to exercise: ` followed by the updated person's details and workout plan.

Examples:

* `dayex 1 2 Tuesday` assigns Tuesday to the currently numbered 2nd exercise for the 1st person.
* `dayex 1 3 su` assigns Sunday to the currently numbered 3rd exercise for the 1st person.

#### Weekday values

Both `addex` with `d/DAY` and `dayex` accept a full weekday name or a prefix of that name, ignoring letter case. The shortest accepted prefixes are:

| Weekday heading | Shortest prefix | Other examples |
|-----------------|-----------------|----------------|
| Monday | `m` | `Mon`, `MONDAY` |
| Tuesday | `tu` | `Tue`, `Tues`, `Tuesday` |
| Wednesday | `w` | `Wed`, `Wednesday` |
| Thursday | `th` | `Thu`, `Thurs`, `Thursday` |
| Friday | `f` | `Fri`, `Friday` |
| Saturday | `s` | `Sa`, `Sat`, `Saturday` |
| Sunday | `su` | `Sun`, `Sunday` |

`t` is invalid; use `tu` for Tuesday or `th` for Thursday. `s` means Saturday; Sunday requires at least `su`. The card always displays the full weekday name, regardless of the prefix you enter.

To add an exercise without an assigned day, omit `d/` from `addex`. The heading `Day not specified` is not a valid `DAY` argument.

### Clearing a client's workout plan: `clearplan`

Clears all exercises from the workout plan of the person identified by the index number used in the last person listing.

Format: `clearplan INDEX`

* `INDEX` refers to the index number shown in the displayed person list. It **must be a positive integer** 1, 2, 3, …​
* If `INDEX` is out of range, Fittix shows `The person index provided is invalid.`
* If the format is wrong, Fittix shows `Invalid command format!` followed by the `clearplan` usage (`MESSAGE_USAGE`):
  ```
  clearplan: Clears all exercises from the workout plan of the person identified by the index number used in the last person listing.
  Parameters: INDEX (must be a positive integer)
  Example: clearplan 1
  ```
* If the plan already has no exercises, Fittix shows `Workout plan is already empty for Person: ` followed by that person, and makes no change.
* Otherwise Fittix clears the plan and shows `Cleared workout plan for Person: ` followed by the updated person (whose workout plan is `Empty`).

Example:
* `clearplan 1` clears the workout plan of the 1st person.

### Exiting the program: `exit`

Exits the program.

Format: `exit`

### Saving the data

AddressBook automatically saves data after every command. You do not need to save manually.

Workout exercises, their sets, reps, weight, and assigned weekdays are restored when you reopen the app. Exercises saved without a day, including older entries labelled `day not specified` or `Day not specified`, appear together under the single **Day not specified** heading.

### Editing the data file

AddressBook data is saved automatically as a JSON file `[JAR file location]/data/addressbook.json`. Advanced users are welcome to update data directly by editing that data file.

<div markdown="span" class="alert alert-warning">:exclamation: **Caution:**
If your changes make the data file invalid, AddressBook starts with an empty address book at the next run. The invalid file remains on disk until you run a command (AddressBook saves after every command). Still, we recommend backing up the file before editing it.<br>
Furthermore, certain edits can cause the AddressBook to behave in unexpected ways (e.g., if a value entered is outside of the acceptable range). Therefore, edit the data file only if you are confident that you can update it correctly.
</div>

### Archiving data files `[coming in v2.0]`

_Details coming soon ..._

--------------------------------------------------------------------------------------------------------------------

## FAQ

**Q**: How do I transfer my data to another computer?<br>
**A**: Install the app on the other computer and overwrite the data file it creates with the data file from your previous AddressBook home folder.

--------------------------------------------------------------------------------------------------------------------

## Known issues

1. **When using multiple screens**, if you move the application to a secondary screen, and later switch to using only the primary screen, the GUI will open off-screen. The remedy is to delete the `preferences.json` file created by the application before running the application again.
2. **If you minimize the Help Window** and then run the `help` command (or use the `Help` menu, or the keyboard shortcut `F1`) again, the original Help Window will remain minimized, and no new Help Window will appear. The remedy is to manually restore the minimized Help Window.

--------------------------------------------------------------------------------------------------------------------

## Command summary

Action | Format, Examples
--------|------------------
**Add** | `add n/NAME p/PHONE_NUMBER e/EMAIL a/ADDRESS [t/TAG]…​` <br> e.g., `add n/James Ho p/22224444 e/jamesho@example.com a/123, Clementi Rd, 1234665 t/friend t/colleague`
**Add exercise** | `addex INDEX n/NAME [s/SETS] [r/REPS] [w/WEIGHT_KG] [d/DAY]`<br> e.g., `addex 1 n/Stretch` or `addex 1 n/Bench Press s/3 r/10 w/60 d/w`
**Clear** | `clear`
**Clear workout plan** | `clearplan INDEX`<br> e.g., `clearplan 1`
**Delete** | `delete INDEX`<br> e.g., `delete 3`
**Delete exercise** | `delex PERSON_INDEX EXERCISE_INDEX`<br> e.g., `delex 1 2`
**Assign/change exercise day** | `dayex PERSON_INDEX EXERCISE_INDEX DAY`<br> e.g., `dayex 1 2 Tuesday`
**Edit** | `edit INDEX [n/NAME] [p/PHONE_NUMBER] [e/EMAIL] [a/ADDRESS] [t/TAG]…​`<br> e.g., `edit 2 n/James Lee e/jameslee@example.com`
**Find** | `find KEYWORD [MORE_KEYWORDS]`<br> e.g., `find James Jake`
**List** | `list`
**Help** | `help`
