# Fittix Client Search and Profile Feature Specification

**Team** - CS2103T-T14-01

This document describes the current implemented behaviour of the client search and profile features in Fittix.

## Features covered

1. Search clients by name
2. Search clients by phone number or email
3. Edit client personal details
4. Filter clients by fitness goal
5. Record each client's fitness goal
6. Record injuries and physical limitations
7. Record dietary restrictions

## Shared client index rules

Commands that take an `INDEX` use the one-based index shown in the current displayed client list. A valid index is a positive whole number that identifies an existing displayed client. Leading zeroes are accepted, so `03` identifies the client at index 3.

## Feature 1: Search clients by name

### 1. Feature

Search for clients using all or part of their names.

### 2. Purpose

Lets trainers locate one or more client records without manually scanning the full client list.

### 3. Command format

```text
find KEYWORD [MORE_KEYWORDS]...

find n/NAME_KEYWORDS
```

Both forms search names only. At least one nonblank keyword is required. The `n/` form can also be combined with other prefixed search criteria.

### 4. Example commands

```text
find priya

find priya daniel

find n/priya daniel
```

### 5. Parameters

| Aspect | Rule and current behaviour |
|---|---|
| Name keyword | Each whitespace-separated value is treated as a name keyword. |
| Matching | A client matches when the client's name contains at least one keyword. Matching ignores letter case and uses substring matching. |
| Multiple keywords | Keywords are combined using OR. `find priya daniel` matches a name containing `priya` or `daniel`. |
| Prefixed form | `n/` may appear once and allows the name criterion to be combined with other prefixed criteria using OR. |
| Required value | At least one nonblank keyword must follow `find`. |

### 6. Output

A successful search replaces the displayed client list with the matching clients.

```text
Found 2 client(s).
```

When no client matches:

```text
Found 0 client(s). Use list to show all clients.
```

### 7. Matching handling

Each matching client appears once even if the name matches more than one supplied keyword. The search does not modify any client data.

### 8. Possible errors

**Missing keyword:** `Search keyword is required. Use: find KEYWORD... or find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL] [dr/RESTRICTION] [h/HEALTH]`

### 9. Relevant UI mockup

```text
Command: find priya daniel

Clients
1. Daniel Tan
2. Priya Nair

Result: Found 2 client(s).
```

### 10. Implemented behaviour

Search names using one or more case-insensitive substring keywords, display each matching client once, and report the result count.

## Feature 2: Search clients by phone number or email

### 1. Feature

Search for clients using all or part of their phone numbers or email addresses.

### 2. Purpose

Lets trainers retrieve a client when contact information is known but the client's name is not.

### 3. Command format

```text
find p/PHONE

find e/EMAIL_KEYWORDS

find p/PHONE e/EMAIL_KEYWORDS
```

The `p/` and `e/` prefixes may each appear at most once. When both are supplied, a client matches either criterion.

### 4. Example commands

```text
find p/9123-4567

find e/example.com

find p/91234567 e/priya
```

### 5. Parameters

| Parameter | Rule and current behaviour |
|---|---|
| Phone `p/` | Must contain at least one digit. Spaces and hyphens are removed from both the query and stored phone number before substring matching. |
| Email `e/` | Accepts one or more whitespace-separated keywords. A client matches when the email contains any keyword, ignoring letter case. |
| Empty value | A supplied `p/` or `e/` value cannot be empty. |
| Repeated prefix | Each supported search prefix may appear only once. |
| Combined criteria | Phone and email criteria are combined using OR. Matching either criterion includes the client. |

### 6. Output

A successful search replaces the displayed client list and reports the number of matches.

```text
Found 1 client(s).
```

When no client matches:

```text
Found 0 client(s). Use list to show all clients.
```

### 7. Matching handling

Phone matching uses the complete supplied `p/` value as one normalized substring query. Email matching treats whitespace-separated values as alternative keywords. A client matching both criteria is displayed only once.

### 8. Possible errors

**Empty search value:** `The p/ search value cannot be empty.` or `The e/ search value cannot be empty.`

**Phone query without a digit:** `Phone search must contain at least one digit.`

**Repeated search prefix:** `The n/, p/, e/, g/, dr/ and h/ prefixes may each be used only once.`

**Unprefixed text mixed with prefixes:** `Invalid command format. Use: find KEYWORD... or find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL] [dr/RESTRICTION] [h/HEALTH]`

### 9. Relevant UI mockup

```text
Command: find p/9123-4567

Clients
1. Priya Nair — 91234567

Result: Found 1 client(s).
```

### 10. Implemented behaviour

Search normalized phone substrings and case-insensitive email substrings, combine supplied criteria using OR, and report the result count.

## Feature 3: Edit client personal details

### 1. Feature

Edit an existing client's personal and profile details.

