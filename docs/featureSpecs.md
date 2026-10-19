# Fittix Feature Specification

**Team** - CS2103T-T14-01

## Features covered

- Feature 1A: Add client
- Feature 1B: Delete client
- Feature 2: Training schedule
- Feature 3: Workout plan management
- Feature 4: Workout session logging
- Feature 5: Diet plan management
- Feature 7: Membership Plan
- Feature 8: Health considerations
- Feature 9: Dietary restrictions
- Feature 10: Body progress tracker
- Feature 11: Advanced scheduling and client monitoring

Feature identifiers follow the section headings, rather than list positions. Feature 11's specification is included below.

## Feature 1A: Add client

### 1. Feature

Add a new client.

### 2. Purpose

Creates the name and phone record required before storing a client's schedule, plans, and workout logs.

### 3. Command format

```text
add n/NAME p/PHONE e/EMAIL a/ADDRESS [g/GOAL] [t/TAG]...
```

Name, phone, email, and address are required; goal and tags are optional. Any parameter order is accepted; repeat `t/` for tags. Retaining these AB3 fields supports richer records without slowing basic registration. The goal is a single current fitness objective; tags do not provide fitness-goal filtering.

### 4. Example commands

```text
add n/Priya Nair p/91234567 e/priya@example.com a/123 Main Street

add n/Mdm Lim p/+65 9876 5432 e/lim@example.com a/10 Sample Road g/Improve mobility t/morning t/regular
```

### 5. Parameters

#### Name parameter

| Aspect         | Rule, message and rationale                                                                                                                                                                                          |
| -------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Accepted value | 1 to 70 characters after normalization. Unicode letters, spaces, apostrophes, hyphens, and full stops are accepted. At least one letter is required. Supports real names; excludes delimiters and unrelated symbols. |
| Whitespace     | Leading and trailing spaces are removed. Consecutive internal whitespace becomes one space. Ensures consistent comparison and display.                                                                               |
| Letter case    | The original case is displayed. Duplicate comparison ignores cases. Preserves display case; prevents case-only duplicates.                                                                                           |
| Error          | Names must be 1 to 70 characters long and may contain only letters, spaces, apostrophes, hyphens, and full stops.                                                                                                    |

**Accepted examples**

```text
Priya Nair   Anne-Marie Tan   D'Angelo Lim   Dr. Ahmad Rahman
```

**Rejected examples**

```text
12345   John @ Tan
```

#### Phone parameter

| Aspect         | Rule, message and rationale                                                                                                                 |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| Accepted value | An optional leading plus sign followed by 7 to 15 digits. Spaces and hyphens may separate digits. Supports local and international numbers. |
| Normalization  | Spaces and hyphens are removed. A leading plus sign is retained. Values such as 8123 4567 and 8123-4567 compare consistently.               |
| Rejected value | Parentheses, letters, decimal points, multiple plus signs, and other symbols are rejected. Keeps phone values comparable.                   |
| Error          | Phone numbers must contain 7 to 15 digits and may begin with one plus sign. Spaces and hyphens may be used as separators.                   |

| Aspect        | Rule, message and rationale               |
| ------------- | ----------------------------------------- |
| 9123 4567     | 91234567 Spaces removed                   |
| 8123-4567     | 81234567 Hyphen removed                   |
| +65 9876 5432 | +6598765432 International prefix retained |

#### Contact and optional profile parameters

| Parameter    | Rule, rationale and error |
| ------------ | ------------------------- |
| Email `e/`   | Trim; maximum 254 ASCII characters; one @; no whitespace. Nonempty local part: letters/digits/.\_+-; no edge or consecutive dots. Domain: 2+ dot-separated labels, each 1–63 letters/digits/hyphens with alphanumeric ends. Preserve local case; lowercase domain. Common addresses supported; delivery unverified.<br><br>Error: Email must be a valid address such as lim@example.com (maximum 254 characters). |
| Address `a/` | Trim; 1–200 printable characters, no line breaks. Preserve internal spacing and case. Free text supports varied postal addresses; no address verification.<br><br>Error: Address must contain 1 to 200 printable characters without line breaks. |
| Fitness goal `g/` | Trim; 1–100 printable characters with at least one letter or digit. Preserve internal spacing and case. Each client has at most one current goal; `edit INDEX g/GOAL` replaces it and `edit INDEX g/` clears it. `find g/KEYWORDS` searches current goals case-insensitively.<br><br>Error: Goal must contain 1 to 100 characters, including at least one letter or digit. |
| Tags `t/`    | Trim; 1–30 ASCII letters/digits per tag. Store lowercase; collapse repeated tags after normalization, retaining first-entry order. At most 10 distinct tags. Compact labels avoid spelling/case duplicates.<br><br>Error: Tags must contain 1 to 30 letters or digits; at most 10 distinct tags are allowed. |

An omitted goal remains absent and omitted tags form an empty set. Any invalid supplied field rejects the entire command; no partial client is saved. Prefix tokens after whitespace are reserved and cannot occur literally inside values.

### 6. Output

#### Successful command

Create, save, display, and select the client. Show normalized name/phone, required email and address, and supplied goal and tags; hide an omitted goal and tags. The success message remains name and phone only.

```text
Added client: Priya Nair; Phone: 91234567.
```

#### Failed Command

**Missing name:** Name is required. Use: `add n/NAME p/PHONE e/EMAIL a/ADDRESS [g/GOAL] [t/TAG]...`

**Missing phone number:** Phone number is required. Use: `add n/NAME p/PHONE e/EMAIL a/ADDRESS [g/GOAL] [t/TAG]...`

**Both values are missing:** Name and phone number are required. Use: `add n/NAME p/PHONE e/EMAIL a/ADDRESS [g/GOAL] [t/TAG]...`

**Repeated parameter:** The `n/`, `p/`, `e/`, `a/`, and `g/` prefixes may each be used only once. Repeat `t/` for multiple tags.

**Unknown parameter or extra text:** Invalid command format. Use: `add n/NAME p/PHONE e/EMAIL a/ADDRESS [g/GOAL] [t/TAG]...`

A failed command does not create a partial client record.

### 7. Duplicate handling

Duplicates require both normalized name (case-insensitive) and phone (spaces/hyphens removed) to match. Different email, address, goal, or tags do not bypass this rule; add never updates or merges an existing client.

```text
This client already exists: Priya Nair; Phone: +6591234567.
```

A shared name or phone alone is allowed: different people may share names or family contact numbers.

### 8. Possible errors

**Data cannot be saved:** The client could not be added because Fittix could not save the updated data. No changes were made.

**Maximum client limit:** The client could not be added because the maximum number of clients has been reached.

If the implementation imposes a maximum, the same value must appear in the User Guide.

### 9. Relevant UI mockup

```text
Clients          | Client details
1. Daniel Tan    | Priya Nair — 91234567
2. Mdm Lim       | Optional goal/tags: hidden when absent
3. Priya Nair [NEW] |
Result: Added client: Priya Nair; Phone: 91234567.
```

### 10. Priority distinctions

#### Must have

Add, validate, normalize, deduplicate, display, and select a client using required name/phone/email/address and optional goal/tags. All six fields are supported in the MVP.

#### Nice to have or later

Bulk import, health considerations, custom identifiers, and tag-based filtering.

## Feature 1B: Delete client

### 1. Feature

Delete a former client.

### 2. Purpose

Removes a former client and their schedule, plans, and workout logs, preventing orphaned records.

### 3. Command format

```text
delete INDEX
```

### 4. Example commands

```text
delete 1
```

If Priya Nair is shown at index 3, `delete 3` removes Priya Nair.

### 5. Parameters

| Aspect              | Rule, message and rationale                                                                                             |
| ------------------- | ----------------------------------------------------------------------------------------------------------------------- |
| Accepted index      | A positive whole number identifying a client in the displayed list. Fast input consistent with AB3.                     |
| Leading zeros       | Leading zeros are accepted. For example, 03 means 3. The numeric meaning is unambiguous.                                |
| Rejected index      | Zero, negative values, decimals, signs, and nonnumeric text are rejected. The command must identify one visible record. |
| Invalid value error | The client index must be a positive whole number shown in the client list.                                              |
| Out of range error  | No client exists at index {INDEX}.                                                                                      |

