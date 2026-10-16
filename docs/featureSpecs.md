# Fittix Feature Specification

**Team** - CS2103T-T14-01
## Features covered

1. Add client
2. Delete client
3. Training schedule
4. Workout plan management
5. Workout session logging
6. Diet plan management

## Feature 1A: Add client

### 1. Feature

Add a new client.

### 2. Purpose

Creates the name and phone record required before storing a client's schedule, plans, and workout logs.

### 3. Command format

```text
add n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...
```

Name/phone are required; email/address/tags are optional. Any parameter order is accepted; repeat `t/` for tags. Retaining these AB3 fields supports richer records without slowing basic registration. Optional inputs remain implemented MVP features; tags do not provide fitness-goal filtering.

### 4. Example commands

```text
add n/Priya Nair p/91234567

add n/Mdm Lim p/+65 9876 5432 e/lim@example.com a/10 Sample Road t/morning t/regular
```

### 5. Parameters

#### Name parameter

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | 1 to 70 characters after normalization. Unicode letters, spaces, apostrophes, hyphens, and full stops are accepted. At least one letter is required. Supports real names; excludes delimiters and unrelated symbols. |
| Whitespace | Leading and trailing spaces are removed. Consecutive internal whitespace becomes one space. Ensures consistent comparison and display. |
| Letter case | The original case is displayed. Duplicate comparison ignores cases. Preserves display case; prevents case-only duplicates. |
| Error | Names must be 1 to 70 characters long and may contain only letters, spaces, apostrophes, hyphens, and full stops. |

**Accepted examples**

```text
Priya Nair   Anne-Marie Tan   D'Angelo Lim   Dr. Ahmad Rahman
```

**Rejected examples**

```text
12345   John @ Tan
```

#### Phone parameter

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | An optional leading plus sign followed by 7 to 15 digits. Spaces and hyphens may separate digits. Supports local and international numbers. |
| Normalization | Spaces and hyphens are removed. A leading plus sign is retained. Values such as 8123 4567 and 8123-4567 compare consistently. |
| Rejected value | Parentheses, letters, decimal points, multiple plus signs, and other symbols are rejected. Keeps phone values comparable. |
| Error | Phone numbers must contain 7 to 15 digits and may begin with one plus sign. Spaces and hyphens may be used as separators. |

| Aspect | Rule, message and rationale |
|---|---|
| 9123 4567 | 91234567 Spaces removed |
| 8123-4567 | 81234567 Hyphen removed |
| +65 9876 5432 | +6598765432 International prefix retained |

#### Optional contact parameters

| Parameter | Rule, rationale and error |
|---|---|
| Email `e/` | Trim; maximum 254 ASCII characters; one @; no whitespace. Nonempty local part: letters/digits/._+-; no edge or consecutive dots. Domain: 2+ dot-separated labels, each 1–63 letters/digits/hyphens with alphanumeric ends. Preserve local case; lowercase domain. Common addresses supported; delivery unverified.<br><br>Error: Email must be a valid address such as lim@example.com (maximum 254 characters). |
| Address `a/` | Trim; 1–200 printable characters, no line breaks. Preserve internal spacing and case. Free text supports varied postal addresses; no address verification.<br><br>Error: Address must contain 1 to 200 printable characters without line breaks. |
| Tags `t/` | Trim; 1–30 ASCII letters/digits per tag. Store lowercase; collapse repeated tags after normalization, retaining first-entry order. At most 10 distinct tags. Compact labels avoid spelling/case duplicates.<br><br>Error: Tags must contain 1 to 30 letters or digits; at most 10 distinct tags are allowed. |

Omitted email/address remain absent and omitted tags form an empty set. Any invalid supplied field rejects the entire command; no partial client is saved. Prefix tokens after whitespace are reserved and cannot occur literally inside values.

### 6. Output

#### Successful command

Create, save, display, and select the client. Show normalized name/phone and supplied email, address, and tags; hide omitted fields. The success message remains name and phone only.

```text
Added client: Priya Nair; Phone: 91234567.
```

#### Failed Command

**Missing name:** Name is required. Use: `add n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...`