### 2. Purpose

Keeps a client's contact information, address, tags, and current fitness goal up to date without replacing the client record.

### 3. Command format

```text
edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [g/GOAL] [t/TAG]...
```

At least one field must be supplied. Any omitted field retains its current value.

### 4. Example commands

```text
edit 2 p/98765432 e/alex.tan@example.com

edit 2 a/18 Clementi Road t/evening t/regular

edit 2 g/
```

### 5. Parameters

| Parameter | Rule and current behaviour |
|---|---|
| Client index | A positive whole number identifying a client in the displayed list. |
| Name `n/` | Must contain only alphanumeric characters and spaces and must not be blank. |
| Phone `p/` | Must contain only digits and be at least 3 digits long. |
| Email `e/` | Must follow the application's local-part and domain email format. |
| Address `a/` | May contain any value but must not be blank. |
| Goal `g/` | A nonblank value replaces the goal. An empty `g/` clears it. Nonblank goals follow the fitness-goal rules in Feature 5. |
| Tags `t/` | Each tag must be alphanumeric. Supplied tags replace the complete tag set; a single empty `t/` clears all tags. |
| Repeated prefixes | `n/`, `p/`, `e/`, `a/`, and `g/` may each appear once. Repeat `t/` to supply multiple tags. |

### 6. Output

On success, the displayed list returns to all clients and the result identifies the edited client.

```text
Edited client: Alex Tan; Phone: 98765432.
```

If all supplied values are equal to the client's current values:

```text
No changes made: the new values are the same as the current details.
```

### 7. Duplicate handling

The edit is rejected if the resulting client would duplicate another client under the application's existing client identity check. Editing a client to values already held by that same client is reported as no change.

### 8. Possible errors

**Missing index:** `Client index is required. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [g/GOAL] [t/TAG]...`

**Invalid index:** `The client index must be a positive whole number shown in the client list.`

**Index outside the displayed list:** `No client exists at index INDEX.`

**No editable field:** `At least one field to edit must be provided.`

**Repeated single-value prefix:** `The n/, p/, e/, a/ and g/ prefixes may each be used only once. Repeat t/ for multiple tags.`

**Duplicate client:** `This person already exists in the address book.`

Invalid field values are rejected using the corresponding field validation message, and the client remains unchanged.

### 9. Relevant UI mockup

```text
Before: Alex Tan — 91234567
Command: edit 2 p/98765432 e/alex.tan@example.com
After:  Alex Tan — 98765432
Result: Edited client: Alex Tan; Phone: 98765432.
```

The selected client's profile and contact fields refresh to show the edited values.

### 10. Implemented behaviour

Replace only the supplied editable fields while preserving the client's remark, workout plan, workout sessions, training schedule, diet plan, dietary restrictions, and health considerations.

## Feature 4: Filter clients by fitness goal

### 1. Feature

Filter the client list using the clients' recorded fitness goals.

### 2. Purpose

Lets trainers find clients who share a goal keyword, such as strength, mobility, or endurance.

### 3. Command format

```text
find g/GOAL_KEYWORDS
```

The `g/` prefix may appear at most once. Goal search can be combined with the other supported prefixed search criteria.

### 4. Example commands

```text
find g/strength

find g/build strength

find n/priya g/mobility
```

### 5. Parameters

| Aspect | Rule and current behaviour |
|---|---|
| Goal keyword | The `g/` value is split into whitespace-separated keywords. |
| Matching | A client matches when the current goal contains at least one supplied keyword, ignoring letter case. |
| Missing goal | A client without a recorded goal does not match a goal criterion. |
| Combined criteria | Goal and other prefixed criteria are combined using OR. `find n/priya g/mobility` matches a client whose name contains `priya` or whose goal contains `mobility`. |
| Required value | A supplied `g/` value cannot be empty. |

### 6. Output

The displayed client list is replaced with matching clients.

```text
Found 3 client(s).
```

When no client matches:

```text
Found 0 client(s). Use list to show all clients.
```

### 7. Matching handling

Multiple goal keywords are alternatives. Each matching client appears once even when its goal contains more than one keyword or it also matches another supplied search criterion.

### 8. Possible errors

**Empty goal search:** `The g/ search value cannot be empty.`

**Repeated goal prefix:** `The n/, p/, e/, g/, dr/ and h/ prefixes may each be used only once.`

**Mixed unprefixed and prefixed search:** `Invalid command format. Use: find KEYWORD... or find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL] [dr/RESTRICTION] [h/HEALTH]`

### 9. Relevant UI mockup

```text
Command: find g/strength

Clients
1. Priya Nair — Goal: Build strength
2. Alex Tan — Goal: Improve strength and mobility

Result: Found 2 client(s).
```

### 10. Implemented behaviour