### 6. Output

#### Successful command

Delete the client and owned data, save, and renumber. Select the next client, or previous if last; clear details when empty.

```text
Deleted client: Priya Nair; Phone: 91234567.
```

**Missing index:** Client index is required. Use: `delete INDEX`

**Extra parameter or text:** Invalid command format. Use: `delete INDEX`

Any failure leaves the client and all associated information unchanged.

### 7. Duplicate handling

The index identifies one record, even when names or phones match. The result shows the deleted name and phone for verification.

### 8. Possible errors

**Data cannot be saved:** The client could not be deleted because Fittix could not save the updated data. No changes were made.

#### Accidental deletion

Undo and confirmation are outside the MVP. The User Guide must state that deletion is permanent.

### 9. Relevant UI mockup

```text
Before: 1. Daniel Tan; 2. Mdm Lim; 3. Priya Nair
Command: delete 3
After: 1. Daniel Tan; 2. Mdm Lim [SELECTED]
Details: Mdm Lim — 98765432
Result: Deleted client: Priya Nair; Phone: 91234567.
```

### 10. Priority distinctions

#### Must have

Delete by valid displayed index, remove owned data, and identify the deleted client.

#### Nice to have or later

Confirmation, undo, archiving, and bulk deletion.

## Feature 2: Training schedule

### 1. Feature

Record and view a client's dated training sessions.

### 2. Purpose

Records future appointments with a date, time, and location, so trainers know when and where to meet clients. Saved appointments remain visible after their date has passed.

### 3. Command format

```text
schedule INDEX d/YYYY-MM-DD tm/HH:mm l/LOCATION
viewschedule INDEX
```

Each `schedule` command appends one session to the client at the displayed index. `viewschedule` lists all saved sessions for that client in date/time order.

### 4. Example commands

```text
schedule 2 d/2099-03-15 tm/14:30 l/Main gym
schedule 2 d/2099-03-16 tm/09:00 l/Studio
viewschedule 2
```

### 5. Parameters

#### Client index

INDEX follows the shared client index rules.

#### Date and time parameters

| Aspect | Rule, message and rationale |
|---|---|
| Date | `d/` takes a real calendar date in `YYYY-MM-DD` format. |
| Time | `tm/` takes one 24-hour time in `HH:mm` format. There is no end time or duration. |
| Future requirement | The combined date and time must be later than the current local date and time. Otherwise: `Training sessions must be in the future.` |
| Invalid value | An invalid or missing date/time produces `Use a valid date (YYYY-MM-DD), time (HH:mm) and non-empty location.` |

#### Location parameter

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | `l/` is required and must contain non-blank text. |
| Invalid value | A missing or blank location produces `Use a valid date (YYYY-MM-DD), time (HH:mm) and non-empty location.` |

### 6. Output

#### Add a session

The session is appended to the client's record. The command reports the scheduled date, time, and location.

```text
Scheduled session for Priya Nair: 2099-03-15 14:30 at Main gym
```

#### View saved sessions

`viewschedule` lists sessions chronologically with one-based numbers. Past sessions remain in the list.

```text
Training sessions for Priya Nair:
1. 2099-03-15 14:30 at Main gym
2. 2099-03-16 09:00 at Studio
```

If no sessions have been saved, the result is `No training sessions saved for Priya Nair.` The index in either command must refer to a client in the current displayed list.

### 7. Duplicate and conflict handling

`schedule` rejects a new appointment if any client already has a training session or meeting at the same date and start time, including an identical session for the same client. The message names the client holding that time slot, and no data changes. Appointments have no stored duration, so different start times are allowed even if the appointments might overlap in practice. Editing or deleting an appointment is not supported by these commands.

### 8. Possible errors

**Invalid index:** The command reports an invalid displayed client index. **Repeated parameter:** Each of `d/`, `tm/`, and `l/` may appear only once. **Invalid date, time, or location:** The command reports the expected formats and non-empty location requirement. **Past date/time:** The command reports that training sessions must be in the future.

### 9. Relevant UI mockup

```text
Command: schedule 2 d/2099-03-15 tm/14:30 l/Main gym
Result: Scheduled session for Priya Nair: 2099-03-15 14:30 at Main gym

Command: viewschedule 2
Result: Training sessions for Priya Nair:
1. 2099-03-15 14:30 at Main gym
```

### 10. Priority distinctions

#### Must have

Record multiple dated sessions per client and view them in date/time order. Preserve them in saved data and when editing other client details.

#### Nice to have or later

Duration-based conflict detection, calendar views, reminders, recurring sessions, and edit/delete commands. The daily appointment view is provided by `dayschedule` (Feature 11).

## Feature 3: Workout plan management

### 1. Feature

Add, remove, re-assign day, and clear exercises in a client's workout plan (`addex` / `delex` / `dayex` / `clearplan`).

### 2. Purpose

Stores a per-client list of structured exercises with sets, reps, weight, and an optional weekday tag, so trainers can build, adjust, and clear plans incrementally.

### 3. Command formats

```text
addex PERSON_INDEX n/NAME [s/SETS] [r/REPS] [w/WEIGHT_KG] [d/DAY]

delex PERSON_INDEX EXERCISE_INDEX

dayex PERSON_INDEX EXERCISE_INDEX DAY

clearplan PERSON_INDEX
```

`PERSON_INDEX` refers to the index number shown in the displayed person list. It must be a positive integer 1, 2, 3, …. `EXERCISE_INDEX` matches the current numbering across all day groups on that person's card. `DAY` in `dayex` is entered directly without a prefix (e.g. `dayex 1 2 Tuesday`).

### 4. Example commands

```text
addex 1 n/Stretch                  # Stretch, 1x1 @0kg, no day
addex 1 n/Shoulder Press r/8 w/25.5  # Shoulder Press, 1x8 @25.5kg, no day
addex 1 n/Bench Press s/3 r/10 w/60 d/w  # Bench Press, 3x10 @60kg, Wednesday
addex 1 n/Bench Press s/3 r/10 w/60 d/f  # Bench Press, 3x10 @60kg, Friday (same name on different day allowed)
addex 2 n/Squat s/4 d/s            # Squat, 4x1 @0kg, Saturday, for 2nd person
delex 1 2                          # removes 2nd exercise from 1st person
dayex 1 2 Tuesday                  # assigns Tuesday to 2nd exercise of 1st person
dayex 1 3 su                       # assigns Sunday to 3rd exercise of 1st person
clearplan 1                        # clears 1st person's plan
```

### 5. Parameters

#### Client index

PERSON_INDEX follows the shared client index rules: a positive integer identifying a client in the displayed list.

#### Exercise name (`addex n/`)

| Aspect          | Rule, message and rationale                                                                                                  |
| --------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| Accepted value  | A non-blank exercise name. Required. Supports common exercise names.                                                         |
| Repeated prefix | Each prefix, including `n/`, may appear at most once.                                                                        |
| Empty value     | An empty field such as `n/` is invalid. To use a default, omit the whole optional field.                                     |
| Error           | Missing required fields, invalid values, or repeated prefixes cause `Invalid command format!` followed by the `addex` usage. |

#### Sets, reps, weight

| Parameter   | Rule, rationale and error                                                                                                                                                     |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Sets `s/`   | Optional. A positive integer greater than 0. Default `1` when omitted. An empty `s/` is invalid. Supports set counts.                                                         |
| Reps `r/`   | Optional. A positive integer greater than 0. Default `1` when omitted. An empty `r/` is invalid. Supports strength and conditioning.                                          |
| Weight `w/` | Optional. A finite, non-negative number in kg; decimals are allowed. Default `0` when omitted. Zero means bodyweight or unweighted. An empty `w/` is invalid. No upper bound. |