**Missing phone number:** Phone number is required. Use: `add n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...`

**Both values are missing:** Name and phone number are required. Use: `add n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...`

**Repeated parameter:** The `n/`, `p/`, `e/`, and `a/` prefixes may each be used only once. Repeat `t/` for multiple tags.

**Unknown parameter or extra text:** Invalid command format. Use: `add n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [t/TAG]...`

A failed command does not create a partial client record.

### 7. Duplicate handling

Duplicates require both normalized name (case-insensitive) and phone (spaces/hyphens removed) to match. Different email, address, or tags do not bypass this rule; add never updates or merges an existing client.

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
2. Mdm Lim       | Optional email/address/tags: hidden when absent
3. Priya Nair [NEW] |
Result: Added client: Priya Nair; Phone: 91234567.
```

### 10. Priority distinctions

#### Must have

Add, validate, normalize, deduplicate, display, and select a client using required name/phone and optional email/address/tags. All five fields are supported in the MVP.

#### Nice to have or later

Bulk import, fitness/health fields, custom identifiers, and tag-based filtering.

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

| Aspect | Rule, message and rationale |
|---|---|
| Accepted index | A positive whole number identifying a client in the displayed list. Fast input consistent with AB3. |
| Leading zeros | Leading zeros are accepted. For example, 03 means 3. The numeric meaning is unambiguous. |
| Rejected index | Zero, negative values, decimals, signs, and nonnumeric text are rejected. The command must identify one visible record. |
| Invalid value error | The client index must be a positive whole number shown in the client list. |
| Out of range error | No client exists at index {INDEX}. |

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

Record a client's recurring or one-off training schedule.

### 2. Purpose

Records weekly sessions and dated appointments, with optional locations, so trainers know when and where to meet clients.

### 3. Command format

```text
schedule INDEX d/DAY_OR_DATE t/START-END [l/LOCATION]
```

One command records one entry. Repeating the command records additional entries for the same client.

### 4. Example commands

```text
schedule 2 d/Mon t/18:00-19:00 l/Tampines Gym

schedule 2 d/2026-10-03 t/09:30-10:30 l/Client home

schedule 2 d/Mon t/18:00-19:30 l/Tampines Gym Level 2
```

### 5. Parameters

#### Client index

INDEX follows the shared client index rules.

#### Day or date parameter

| Aspect | Rule, message and rationale |
|---|---|
| Weekday | Mon to Sun or the corresponding full weekday name. Matching ignores case and display uses the three letter form. Fast recurring-session input. |
| Date | A real Gregorian date in yyyy-MM-dd format. It must be today or later. ISO dates are unambiguous and sortable. |
| Invalid format error | Day/date must be a weekday such as Mon or a date in yyyy-MM-dd format. |
| Impossible date error | {DATE} is not a valid calendar date. |
| Past date error | A one-off schedule date cannot be in the past. Keeps upcoming schedules separate from history. |

#### Time range parameter

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | START-END using 24 hour HH:mm times. Hours are 00 to 23 and minutes are 00 to 59. Compact start/end input. |
| Ordering | The end must be later than the start. Both times are on the same day. The MVP does not represent overnight sessions. |
| Format error | Time must use START-END in 24-hour HH:mm format, for example `t/18:00-19:00`. |
| Order error | Schedule end time must be later than its start time. |

#### Location parameter

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | Optional. 1 to 100 printable characters after trimming. Internal spacing and case are preserved. Line breaks are rejected. Supports any meeting place. |
| Error | Location must contain 1 to 100 printable characters. |

### 6. Output

#### New schedule entry

Save the entry; display weekly entries Monday–Sunday and dated entries chronologically.

```text
Added schedule for Priya Nair: Mon, 18:00-19:00 at Tampines Gym.
Added schedule for Priya Nair: Wed, 07:30-08:30.
```

#### Update to an existing slot

Matching client, day/date, and start time identify a slot. Different end time or location replaces it.

```text
Updated schedule for Priya Nair: Mon, 18:00-19:30 at Tampines Gym Level 2.
```

**Missing day or date:** Day/date is required. Use: `schedule INDEX d/DAY_OR_DATE t/START-END [l/LOCATION]`

**Missing time:** Time is required. Use: `schedule INDEX d/DAY_OR_DATE t/START-END [l/LOCATION]`

**Repeated parameter:** The `d/`, `t/`, and `l/` prefixes may each be used only once.

**Unknown parameter or extra text:** Invalid command format. Use: `schedule INDEX d/DAY_OR_DATE t/START-END [l/LOCATION]`

A failed command does not add or change a schedule entry.

### 7. Duplicate handling

Reject entries with identical clients, normalized day/date, start/end times, and normalized location.

```text
This schedule entry already exists for Priya Nair.
```

Different start times and cross-client overlaps are accepted. Conflict detection is later scope; acceptance does not imply no conflict.

### 8. Possible errors

**Overnight session:** Overnight schedule entries are not supported. Record a same-day start and end time.

**Data cannot be saved:** The schedule could not be recorded because Fittix could not save the updated data. No changes were made.

### 9. Relevant UI mockup

```text
Clients          | Priya Nair — TRAINING SCHEDULE
2. Priya Nair [SELECTED] | Mon 18:00–19:00 Tampines Gym
                 | Wed 07:30–08:30 (no location)
                 | 03 Oct 2026 09:30–10:30 Client home