Filter clients by case-insensitive goal substrings, support multiple alternative keywords, and combine goal criteria with other prefixed criteria using OR.

## Feature 5: Record each client's fitness goal

### 1. Feature

Record one optional current fitness goal for each client.

### 2. Purpose

Keeps the client's main fitness objective visible in the selected client profile and available for goal-based filtering.

### 3. Command formats

```text
add n/NAME p/PHONE e/EMAIL a/ADDRESS [g/GOAL] [t/TAG]...

edit INDEX g/GOAL

edit INDEX g/
```

The goal may be supplied when adding a client, replaced using `edit`, or cleared using an empty `g/` value with `edit`.

### 4. Example commands

```text
add n/Priya Nair p/91234567 e/priya@example.com a/123 Main Street g/Build strength

edit 1 g/Complete a 10 km run

edit 1 g/
```

### 5. Parameters

| Aspect | Rule and current behaviour |
|---|---|
| Accepted value | From 1 to 100 Unicode code points after trimming, including at least one Unicode letter or digit. |
| Whitespace | Leading and trailing whitespace is removed. Internal spacing is retained. |
| Character handling | Control characters are rejected. Letter case and display spelling are retained. |
| Number of goals | Each client has at most one current goal. A new value replaces the complete existing goal. |
| Optional value | Omitting `g/` when adding a client stores no goal. An empty `g/` is supported only by `edit` and clears the existing goal. |
| Error | `Goal must contain 1 to 100 characters, including at least one letter or digit.` |

### 6. Output

Adding a client uses the normal add-client success output. Editing or clearing a goal uses the normal edit-client success output.

```text
Edited client: Priya Nair; Phone: 91234567.
```

Submitting the existing goal again reports:

```text
No changes made: the new values are the same as the current details.
```

### 7. Duplicate handling

There is one current goal per client. Recording a new goal replaces the old goal. The goal does not change how duplicate clients are identified.

### 8. Possible errors

**Invalid goal:** `Goal must contain 1 to 100 characters, including at least one letter or digit.`

**Repeated goal prefix:** The `g/` prefix may appear only once in an `add` or `edit` command.

**Empty goal during add:** An empty `g/` value is invalid because an omitted `g/` already represents no goal for a new client.

Any rejected command leaves the existing client data unchanged.

### 9. Relevant UI mockup

```text
Client details
Priya Nair
Phone: 91234567
Goal: Build strength
```

The selected client's details display the goal. When no goal is recorded, the goal field displays an em dash (`—`).

### 10. Implemented behaviour

Create, replace, clear, validate, persist, display, and search one optional current fitness goal per client. Other client updates preserve the recorded goal.

## Feature 6: Record injuries and physical limitations

### 1. Feature

Record a client's current injuries and physical limitations as one trainer-visible health-considerations note.

### 2. Purpose

Keeps the client's current physical considerations available in the selected client profile when the trainer plans or conducts a session.

### 3. Command format

```text
health INDEX h/NOTE
```

`INDEX` identifies a client in the current displayed list. A nonblank `h/NOTE` creates or replaces the note. `health INDEX h/` clears it. Exactly one `h/` prefix is required.

### 4. Example commands

```text
health 2 h/Recovering from a left ankle sprain; avoid jumping exercises

health 2 h/

find h/ankle sprain
```

### 5. Parameters

| Aspect | Rule and current behaviour |
|---|---|
| Client index | A positive whole number identifying a client in the displayed list. |
| Health note `h/` | Trimmed printable text from 1 to 500 Unicode code points, containing at least one letter or digit. Internal spacing and display case are preserved. |
| Rejected characters | ISO control, format, line-separator, paragraph-separator, surrogate, private-use, and unassigned characters are rejected. |
| Clear value | An empty `h/` clears the current note without changing other client information. |
| Number of notes | Each client has at most one current health-considerations note. A new nonblank value replaces the complete existing note. |
| Error | `Health considerations must contain 1 to 500 printable characters and include at least one letter or digit.` |

### 6. Output

On creation or replacement:

```text
Recorded health considerations for Priya Nair.
```

On clearing:

```text
Cleared health considerations for Priya Nair.
```

When the submitted value is already current:

```text
No changes made: health considerations are already current.
```

### 7. Duplicate handling

Supplying a new nonblank note replaces the existing note. Supplying the identical trimmed note, or clearing a client that already has no note, is rejected as no change.

### 8. Possible errors

**Missing, repeated, or malformed prefix:** The command is rejected using the `health INDEX h/NOTE` usage message unless exactly one `h/` value is supplied.

**Invalid client index or format:** The command is rejected using the `health INDEX h/NOTE` usage message when the index cannot be parsed.

**Index outside the displayed list:** `The person index provided is invalid.`

**Invalid note:** `Health considerations must contain 1 to 500 printable characters and include at least one letter or digit.`