To use a default, omit the whole field. An empty field such as `s/` or `d/` is invalid. Only `n/`, `s/`, `r/`, `w/`, and `d/` are accepted prefixes; an unknown `x/`-style prefix causes `Invalid command format!`.

#### Day tag (`addex d/` and `dayex DAY`)

| Aspect            | Rule, message and rationale                                                                                                                                                                                                                                                                                                                                                  |
| ----------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Accepted value    | Optional in `addex`; required as positional `DAY` in `dayex`. A full weekday name or accepted prefix, ignoring letter case. Omitted `d/` groups the exercise under **Day not specified**. Each exercise has one assigned day; assigning a new day replaces the previous day while keeping name, sets, reps, and weight. Assigning an exercise to its current day is allowed. |
| Shortest prefixes | Monday `m`; Tuesday `tu`; Wednesday `w`; Thursday `th`; Friday `f`; Saturday `s`; Sunday `su`. Other examples: `Mon`, `MONDAY`, `Tue`, `Tues`, `Tuesday`, `Wed`, `Wednesday`, `Thu`, `Thurs`, `Thursday`, `Fri`, `Friday`, `Sa`, `Sat`, `Saturday`, `Sun`, `Sunday`.                                                                                                         |
| Invalid day       | `t` is invalid; use `tu` for Tuesday or `th` for Thursday. `s` means Saturday; Sunday requires at least `su`. Invalid `d/DAY` such as `d/t` or `d/funday` causes `Invalid command format!` followed by usage. The heading `Day not specified` is not a valid `DAY` argument.                                                                                                 |
| Display           | The card always displays the full weekday name, regardless of the prefix entered.                                                                                                                                                                                                                                                                                            |
| Repeated prefix   | The `d/` prefix may appear at most once in `addex`. `dayex` takes exactly three arguments in order: `PERSON_INDEX EXERCISE_INDEX DAY`.                                                                                                                                                                                                                                       |

#### Exercise index (`delex` / `dayex`)

| Aspect                | Rule, message and rationale                                                                                                                                                                                                                                                                |
| --------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Accepted value        | A positive integer 1, 2, 3, … matching the current numbering across all day groups on that person's card. The plan is immediately sorted by weekday and renumbered after `addex`, `dayex`, or `delex`. Check the updated card before entering another command that uses an exercise index. |
| Ordering effect       | Adding an exercise, changing its day, or deleting an exercise can change the numbers shown. Always use the current displayed exercise number for `dayex` and `delex`. Exercises already in the same day group keep their relative order when the plan is sorted.                           |
| Person out of range   | `The person index provided is invalid.`                                                                                                                                                                                                                                                    |
| Exercise out of range | `The exercise index provided is invalid.` This includes when the plan is empty.                                                                                                                                                                                                            |
| Remaining exercises   | Remaining exercises are renumbered after deletion. A day heading disappears when its last exercise is removed.                                                                                                                                                                             |

#### Daily capacity limit (`addex` / `dayex`)

| Aspect         | Rule, message and rationale                                                                                                                                                                                                       |
| -------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Limit          | Each client can have at most 7 exercises per weekday. Other weekdays and other clients have separate limits. Exercises under **Day not specified** are not capped.                                                                |
| `addex`        | Adding an 8th exercise to the same weekday is rejected with `A client cannot have more than 7 exercises on the same day.` The plan is left unchanged.                                                                             |
| `dayex`        | Moving an exercise into a weekday that already has 7 exercises is rejected with the same message. Assigning an exercise to its current day when that day already has 7 is allowed, as is moving an exercise away from a full day. |
| Day resolution | Weekday prefixes and letter case are resolved through the same weekday table above.                                                                                                                                               |

### 6. Output

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

An empty plan shows `No workout plan yet` on the person card. Command feedback also shows the plan in weekday order with the same exercise numbers.

Workout exercises, their sets, reps, weight, and assigned weekdays are restored when you reopen the app. Exercises saved without a day, including older entries labelled `day not specified` or `Day not specified`, appear together under the single **Day not specified** heading.

#### `addex` success

On success, Fittix shows `Added exercise to Person: ` followed by the updated person's details and workout plan, grouped by day.

**Missing required field:** Missing `INDEX` or `n/NAME`, invalid values (including an invalid `d/DAY` such as `d/t` or `d/funday`), or repeated/unknown prefixes cause `Invalid command format!` followed by the `addex` usage:

```text
addex: Adds an exercise to the workout plan of the person identified by the index number used in the last person listing.
Parameters: INDEX (must be a positive integer) n/EXERCISE_NAME [s/SETS] [r/REPS] [w/WEIGHT_KG] [d/DAY]
Example: addex 1 n/Bench Press s/3 r/10 w/60 d/M
```

If `INDEX` is out of range, Fittix shows `The person index provided is invalid.`

A failed command does not add an exercise.

#### `delex` success

On success, Fittix shows `Removed exercise from Person: ` followed by the updated person including the remaining workout plan.

**Format error:** If the format is wrong, Fittix shows `Invalid command format!` followed by the `delex` usage:

```text
delex: Removes the exercise at the specified exercise index from the workout plan of the person identified by the index number used in the last person listing.
Parameters: PERSON_INDEX EXERCISE_INDEX (both must be positive integers)
Example: delex 1 2
```

A failed command leaves the workout plan unchanged.

#### `dayex` success

On success, Fittix shows `Assigned day to exercise: ` followed by the updated person's details and workout plan. Supply all three arguments in the order shown.

**Format error:** Missing or extra arguments, non-positive or non-integer indices, or an invalid day cause `Invalid command format!` followed by the `dayex` usage:

```text
dayex: Assigns a day tag to an exercise in a person's workout plan.
Parameters: PERSON_INDEX EXERCISE_INDEX DAY
Example: dayex 1 2 Tuesday
```

A failed command leaves the workout plan unchanged.

#### `clearplan` success and empty case

If the plan already has no exercises, Fittix shows `Workout plan is already empty for Person: ` followed by that person, and makes no change. Otherwise Fittix clears the plan and shows `Cleared workout plan for Person: ` followed by the updated person (whose workout plan is `Empty`).

**Format error:** If the format is wrong, Fittix shows `Invalid command format!` followed by the `clearplan` usage:

```text
clearplan: Clears all exercises from the workout plan of the person identified by the index number used in the last person listing.
Parameters: INDEX (must be a positive integer)
Example: clearplan 1
```

### 7. Duplicate handling

An exercise with the same name (case-insensitive) cannot be added twice on the **same day** for the same person, even if sets, reps, or weight differ.

```text
This exercise already exists on this day in the workout plan.
```

Fittix does not add the duplicate. The same exercise name on **different** weekdays is allowed (e.g. Bench Press on both Monday and Friday). Exercises under **Day not specified** share one group, so the same name can appear there only once. The same exercise name for different clients is allowed.

Moving an exercise with `dayex` to a day that already contains the same name (case-insensitive) shows the same message and leaves the plan unchanged.

### 8. Possible errors

**Person index out of range:** `The person index provided is invalid.`

**Exercise index out of range:** `The exercise index provided is invalid.`

**Duplicate on same day:** `This exercise already exists on this day in the workout plan.`

**Daily limit exceeded:** `A client cannot have more than 7 exercises on the same day.`

**Invalid command format:** `Invalid command format!` followed by the relevant `addex` / `delex` / `dayex` / `clearplan` usage shown above.

**Data cannot be saved:** The workout plan could not be saved because Fittix could not save the updated data. No changes were made.

Extraneous parameters for commands that take no parameters, such as `help`, `list`, `exit`, and `clear`, are ignored. `addex`, `delex`, `dayex`, and `clearplan` do not ignore extraneous parameters; extra arguments cause `Invalid command format!`.

### 9. Relevant UI mockup

```text
Clients          | Alex Yeoh — WORKOUT PLAN
1. Alex Yeoh [SELECTED] | Workout Plan (4):
                 | Monday
                 | 1. Squat 4x8 @80kg
                 |
                 | Wednesday
                 | 2. Bench Press 3x10 @60kg
                 |
                 | Day not specified
                 | 3. Stretch 1x1 @0kg
                 | 4. Shoulder Press 1x8 @25.5kg
Result: Added exercise to Person: Alex Yeoh ...
```