Result: Added schedule for Priya Nair:
Mon, 18:00-19:00 at Tampines Gym.
```

### 10. Priority distinctions

#### Must have

Record/display weekly and dated sessions, same-day time ranges, and optional locations.

#### Nice to have or later

Conflict detection, daily/calendar views, reminders, other recurrence, edit/delete commands, and alternative date/time formats.

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

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | A non-blank exercise name. Required. Supports common exercise names. |
| Repeated prefix | Each prefix, including `n/`, may appear at most once. |
| Empty value | An empty field such as `n/` is invalid. To use a default, omit the whole optional field. |
| Error | Missing required fields, invalid values, or repeated prefixes cause `Invalid command format!` followed by the `addex` usage. |

#### Sets, reps, weight

| Parameter | Rule, rationale and error |
|---|---|
| Sets `s/` | Optional. A positive integer greater than 0. Default `1` when omitted. An empty `s/` is invalid. Supports set counts. |
| Reps `r/` | Optional. A positive integer greater than 0. Default `1` when omitted. An empty `r/` is invalid. Supports strength and conditioning. |
| Weight `w/` | Optional. A finite, non-negative number in kg; decimals are allowed. Default `0` when omitted. Zero means bodyweight or unweighted. An empty `w/` is invalid. No upper bound. |

To use a default, omit the whole field. An empty field such as `s/` or `d/` is invalid. Only `n/`, `s/`, `r/`, `w/`, and `d/` are accepted prefixes; an unknown `x/`-style prefix causes `Invalid command format!`.

#### Day tag (`addex d/` and `dayex DAY`)

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | Optional in `addex`; required as positional `DAY` in `dayex`. A full weekday name or accepted prefix, ignoring letter case. Omitted `d/` groups the exercise under **Day not specified**. Each exercise has one assigned day; assigning a new day replaces the previous day while keeping name, sets, reps, and weight. Assigning an exercise to its current day is allowed. |
| Shortest prefixes | Monday `m`; Tuesday `tu`; Wednesday `w`; Thursday `th`; Friday `f`; Saturday `s`; Sunday `su`. Other examples: `Mon`, `MONDAY`, `Tue`, `Tues`, `Tuesday`, `Wed`, `Wednesday`, `Thu`, `Thurs`, `Thursday`, `Fri`, `Friday`, `Sa`, `Sat`, `Saturday`, `Sun`, `Sunday`. |
| Invalid day | `t` is invalid; use `tu` for Tuesday or `th` for Thursday. `s` means Saturday; Sunday requires at least `su`. Invalid `d/DAY` such as `d/t` or `d/funday` causes `Invalid command format!` followed by usage. The heading `Day not specified` is not a valid `DAY` argument. |
| Display | The card always displays the full weekday name, regardless of the prefix entered. |
| Repeated prefix | The `d/` prefix may appear at most once in `addex`. `dayex` takes exactly three arguments in order: `PERSON_INDEX EXERCISE_INDEX DAY`. |

#### Exercise index (`delex` / `dayex`)

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | A positive integer 1, 2, 3, … matching the current numbering across all day groups on that person's card. The plan is immediately sorted by weekday and renumbered after `addex`, `dayex`, or `delex`. Check the updated card before entering another command that uses an exercise index. |
| Ordering effect | Adding an exercise, changing its day, or deleting an exercise can change the numbers shown. Always use the current displayed exercise number for `dayex` and `delex`. Exercises already in the same day group keep their relative order when the plan is sorted. |
| Person out of range | `The person index provided is invalid.` |
| Exercise out of range | `The exercise index provided is invalid.` This includes when the plan is empty. |
| Remaining exercises | Remaining exercises are renumbered after deletion. A day heading disappears when its last exercise is removed. |

#### Daily capacity limit (`addex` / `dayex`)

| Aspect | Rule, message and rationale |
|---|---|
| Limit | Each client can have at most 7 exercises per weekday. Other weekdays and other clients have separate limits. Exercises under **Day not specified** are not capped. |
| `addex` | Adding an 8th exercise to the same weekday is rejected with `A client cannot have more than 7 exercises on the same day.` The plan is left unchanged. |
| `dayex` | Moving an exercise into a weekday that already has 7 exercises is rejected with the same message. Assigning an exercise to its current day when that day already has 7 is allowed, as is moving an exercise away from a full day. |
| Day resolution | Weekday prefixes and letter case are resolved through the same weekday table above. |

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

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | A real date in yyyy-MM-dd format that is today or earlier. Unambiguous dates for completed sessions. |
| Format error | Session date must use yyyy-MM-dd. |
| Impossible date error | {DATE} is not a valid calendar date. |
| Future date error | A completed workout session cannot have a future date. |

#### Session time

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | Optional 24 hour HH:mm time with hours 00 to 23 and minutes 00 to 59. Distinguishes sessions on the same date. |
| Error | Session time must use 24-hour HH:mm format, for example `t/18:00`. |

#### Exercise entry

Every `ex/` value contains exactly four components separated by vertical bars.

| Aspect | Rule, message and rationale |
|---|---|
| Exercise | 1 to 50 characters with at least one letter. Letters, digits, spaces, hyphens, apostrophes, and parentheses are accepted. The vertical bar is rejected. This supports common exercise names while reserving the separator. |
| Exercise error | Exercise names must be 1 to 50 characters long and may contain letters, digits, spaces, hyphens, apostrophes, and parentheses. |
| Weight | A number from 0 to 1000 with up to two decimal places. The value is kilograms. Zero means bodyweight or unweighted. Supports gym equipment and incremental loads. |
| Weight error | Exercise weight must be between 0 and 1000 kg and may have up to 2 decimal places. |
| Sets | A whole number from 1 to 100. Requires a set; catches excessive values. |
| Sets error | Sets must be a whole number from 1 to 100. |
| Repetitions | A whole number from 1 to 1000. Supports strength and conditioning. |
| Repetitions error | Repetitions must be a whole number from 1 to 1000. |
| Entry structure error | Exercise entries must use `ex/EXERCISE\|WEIGHT\|SETS\|REPS`. |
| Entry count error | A workout session may contain at most 30 exercise entries. |

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

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | 1 to 200 printable characters after trimming, with at least one letter or digit. Internal spaces, punctuation, numbers, units, and case are preserved. Supports varied nutrition targets. |
| Line breaks | Line breaks are rejected. The current plan must remain readable in the details panel. |
| Error | Diet target must contain 1 to 200 printable characters. |

Fittix records the trainer's text. It does not decide whether the target is medically appropriate.

#### Diet notes

| Aspect | Rule, message and rationale |
|---|---|
| Accepted value | Optional. 1 to 300 printable characters after trimming. Internal spacing, punctuation, and case are preserved. Optional coaching instructions. |
| Line breaks | Line breaks are rejected. The plan remains compact and easy to scan. |
| Error | Diet-plan notes must contain 1 to 300 printable characters. |

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