### 9. Relevant UI mockup

```text
Client details
Priya Nair

Health considerations
Recovering from a left ankle sprain; avoid jumping exercises
```

The health-considerations section is shown only when the selected client has a note. `find h/KEYWORDS` matches any case-insensitive substring keyword in the current note and combines with other prefixed criteria using OR.

### 10. Implemented behaviour

Create, replace, clear, validate, persist, display, and search one current health-considerations note per client. Saved data without this optional field loads with no note, and other client updates preserve it.

## Feature 7: Record dietary restrictions

### 1. Feature

Record a client's current dietary restrictions separately from the client's diet-plan target and coaching notes.

### 2. Purpose

Keeps allergies, exclusions, religious requirements, and food preferences visible without mixing them into nutrition targets.

### 3. Command format

```text
restriction INDEX r/RESTRICTION [r/RESTRICTION]...
```

One or more nonblank `r/` values replace the client's complete restriction list. A command containing exactly one empty `r/` clears the list.

### 4. Example commands

```text
restriction 2 r/Peanut allergy r/Halal meals r/No shellfish

restriction 2 r/

find dr/peanut halal
```

### 5. Parameters

| Aspect | Rule and current behaviour |
|---|---|
| Client index | A positive whole number identifying a client in the displayed list. |
| Restriction `r/` | Each nonblank value is trimmed printable text from 1 to 100 Unicode code points and must contain at least one letter or digit. Internal spacing and display case are preserved. |
| Rejected characters | ISO control, format, line-separator, paragraph-separator, surrogate, private-use, and unassigned characters are rejected. |
| Entry count | Supply from 1 to 10 nonblank restrictions. |
| Clear value | Exactly one empty `r/` clears the complete list. An empty value cannot be combined with another `r/` value. |
| Duplicate value | Values that match after trimming and case-insensitive comparison are rejected as duplicates. |
| Ordering | Entry order and original display spelling are retained. |
| Error | `Each dietary restriction must contain 1 to 100 printable characters and include at least one letter or digit. At most 10 restrictions are allowed, without duplicates.` |

### 6. Output

On replacement:

```text
Recorded dietary restrictions for Priya Nair.
```

On clearing:

```text
Cleared dietary restrictions for Priya Nair.
```

When the submitted ordered list is already current:

```text
No changes made: dietary restrictions are already current.
```

### 7. Duplicate handling

The command replaces the complete current list rather than appending to it. Duplicate values within one command are rejected after trimming and case-insensitive comparison. Submitting the same ordered list, or clearing an already empty list, is rejected as no change.

### 8. Possible errors

**Missing restriction prefix:** The command is rejected using the `restriction INDEX r/RESTRICTION [r/RESTRICTION]...` usage message.

**Invalid clear form:** An empty `r/` cannot be repeated or combined with a nonblank restriction.

**Invalid client index or format:** The command is rejected using the restriction command usage message when the index cannot be parsed.

**Index outside the displayed list:** `The person index provided is invalid.`

**Invalid restriction list:** `Each dietary restriction must contain 1 to 100 printable characters and include at least one letter or digit. At most 10 restrictions are allowed, without duplicates.`

### 9. Relevant UI mockup

```text
Client context
Dietary restrictions
Peanut allergy; Halal meals; No shellfish
```

The dietary-restrictions section is shown only when the selected client has at least one restriction. `find dr/KEYWORDS` matches when any current restriction contains any supplied keyword, ignoring case, and combines with other prefixed criteria using OR.

### 10. Implemented behaviour

Replace, clear, validate, persist, display, and search one current ordered list of dietary restrictions per client. Saved data without this optional list loads with an empty list, and other client updates preserve it independently of the diet plan.

## Combined prefixed search behaviour

The client search features share the following prefixed command form:

```text
find [n/NAME_KEYWORDS] [p/PHONE] [e/EMAIL_KEYWORDS]
     [g/GOAL_KEYWORDS] [dr/RESTRICTION_KEYWORDS] [h/HEALTH_KEYWORDS]
```

| Aspect | Rule and current behaviour |
|---|---|
| Prefix count | Each supported prefix may appear at most once. |
| Keyword fields | `n/`, `e/`, `g/`, `dr/`, and `h/` split their values into whitespace-separated keywords. |
| Phone field | `p/` uses its complete value as one normalized substring query. |
| Combination | All supplied field criteria are combined using OR. A client is displayed when any criterion matches. |
| Command forms | A prefixed search cannot contain unprefixed search text. |
| Data handling | Searching changes only the displayed client list and does not modify stored client data. |

For example:

```text
find n/priya g/strength dr/peanut
```

This displays clients whose name contains `priya`, whose goal contains `strength`, or whose dietary restrictions contain `peanut`.