### 10. Priority distinctions

#### Must have

Add (`addex`), remove (`delex`), re-assign day (`dayex`), clear (`clearplan`), and display exercises with name, sets, reps, weight, and weekday grouping; validate indices, fields, and weekday values; enforce case-insensitive name uniqueness **per weekday** (same name allowed on different days); enforce at most 7 exercises per weekday per client (`Day not specified` uncapped); sort Monday-to-Sunday with `Day not specified` last; use continuous exercise numbering.

#### Nice to have or later

Structured templates, version history, shared plans, plan comparisons, and exercise editing beyond day reassignment.

## Feature 4: Workout session logging

### 1. Feature

Log a completed workout session with exercises, weights, sets, and repetitions.

### 2. Purpose

Records completed exercises for reliable recall and later history and progression features.

### 3. Command format

```text
log INDEX d/DATE [t/TIME] ex/EXERCISE|WEIGHT|SETS|REPS [ex/EXERCISE|WEIGHT|SETS|REPS]...
```

Date and at least one exercise are mandatory. Time is optional. Up to 30 exercise entries may be supplied and weight is measured in kilograms.

### 4. Example commands

```text
log 2 d/2026-09-16 ex/Back squat|60|3|10

log 2 d/2026-09-16 t/18:00 ex/Back squat|60|3|10 ex/Bench press|40|3|8 ex/Lat pulldown|35|3|12

log 3 d/2026-09-16 ex/Push-up|0|3|15
```

### 5. Parameters

#### Client index

INDEX follows the shared client index rules.

#### Session date

| Aspect                | Rule, message and rationale                                                                          |
| --------------------- | ---------------------------------------------------------------------------------------------------- |
| Accepted value        | A real date in yyyy-MM-dd format that is today or earlier. Unambiguous dates for completed sessions. |
| Format error          | Session date must use yyyy-MM-dd.                                                                    |
| Impossible date error | {DATE} is not a valid calendar date.                                                                 |
| Future date error     | A completed workout session cannot have a future date.                                               |

#### Session time

| Aspect         | Rule, message and rationale                                                                                    |
| -------------- | -------------------------------------------------------------------------------------------------------------- |
| Accepted value | Optional 24 hour HH:mm time with hours 00 to 23 and minutes 00 to 59. Distinguishes sessions on the same date. |
| Error          | Session time must use 24-hour HH:mm format, for example `t/18:00`.                                             |

#### Exercise entry

Every `ex/` value contains exactly four components separated by vertical bars.

| Aspect                | Rule, message and rationale                                                                                                                                                                                                |
| --------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Exercise              | 1 to 50 characters with at least one letter. Letters, digits, spaces, hyphens, apostrophes, and parentheses are accepted. The vertical bar is rejected. This supports common exercise names while reserving the separator. |
| Exercise error        | Exercise names must be 1 to 50 characters long and may contain letters, digits, spaces, hyphens, apostrophes, and parentheses.                                                                                             |
| Weight                | A number from 0 to 1000 with up to two decimal places. The value is kilograms. Zero means bodyweight or unweighted. Supports gym equipment and incremental loads.                                                          |
| Weight error          | Exercise weight must be between 0 and 1000 kg and may have up to 2 decimal places.                                                                                                                                         |
| Sets                  | A whole number from 1 to 100. Requires a set; catches excessive values.                                                                                                                                                    |
| Sets error            | Sets must be a whole number from 1 to 100.                                                                                                                                                                                 |
| Repetitions           | A whole number from 1 to 1000. Supports strength and conditioning.                                                                                                                                                         |
| Repetitions error     | Repetitions must be a whole number from 1 to 1000.                                                                                                                                                                         |
| Entry structure error | Exercise entries must use `ex/EXERCISE\|WEIGHT\|SETS\|REPS`.                                                                                                                                                               |
| Entry count error     | A workout session may contain at most 30 exercise entries.                                                                                                                                                                 |

### 6. Output

#### Successful command

Save and display one completed session, preserving exercise entry order.

```text
Logged workout for Priya Nair on 16 Sep 2026 at 18:00: 3 exercises.

Logged workout for Priya Nair on 16 Sep 2026: 3 exercises.
```

**Missing date:** Session date is required. Use: `log INDEX d/DATE [t/TIME] ex/EXERCISE|WEIGHT|SETS|REPS...`

**Missing exercise:** At least one exercise entry is required. Use `ex/EXERCISE|WEIGHT|SETS|REPS`.

**Repeated date or time:** The `d/` and `t/` prefixes may each be used only once. Repeating `ex/` is valid and required for multiple exercises.

**Unknown parameter or extra text:** Invalid command format. Use: `log INDEX d/DATE [t/TIME] ex/EXERCISE|WEIGHT|SETS|REPS...`

If one exercise is invalid, Fittix records none of the session.

### 7. Duplicate handling

Client, date, and optional time form a unique key. A matching key is rejected even with different exercises; history editing is later scope.

```text
A workout session already exists for Priya Nair on 16 Sep 2026 at 18:00.

An unspecified-time workout session already exists for Priya Nair on 16 Sep 2026. Add t/TIME to distinguish another session.
```

Different times permit multiple daily sessions. Repeated exercise names allow warm-up and working sets at different loads.

### 8. Possible errors

#### Package balance

Logging a workout does not deduct a package session in the MVP. Fittix must not show a misleading balance change.

#### Duration based exercise

The MVP structure requires repetitions. Duration and distance fields are later enhancements, and Fittix must not reinterpret seconds or metres as repetitions.

**Data cannot be saved:** The workout session could not be logged because Fittix could not save the updated data. No changes were made.

### 9. Relevant UI mockup

```text
Clients          | Priya Nair — 16 Sep 2026 18:00
2. Priya Nair [SELECTED] | Exercise      Weight  Sets  Reps
                 | Back squat      60 kg    3    10
                 | Bench press     40 kg    3     8
                 | Lat pulldown    35 kg    3    12
Result: Logged workout for Priya Nair: 3 exercises.
```

### 10. Priority distinctions

#### Must have

Log/display dated sessions with optional times and exercise/weight/sets/reps; reject duplicate client/date/time keys.

#### Nice to have or later

Log edits/deletion, latest/full history, duration/distance/effort/rest/notes, package deductions, and plan comparisons.

## Feature 5: Diet plan management

### 1. Feature

Create or replace a client's current diet plan.

### 2. Purpose

Stores one current nutrition target and optional notes per client. Structured dietary restrictions remain later scope.

### 3. Command format

```text
diet INDEX t/TARGET [n/NOTES]
```

### 4. Example commands

```text
diet 2 t/1800 kcal per day, 120 g protein

diet 2 t/1800 kcal per day, 120 g protein n/Prioritise lean protein and vegetables

diet 3 t/2000 kcal per day n/Halal meals only; avoid shellfish
```

The final example is an MVP workaround. It does not replace the later structured dietary restriction field.

**Parsing rule:** Parameter prefixes such as `t/` and `n/` are recognized only at the beginning of an argument or after whitespace. A slash within a value, such as `kcal/day`, is treated as ordinary text.

### 5. Parameters

#### Client index

INDEX follows the shared client index rules.

#### Diet target

| Aspect         | Rule, message and rationale                                                                                                                                                               |
| -------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Accepted value | 1 to 200 printable characters after trimming, with at least one letter or digit. Internal spaces, punctuation, numbers, units, and case are preserved. Supports varied nutrition targets. |
| Line breaks    | Line breaks are rejected. The current plan must remain readable in the details panel.                                                                                                     |
| Error          | Diet target must contain 1 to 200 printable characters.                                                                                                                                   |

Fittix records the trainer's text. It does not decide whether the target is medically appropriate.

#### Diet notes

| Aspect         | Rule, message and rationale                                                                                                                    |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| Accepted value | Optional. 1 to 300 printable characters after trimming. Internal spacing, punctuation, and case are preserved. Optional coaching instructions. |
| Line breaks    | Line breaks are rejected. The plan remains compact and easy to scan.                                                                           |
| Error          | Diet-plan notes must contain 1 to 300 printable characters.                                                                                    |

### 6. Output

#### Create a plan

Save and display the target and optional notes as the current plan.

```text
Created diet plan for Priya Nair.
```

#### Replace a plan

Replaces a different current plan in full; previous versions are not retained.

```text
Replaced diet plan for Priya Nair.
```

**Missing target:** Diet target is required. Use: `diet INDEX t/TARGET [n/NOTES]`

**Repeated parameter:** The `t/` and `n/` prefixes may each be used only once.

**Unknown parameter or extra text:** Invalid command format. Use: `diet INDEX t/TARGET [n/NOTES]`

Any failure leaves the current plan unchanged.

### 7. Duplicate handling

Reject matching trimmed targets and notes, compared case-insensitively.

```text
This is already the current diet plan for Priya Nair.
```

A different plan replaces the current version.

### 8. Possible errors

**Unsupported restrictions prefix:** Invalid command format. Use: `diet INDEX t/TARGET [n/NOTES]`

A trainer may temporarily place an essential restriction in notes. Structured restriction storage remains later scope.

#### Medical suitability

Fittix must not claim that an entered plan is safe, medically suitable, or nutritionally complete.

**Data cannot be saved:** The diet plan could not be saved. The previous plan remains unchanged.

### 9. Relevant UI mockup

```text
Clients          | Priya Nair — CURRENT DIET PLAN
2. Priya Nair [SELECTED] | Target: 1800 kcal per day, 120 g protein
                 | Notes: Prioritise lean protein
                 | and vegetables
Result: Created diet plan for Priya Nair.
```

### 10. Priority distinctions

#### Must have

Store, replace, and display one required target with optional notes per client.

#### Nice to have or later

Structured restrictions/macros, version history, meal templates, partial edits, and suitability checks.

## Feature 7: Membership Plan

### 1. Feature

Users can create and view membership plans with a unique name, number of training sessions, and price. They can assign one plan at a time to a client, with an optional expiry date for that assignment, or remove the assignment. The plan name, sessions remaining, and any expiry date appear on the client information card. Users can increment or decrement each client's session balance.

### 2. Purpose

Trainers can offer several plans and track each client's remaining sessions independently.

### 3. Command Format

#### Adding a new membership plan

```text
addmem n/NAME p/PRICE s/NUMBER_OF_SESSIONS
```

#### Deleting a plan

```text
delmem n/NAME
```

#### Assign the membership plan to a client

```text
amem MEMBERSHIP_NAME PERSON_INDEX [e/YYYY-MM-DD]
```

#### Remove membership plan from a client

```text
rmem PERSON_INDEX
```

#### Increment the number of sessions left

```text
imem PERSON_INDEX NUMBER_TO_INCREMENT
```

#### Decrement the number of sessions

```text
dmem PERSON_INDEX NUMBER_TO_DECREMENT
```

#### View membership plans

```text
view mem[bership]
```

`view mem` and `view membership` are aliases. They display the same membership plans in the same order and use the same empty-list message.

#### Clearing all membership plans

```text
clearmem
```

`clearmem` deletes every membership plan in the catalog and nothing else. It takes no parameters. It does not delete clients, workout plans, schedules, diet plans, workout logs, or any other data. If any membership plan is assigned to a client, no plans are deleted and Fittix shows `Membership plans are in use and cannot be cleared. Remove all client assignments with rmem first.` Remove every assignment with `rmem` first, or use `delmem` to delete individual unreferenced plans. The general client command `clear` removes clients only and does **not** delete membership plans; use `clearmem` when the catalog must be emptied.

### 4. Example Commands

```text
addmem n/Gold p/150 s/30

delmem n/Silver

amem Gold 1 e/2026-12-30

rmem 1

imem 1 3

dmem 1 2

view mem

view membership

clearmem
```

Command words and the `mem` or `membership` argument of `view` are case-sensitive. Plan names and parameter prefixes (`n/`, `p/`, `s/`, `e/`) are case-insensitive; for example, `addmem N/Gold P/150 S/30` is valid.

### 5. Parameters

#### Membership plan name

| Aspect              | Rule, message and rationale                                                                                                                                                                                                    |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Accepted value      | 1 to 50 characters after trimming. Letters, digits, spaces, apostrophes, hyphens, and full stops are accepted; at least one letter or digit is required. This keeps plan names readable in the client card.                    |
| Whitespace and case | Remove leading/trailing spaces and collapse consecutive internal whitespace. The stored name preserves the entered case; the catalog and client-card views render it with the first character upper-cased, so a plan stored as `gold` reads as `Gold`. Compare names case-insensitively. The name passed to `amem` and `delmem` follows the same comparison rule. |
| Error               | Membership plan name must contain 1 to 50 characters and may contain only letters, digits, spaces, apostrophes, hyphens, and full stops.                                                                                       |

For a plan name containing spaces, `amem` treats the final non-prefixed argument as PERSON_INDEX and the preceding text as MEMBERSHIP_NAME. If supplied, `e/` must follow PERSON_INDEX. For example: `amem Gold Plus 1 e/2026-12-30`.

#### Price

| Aspect         | Rule, message and rationale                                                                                                                                                                                |
| -------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Accepted value | A positive amount with at most two decimal places, written without a currency symbol or thousands separators (for example, `150` or `150.50`). Store it to two decimal places to avoid rounding ambiguity. |
| Error          | Price must be greater than zero and have at most two decimal places.                                                                                                                                       |

#### Number of sessions

| Aspect             | Rule, message and rationale                                                                                                                                                         |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Plan sessions `s/` | A whole number from 1 to 1000. This is the starting balance when the plan is assigned to a client.                                                                                  |
| Session adjustment | `NUMBER_TO_INCREMENT` and `NUMBER_TO_DECREMENT` must each be a whole number from 1 to 1000. The resulting balance must be from 0 to 1000. Zero is allowed after using all sessions. |
| Error              | Number of sessions must be a whole number from 1 to 1000.                                                                                                                           |

#### Expiry date

| Aspect         | Rule, message and rationale                                                                                                                                                                            |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| Accepted value | Optional valid calendar date in `YYYY-MM-DD` format on `amem`. It must be later than the local date when the command runs. An omitted date means the client's assignment has no specified expiry date. |
| Error          | Expiry date must be a valid future date in YYYY-MM-DD format.                                                                                                                                          |

The expiry date belongs to the client's assignment, so clients on the same plan can have different dates. Check the future-date rule only when `amem` runs; an assignment remains valid to load after its date passes. Expiry does not automatically change the remaining session balance.

#### Client index

PERSON_INDEX follows the shared client index rules: a positive integer identifying a client in the displayed list. An index outside the displayed list is rejected. `rmem`, `imem`, and `dmem` require that client to have an assigned plan.

### 6. Output

#### Successful commands

`addmem` saves the plan definition. `view mem` and `view membership` show the available plans with their prices and starting session counts. `amem` stores a copy of the plan name and starting session count on the client, together with a reference to the shared plan and any supplied expiry date. The client's copied count becomes their independent remaining balance. The reference remains attached until `rmem` removes the assignment, including its remaining balance and expiry date. Session adjustments affect only the selected client. When no plan is assigned, `clearmem` removes every plan definition and leaves all other data untouched. When any plan is still assigned to a client, `clearmem` removes nothing and reports that the plans are in use.

```text
Added membership plan: Gold; Price: 150.00; Sessions: 30.
Assigned Gold to Priya Nair; Sessions remaining: 30; Expiry: 2026-12-30.
Removed Gold plan from Priya Nair; 12 remaining sessions lost.
Increased sessions for Priya Nair by 3; Sessions remaining: 33.
Decreased sessions for Priya Nair by 2; Sessions remaining: 31.
Deleted membership plan: Silver.
Cleared all membership plans.
Membership plans are in use and cannot be cleared. Remove all client assignments with rmem first.
```

For an assignment without an expiry date, omit the `Expiry` field in the success message and client card. After `rmem`, the client's card shows no membership plan or session balance. When no plans exist, `view mem` reports `No membership plans available.` After a successful `clearmem`, the catalog is empty and `view mem` reports the same message, while client records and all other data remain unchanged. If any plan is still assigned, `clearmem` deletes nothing and shows `Membership plans are in use and cannot be cleared. Remove all client assignments with rmem first.`

#### Failed commands

**Missing or repeated parameter:** Required `n/`, `p/`, and `s/` values must each appear once in `addmem`. `delmem` requires one `n/` value. Optional `e/` may appear at most once in `amem`.

**Unknown parameter or extra text:** Invalid command format. Use the relevant format in Section 3.

**Invalid client index:** The client index must identify a client in the displayed list.

**No assigned plan:** This client has no membership plan to remove or adjust.

**Insufficient sessions:** Cannot decrease sessions below zero. The balance remains unchanged.

**Session limit exceeded:** Cannot increase sessions above 1000. The balance remains unchanged.

Any failed command leaves plan definitions, assignments, and balances unchanged.

### 7. Duplicate handling

Plan names must be unique after whitespace normalization and case-insensitive comparison. `addmem n/gold ...` is rejected when `Gold` already exists, even if its price or session count differs.

```text
A membership plan named Gold already exists.
```

A client may have at most one assigned plan. Assigning a plan to a client who already has one is rejected, including when it is the same plan; the existing balance is preserved. Use `rmem` before assigning a new plan. Removing an assignment discards its copied name, unused sessions, plan reference, and expiry date.

```text
Priya Nair already has an assigned membership plan.
```

Multiple clients may be assigned the same plan. Their session balances are independent.

### 8. Possible errors

**Plan not found:** No membership plan named Silver exists. This applies to `delmem` and `amem`.

**Plan in use:** A plan referenced by any client assignment cannot be deleted. Remove those clients' assignments with `rmem` first; no client card is changed by a failed deletion.

**Plans in use during clearing:** If any membership plan is assigned to a client, `clearmem` deletes nothing and shows `Membership plans are in use and cannot be cleared. Remove all client assignments with rmem first.` All plans, assignments, and balances remain unchanged.

**Expired assignment:** An expiry date in the past does not silently remove an assignment or deduct sessions. Display the date so the trainer can act on it.

**Data cannot be saved:** The membership change could not be saved. No plan, assignment, or balance was changed.

### 9. Relevant UI mockup

```text
Membership plans  | Gold — 150.00 — 30 sessions
                  | Silver — 80.00 — 12 sessions

Clients           | Priya Nair [SELECTED]
1. Priya Nair     | Gold Member: 31 sessions left [expires 2026-12-30]
Result: Decreased sessions for Priya Nair by 2; Sessions remaining: 31.
```

### 10. Priority distinctions

#### Must have

Create, view, and delete uniquely named plans with a price and 1 to 1000 starting sessions; clear all plans with `clearmem` only when no plan is assigned, otherwise report that the plans are in use; assign at most one plan per client with an optional future, client-specific expiry date; store a copied name and independent balance plus a reference to the plan; block deletion while referenced; remove assignments; show each client's plan and balance on the client card; keep adjustments within 0 to 1000.

#### Nice to have or later

Edit plan definitions, replace an assignment without first removing it, preserve unused sessions on renewal, automatic deductions from workout logs, renewal history, expiry enforcement and alerts, and payment tracking.

## Feature 8: Health considerations

### 1. Feature

Record a client's current injuries and physical limitations as one trainer-visible health profile.

### 2. Purpose

Lets trainers see practical safety considerations before planning or conducting a session. Fittix records the trainer's supplied text only; it does not diagnose, assess medical suitability, or recommend treatment.

### 3. Command format

```text
health INDEX h/NOTE
```

`INDEX` identifies a client in the current displayed list. A nonblank `h/NOTE` creates or replaces the profile. `health INDEX h/` clears it. The `h/` prefix is required in both cases.

### 4. Example commands

```text
health 2 h/Recovering from a left ankle sprain; avoid jumping exercises

health 2 h/
```

### 5. Parameters

| Aspect | Rule, message and rationale |
|---|---|
| Client index | A positive whole number identifying a client in the displayed list. |
| Health note `h/` | Trimmed printable text from 1 to 500 characters, containing at least one letter or digit. Internal spaces and display case are preserved. New lines and control characters are rejected. |
| Clear value | An empty `h/` clears the current profile; it does not delete the client or other client information. |
| Error | Health considerations must contain 1 to 500 printable characters and include at least one letter or digit. |

### 6. Output

On creation or replacement, show `Recorded health considerations for NAME.` On clearing, show `Cleared health considerations for NAME.` The selected client's details show the current profile when present and hide it when absent.

```text
Recorded health considerations for Priya Nair.
```

### 7. Duplicate handling

There is one current profile per client. Supplying a new nonblank note replaces the entire existing note. Supplying the identical trimmed note is rejected as no change.

### 8. Possible errors

**Missing or repeated prefix:** The command requires exactly one `h/` prefix.

**Invalid client index:** The client index must identify a client in the displayed list.

**Medical scope:** The record is informational only. Fittix must not claim that a workout, diet, or appointment is medically safe.

### 9. Search

`find h/KEYWORDS` finds clients whose current health profile contains any supplied keyword, case-insensitively. It combines with other find criteria using the existing OR behavior.

### 10. Priority distinctions

#### Must have

Create, replace, clear, persist, display, and search one current health profile per client.

#### Nice to have or later

Dated medical history, structured injury categories, severity, clinician details, attachments, treatment recommendations, and automated exercise warnings.

## Feature 9: Dietary restrictions

### 1. Feature

Record a client's current dietary restrictions separately from their diet target and coaching notes.

### 2. Purpose

Lets trainers record allergies, exclusions, religious requirements, and food preferences without mixing them into nutrition targets. Fittix records trainer-entered text only and does not determine nutritional or medical suitability.

### 3. Command format

```text
restriction INDEX r/RESTRICTION [r/RESTRICTION]...
```

Repeated `r/` values replace the client's complete current restriction list. A command containing exactly one empty `r/` clears the list.

### 4. Example commands

```text
restriction 2 r/Peanut allergy r/Halal meals r/No shellfish

restriction 2 r/
```

### 5. Parameters

| Aspect | Rule, message and rationale |
|---|---|
| Client index | A positive whole number identifying a client in the displayed list. |
| Restriction `r/` | Each nonblank value is trimmed printable text from 1 to 100 characters, containing at least one letter or digit. Preserve display case and internal spacing. |
| Entry count | Supply from 1 to 10 nonblank restrictions. A single empty `r/` is the sole valid clear form. |
| Duplicate value | Values that match after trimming and ignoring case are duplicates and are rejected. The first-entered spelling and order are retained. |
| Error | Each dietary restriction must contain 1 to 100 printable characters and include at least one letter or digit. |

### 6. Output

On replacement, show `Recorded dietary restrictions for NAME.` On clearing, show `Cleared dietary restrictions for NAME.` The selected client's details show the current ordered list when nonempty and hide it when empty.

### 7. Duplicate handling

The command replaces the whole current list rather than appending to it. Repeating the same restriction in one command, or submitting an unchanged ordered list, is rejected without changing stored data.

### 8. Possible errors

**Missing prefix:** At least one `r/` prefix is required.

**Invalid clear form:** An empty `r/` cannot be combined with nonempty entries or repeated.

**Invalid client index:** The client index must identify a client in the displayed list.

**Medical scope:** The record does not assess allergies, nutritional completeness, or meal safety.

### 9. Search

`find dr/KEYWORDS` finds clients when any current restriction contains any supplied keyword, case-insensitively. It combines with other find criteria using the existing OR behavior.

### 10. Priority distinctions

#### Must have

Replace, clear, persist, display, and search a current ordered list of dietary restrictions independently of the diet plan.

#### Nice to have or later

Restriction categories, severity, expiry dates, meal validation, nutritional guidance, and historical changes.

## Feature 10: Body progress tracker

**Status:** Planned for v1.4; this section defines the intended implementation contract. These commands are not available in v1.3. The original feature-specification PDF is not the reference for this feature.

### 1. Feature and scope

Record starting body measurements, record dated progress with weight and optional notes, view the complete history, and compare the latest recorded weight with the baseline. The v1.4 baseline contains **weight in kilograms and height in centimetres**. Later entries contain weight; height is baseline context, not a repeatedly measured value. Corrections and deletion of progress entries are included so input mistakes can be repaired.

### 2. Purpose

A trainer can review recorded changes for one client without searching separate notes. Fittix reports measurements and arithmetic differences; it does not infer health, diagnose conditions, or label weight gain/loss as good or bad.

### 3. Commands

| Capability | Format | Effect |
| --- | --- | --- |
| Starting measurements | `bodybase INDEX d/DATE w/WEIGHT_KG h/HEIGHT_CM` | Create or replace the client's baseline. |
| Dated progress | `bodylog INDEX d/DATE w/WEIGHT_KG [n/NOTES]` | Add an entry, or replace the complete entry on that date. |
| Complete history | `bodyhistory INDEX` | Show baseline and all progress entries, oldest to newest. |
| Latest comparison | `bodycompare INDEX` | Show baseline weight, latest progress weight and their signed difference. |
| Correct a mistaken record | `bodydelete INDEX d/DATE` | Delete one progress entry; never delete the baseline. |

All five command words and prefix letters are case-sensitive. The displayed client index comes first; prefixed parameters may follow in any order. Each accepted prefix appears at most once. Unknown prefixes, missing/empty required fields and extra positional arguments are rejected. Values such as weight cannot contain extra words or units. No-argument commands' handling of extra text does not apply to these commands.

Example workflow (all dates must be today or earlier):

```text
bodybase 2 d/2020-01-01 w/75.50 h/175
bodylog 2 d/2020-01-08 w/74.80 n/Measured before breakfast
bodylog 2 d/2020-01-15 w/74.20
bodyhistory 2
bodycompare 2
bodylog 2 d/2020-01-08 w/74.60 n/Corrected scale reading
bodydelete 2 d/2020-01-15
```

### 4. Shared validation and parsing

| Parameter | Rule | Error |
| --- | --- | --- |
| `INDEX` | Positive decimal integer in the **currently displayed** client list, including search results. Leading zeros are accepted. Reject zero, signs, fractions, non-digits and values beyond the supported integer range. Never reinterpret an invalid index as a full-list index. | `Client index must be a positive whole number shown in the client list.` Invalid displayed position: `No client exists at index INDEX.` |
| `d/DATE` | Exactly `yyyy-MM-dd`, with a real Gregorian date and four-digit year 0001–9999. Leading/trailing horizontal whitespace is trimmed. On command execution, date must be today or earlier in the local time zone. | `Measurement date must be a real date in yyyy-MM-dd format and must not be in the future.` |
| `w/WEIGHT_KG` | Decimal greater than 0 and at most 500 kg, with at most two decimal places. | `Weight must be greater than 0 and at most 500 kg, with at most 2 decimal places.` |
| `h/HEIGHT_CM` | Decimal greater than 0 and at most 300 cm, with at most two decimal places. Required only for `bodybase`. | `Height must be greater than 0 and at most 300 cm, with at most 2 decimal places.` |
| `n/NOTES` | Optional for `bodylog`. When supplied, 1–300 Unicode code points after trimming outer horizontal whitespace. Preserve internal spaces, punctuation and case; reject line breaks, control and other nonprintable characters before trimming. Omission means absent notes. An explicitly blank `n/` is invalid. | `Progress notes must contain 1 to 300 printable characters without line breaks.` |

Weight/height accept only ASCII digits with an optional decimal point followed by one or two digits, after trimming outer horizontal whitespace. Examples: `75`, `75.5`, `075.50`; reject `+75`, `-1`, `.5`, `75.`, `75.500`, `1e2`, `NaN`, `Infinity`, `75kg` and `1,000`. Store and calculate with exact decimals, never binary floating point. Display weights and height with two decimal places. The upper bounds are product input limits to catch mistakes, not medical suitability thresholds.

Prefixes are recognized only at argument boundaries: start of the argument or after horizontal whitespace. A separated alphabetic token ending in `/` reserves a prefix (`n/`, `w/`, `ex/`, etc.); unsupported or repeated prefixes are errors, including inside notes. A slash within a word, such as `km/day`, is ordinary text. There is no escape syntax for literal reserved prefix tokens in v1.4. Reject embedded line breaks instead of normalizing them into accepted input. Command parsing must preserve raw values until these checks run.

Format failures report `Invalid command format. Use: FORMAT`, substituting the relevant format from Section 3. Validation priority is: command structure (including required fields and duplicate/unknown prefixes), index syntax, field values, displayed-index range, then the state-dependent rules below. Do not partially apply a command when a later field fails.

### 5. Starting measurements: `bodybase`

Each client has at most one baseline. All three fields (date, weight, height) are required on both creation and replacement. A different baseline replaces the whole baseline and **retains every progress entry**. With existing entries, the replacement baseline's date must be strictly earlier than every progress date; otherwise reject it with:

```text
Baseline date must be earlier than every recorded progress date. No changes were made.
```

An identical date, numerical weight and numerical height is rejected with `This is already the current body baseline for NAME.` Differences only in numeric formatting, such as `75.5` and `75.50`, are identical. There is no baseline version history. Replacement immediately changes the reference used by comparison; the trainer must correct the baseline deliberately.

Success:

```text
Created body baseline for Priya Nair: 2020-01-01; Weight: 75.50 kg; Height: 175.00 cm.
Replaced body baseline for Priya Nair: 2020-01-01; Weight: 75.00 kg; Height: 175.00 cm.
```

### 6. Progress entries: `bodylog`

A baseline is required first. An entry's date must be strictly after the baseline date and no later than today. Reject a missing baseline with `No body baseline recorded for NAME. Use bodybase first.` Reject an entry on/before the baseline with `Progress date must be later than the baseline date.`

"Weekly" describes the expected trainer workflow, not a mandatory seven-day interval. Daily, irregular and backdated entries are accepted when these date rules hold. There is at most **one progress entry per client per date**; multiple clients may share dates. Re-entering an existing date replaces its entire entry, including notes, with explicit replacement feedback. Omitting `n/` during replacement clears old notes. To correct an erroneous date, record the entry on the correct date and delete the mistaken one using `bodydelete`.

An entry with the same date, numerical weight and notes is rejected with `This body progress entry already exists for NAME on DATE.` Notes comparison is case-sensitive after outer trimming; internal spaces matter. A case-only notes correction is therefore allowed.

Success:

```text
Recorded body progress for Priya Nair on 2020-01-08: 74.80 kg.
Replaced body progress for Priya Nair on 2020-01-08: 74.60 kg.
```

### 7. History: `bodyhistory`

Show the targeted client's name, baseline date/weight/height, and **every** progress entry with date, weight and optional notes. List entries by measurement date ascending, regardless of input order. The baseline is displayed separately and is not counted as a progress entry. Omitted notes appear as `—`; long valid notes remain readable through wrapping and vertical scrolling. Never truncate the history to a fixed number of recent entries or silently discard older entries.

No baseline: `No body baseline recorded for NAME. Use bodybase first.` A baseline with no entries still displays its measurements and `No body progress entries recorded.` Successful feedback: `Showing body progress history for NAME: N entries.`

For a valid client index, missing-data results from history/comparison are successful empty-state displays: select that client and show the relevant empty state. They are not invalid mutations. An invalid index never changes selection.

### 8. Comparison: `bodycompare`

Choose the entry with the **greatest measurement date**, not the entry most recently entered or edited. Display its date/weight alongside the baseline date/weight/height. Calculate `latest weight - baseline weight`, in kg, exactly, with two decimals: positive `+1.30 kg`, negative `-1.30 kg`, unchanged `0.00 kg`. Do not show a negative sign for zero. Do not compare height, calculate BMI/percentages, or assess whether the change is desirable.

No baseline: use the same missing-baseline message as history. Baseline only: `No body progress entries recorded for NAME. Use bodylog before comparing.` Do not present the baseline as a later measurement. Successful feedback: `Showing body progress comparison for NAME.` Editing/deleting an entry or replacing the baseline refreshes the comparison; deleting the latest entry selects the next greatest date, or restores the no-progress state.

### 9. Corrections: `bodydelete`

Identify a progress entry by date within the indexed client; other clients are unaffected. A missing baseline uses the shared missing-baseline error. A valid date with no progress entry reports `No body progress entry exists for NAME on DATE.` Specifying the baseline date reports `The baseline cannot be deleted with bodydelete. Use bodybase to correct it.` Deletion is permanent in the v1.4 tracker and has no confirmation step; any later shared undo/redo support must be documented when integrated.

Success: `Deleted body progress for NAME on DATE.` Deleting the last entry retains the baseline and produces empty history/comparison states. There is no command to delete only the baseline; it can be corrected by replacement.

### 10. Display, persistence and failures

The selected client's scrollable details area contains baseline, history and comparison sections. `bodyhistory` and `bodycompare` select the requested client and reveal the relevant section **without changing the current client filter or numbering**. Successful mutations select that client and refresh the body-progress display after saving. Selection changes clear stale fields, notes and comparison values. No body data shows the missing-baseline state. Invalid commands and failed saves must not send a success/display request or leave rejected values visible.

```text
Priya Nair — BODY PROGRESS
Baseline: 2020-01-01 | 75.50 kg | 175.00 cm
Progress history (2):
2020-01-08 | 74.80 kg | Measured before breakfast
2020-01-15 | 74.20 kg | —
Latest comparison: 2020-01-15
75.50 kg -> 74.20 kg | Change: -1.30 kg
```

Keep valid maximum-length text readable at the application's minimum supported window size, with wrapping/scrolling rather than automatically resizing the window. A later UI redesign may change placement, but must preserve all specified data, empty states and targeting behavior.

Body data belongs to a client. Contact edits, diet/workout changes, remarks, scheduling, membership changes and workout logging preserve it. Body commands preserve those other fields and never alter membership sessions or payments. Deleting a client removes their body data; `clear` removes body data with its clients and follows the shared catalog-preservation rule. `clearmem` does not alter body data.

Save baseline and entries with the client. Older files with a missing/null body-progress field load as an empty tracker. Persist exact measurement values, optional notes and dates; restore chronological order on load. Reject malformed data (including null list elements, wrong field types, invalid dates/numbers, duplicate entry dates, entries without a baseline, or entries on/before its date) through the normal data-loading error path, not unchecked exceptions or silent omission. Loading also rejects future measurement dates against the local date; tests must control the clock.

Every mutation validates its full candidate before replacing the client. A save failure restores the previous in-memory client and saved file, including all other feature data, and reports `The body progress change could not be saved. No changes were made.` This includes baseline creation/replacement, entry creation/replacement and deletion. History/comparison do not modify measurements or other domain data. No partial write or later successful command may persist a previously rejected mutation.

### 11. v1.4 priorities and acceptance evidence

**Must have:** all four assigned capabilities and the correction/deletion paths above; strict validation; clear empty/duplicate/error states; independent client targeting; persistence/rollback; readable display; current UG and DG. Verify positive/negative/zero comparisons, out-of-order entry dates, replacing/clearing notes, baseline corrections that retain history, last-entry deletion, filtered indices, restart with populated data, legacy files, malformed loads and fault-injected failed saves.

**Later scope:** circumference/body-fat measurements, repeated height, charts, BMI/health interpretations, photographs, reminders, imports/exports, multiple measurements on one day, baseline version history and a dedicated tracker undo/redo command. These are not needed to complete this v1.4 feature.

## Feature 11: Advanced scheduling and client monitoring (v1.4 Alpha)

### 1. Feature

Find clients without a recently recorded workout, view all client appointments on a date, record ad-hoc meetings, and prevent two appointments from starting at the same time.

### 2. Purpose

Give a trainer one daily agenda and a way to identify clients who may need follow-up. Keep meetings distinct from training sessions while preserving existing schedule records.

### 3. Command format

```text
inactive DAYS
dayschedule d/YYYY-MM-DD
meeting INDEX d/YYYY-MM-DD tm/HH:mm l/LOCATION
schedule INDEX d/YYYY-MM-DD tm/HH:mm l/LOCATION
```

`schedule` is the existing training appointment command (Feature 2); Feature 11 adds the shared start-time conflict rule. `viewschedule INDEX` continues to show one client's appointments, including meetings.

### 4. Example commands

```text
inactive 14
dayschedule d/2099-03-15
meeting 1 d/2099-03-15 tm/14:30 l/Cafe
schedule 2 d/2099-03-15 tm/15:30 l/Gym
```

### 5. Parameters

| Parameter | Rule |
|---|---|
| `DAYS` | Positive whole number within the supported integer range. The look-back window includes today and the previous `DAYS - 1` calendar days. |
| `INDEX` | Positive index in the currently displayed client list, including filtered results. |
| `d/YYYY-MM-DD` | Real calendar date. `dayschedule` may inspect a past, current, or future date; a new `meeting` or `schedule` appointment must be later than the current local date and time. |
| `tm/HH:mm` | Valid 24-hour local time for a new appointment. |
| `l/LOCATION` | Nonblank meeting or training location. |

`inactive` and `dayschedule` inspect the full client roster even when the current client list is filtered. They do not change the current filter or stored data.

### 6. Output

`inactive` lists clients with no completed workout logged within the window, sorted by client name. Each row shows the latest logged workout date, or `never recorded` if none exists. Scheduled training and meetings do not count as completed workouts. If every client has a recent workout, show `All clients have a recorded workout in the last DAYS days.`

`dayschedule` lists all training sessions and meetings on the chosen date, ordered by start time and then client name. Each row identifies its appointment type and location. An empty day shows `No client appointments on DATE.` A successful `meeting` reports the client, date, time, and location, and the appointment also appears in `viewschedule` and `dayschedule`.

```text
Clients without a recorded workout in the last 14 days:
1. Bob Cat — last workout: 2026-09-15
2. Priya Nair — last workout: never recorded

Client appointments on 2099-03-15:
1. 14:30 Amy Bee (meeting) at Cafe
2. 15:30 Bob Cat (training) at Gym

Recorded meeting for Amy Bee: 2099-03-15 14:30 at Cafe
```

### 7. Duplicate and conflict handling

Before saving a `meeting` or `schedule`, search every client's training sessions and meetings for the same date **and start time**, including the selected client's records. Reject a clash without changing data: `Scheduling conflict: NAME already has an appointment at YYYY-MM-DD HH:mm.` Older saved sessions with no appointment type load as training. Appointment durations are not stored, so different start times can still overlap in practice.

### 8. Possible errors

**Invalid day count:** Reject zero, negative, fractional, nonnumeric, or oversized `DAYS` values.

**Invalid appointment input:** Reject an invalid displayed index, malformed date or time, blank location, or a new appointment that is not in the future. Do not partially save an appointment.

**Same start time:** Report the existing client's name and start time using the conflict message above; preserve all appointments.

### 9. Search and persistence

`inactive` searches completed workout logs, not scheduled appointments. `dayschedule` searches all clients' appointments. Meetings use the existing `trainingSessions` storage list with a type field; records saved before this feature remain readable as training sessions. Successful changes persist through the app's normal save path.

### 10. Priority distinctions

#### Must have

The four commands and behaviors above: full-roster inactivity and day views, dated meetings, exact start-time conflict checks for both appointment types, persistence, and compatibility with older training sessions.

#### Nice to have or later

Appointment durations and overlap detection, recurring meetings, reminders, calendar layout, and edit/delete appointment commands.
