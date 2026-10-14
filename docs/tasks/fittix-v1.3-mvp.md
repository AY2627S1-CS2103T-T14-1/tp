# Fittix v1.3 MVP: Find, Goal Field, Edit (Kaiwen)

Team: AY2627S1-CS2103T-T14-1. Codebase: fork of AB3 (Java 17, JavaFX, Jackson JSON storage).

This file is self-contained. A coding agent should be able to implement every v1.3 issue owned by Kaiwen using only this file and the repository. Each task below maps to exactly one GitHub issue, so one agent run produces one PR that closes one issue.

## Task index

Fill in the issue numbers once, then give the agent one task at a time.

| Task | GitHub issue title | Issue # | Depends on |
|---|---|---|---|
| 1 | Search by name: support n/ prefix and OR-combined criteria in find | #___ | P1 |
| 2 | Search by phone or email: support p/ and e/ in find | #___ | Task 1, P3, P4 |
| 3 | Fitness goal: add optional goal to Person and g/ on add and edit | #___ | P2 |
| 4 | Fitness goal: persist and display goals | #___ | Task 3 |
| 5 | Fitness goal: support g/ filter in find | #___ | Tasks 2, 3 |
| 6 | Edit client: align edit with AB3 validation and duplicate rules | #___ | Task 3, P5 |
| 7 | Search by name: document and verify MVP behavior | #___ | Task 1 |
| 8 | Search by phone or email: document and verify MVP behavior | #___ | Task 2 |
| 9 | Fitness goal: document and verify MVP behavior | #___ | Tasks 3, 4, 5 |
| 10 | Edit client: document and verify MVP behavior | #___ | Task 6 |

```
P1 ──> Task 1 ──> Task 2 ──┬──> Task 5
P3, P4 ─────────────┘      │
P2 ──> Task 3 ──┬──────────┘
                ├──> Task 4
                └──> Task 6 <── P5

Docs: Task 7 after 1 | Task 8 after 2 | Task 9 after 3, 4, 5 | Task 10 after 6
```

Tasks 1 and 3 can start in parallel. Task 6 starts after Task 3 and P5.

---

## Branch and PR workflow

This task plan uses two parallel workstreams. Finish dependent tasks sequentially on the same workstream branch;
create another parallel branch only for work that is independent.

```
master ──> Find workstream: Task 1 ──> Task 2
master ──> Goal workstream: Task 3 ──> Task 4

After both workstreams merge into `master`:
master ──> Task 5
```

- Create the find workstream and goal workstream directly from `master`.
- Keep Tasks 1 and 2 on the find branch, and Tasks 3 and 4 on the goal branch.
- Start Task 5 from `master` only after both workstreams have merged.
- Start Task 6 from `master` after Task 3 and P5 are available. It uses the current AB3 model validation and
  name-based identity rules.
- Keep one task per commit and, where practical, one task per PR. Preserve existing feature branches as history.

Documentation tasks follow the same dependency model. Complete Tasks 7 and 8 sequentially on the find workstream
after their respective feature tasks; this avoids competing edits to the `find` guide section. Tasks 9 and 10 remain
blocked by their listed feature dependencies.

---

## 0. Rules for the coding agent

1. Read sections 0 to 3 fully before starting any task.
2. Do exactly one task per run. Check its "Depends on" line first. If a dependency is not present in the codebase, stop and report it instead of implementing it.
3. Run the prerequisite check (section 2) before the first task.
4. Do not perform git operations (branching, committing, pushing, PRs). The developer handles git.
5. Do not change code belonging to teammates' features (add/delete client validation, training schedule, workout plan, workout log, diet plan) beyond what a task explicitly lists.
6. Stay inside the task's scope. If a later task covers something, leave it for that task even if it looks easy to add now.
7. Package paths assume AB3's `seedu.address`. If the team has renamed packages, use the renamed equivalent and keep the same class names.
8. Every user-facing message must match section 3 character for character, including the trailing period.
9. Stop when the task's acceptance criteria pass.
10. When the codebase contradicts this file (for example, a field you expect does not exist), stop and report the contradiction rather than guessing.
11. Manual checks are run by the developer in the real app (`./gradlew run`). Do not claim to have run them. At the end of a docs task, output the manual check list as a markdown checklist the developer can paste into the PR description.

---

## 1. Build and coding standard

Run after every task. All must pass:

```
./gradlew clean test checkstyleMain checkstyleTest
```

Coding standard: https://se-education.org/guides/conventions/java/intermediate.html

Rules most often broken:

- Javadoc first sentence is a third-person verb phrase ending in a period: `Returns true if the given string is a valid goal.`
- Every public class and every non-trivial public method has a Javadoc header.
- Boolean names sound boolean: `isValidGoal`, `hasGoal`, `isGoalCleared`.
- Constants are `UPPER_SNAKE_CASE`. No wildcard imports.
- Every new main class gets a matching test class.
- Copy existing AB3 structure (for example `Name.java` and `NameTest.java`) instead of inventing new patterns.
- Predicates implement `equals`, `hashCode` and `toString` (using `ToStringBuilder`), like `NameContainsKeywordsPredicate`.

---

## 2. Prerequisite check (v1.2 foundations)

These were v1.2 issues. Verify each one exists and behaves as described. If one is missing, stop and report it; the developer will decide whether to implement it first using the spec here.

### P1. Partial name matching

Verify: `StringUtil.containsIgnoreCase(String sentence, String keyword)` exists, and `NameContainsKeywordsPredicate.test` uses it instead of `containsWordIgnoreCase`.

Spec:
- `containsIgnoreCase`: `requireNonNull` on both arguments; `checkArgument` that the keyword is not blank (same style as `containsWordIgnoreCase`); compare using `toLowerCase(Locale.ROOT)` and `String.contains`.
- `NameContainsKeywordsPredicate` matches if the name contains any keyword (OR).
- Tests: `"pau"` matches `Alice Pauline`; `"ALI"` matches `Alice Pauline`; `"xyz"` does not; `["zzz", "bob"]` matches `Alice Bob`.

### P2. Goal class

Verify: `model/person/Goal.java` exists with this behaviour.

Spec (structure like `Name.java`):
- `MESSAGE_CONSTRAINTS = "Goal must contain 1 to 50 characters, including at least one letter or digit."`
- `MAX_LENGTH = 50`, `public final String value`.
- Constructor: `requireNonNull`, `checkArgument(isValidGoal(goal), MESSAGE_CONSTRAINTS)`, stores `goal.strip()`.
- `isValidGoal(String test)` returns true iff, after `strip()`: length is 1 to 50, no control characters (regex `[^\p{Cntrl}]+`), and at least one letter or digit (regex `.*[\p{L}\p{N}].*`).
- `equals` compares `value` exactly; `hashCode` is `value.hashCode()`; `toString` returns `value`.

### P3. PhoneMatchesPredicate

Verify: `model/person/PhoneMatchesPredicate.java` exists.

Spec:
- Constructor takes the raw query and stores it normalised (all spaces and hyphens removed).
- `test(person)`: normalise the person's phone the same way, return `normalisedPhone.contains(normalisedQuery)`.
- If `Phone` already exposes a normalisation method, use that instead of a private helper.

### P4. EmailContainsKeywordsPredicate

Verify: `model/person/EmailContainsKeywordsPredicate.java` exists.

Spec:
- Takes `List<String> keywords`. Matches if the email contains any keyword as a case-insensitive substring.
- Returns false if the client has no email.

### P5. Reject no-change edits

Verify: `EditCommand` has `MESSAGE_NO_CHANGES` and throws it when the edited client equals the original.

Spec:
- `public static final String MESSAGE_NO_CHANGES = "No changes made: the new values are the same as the current details.";`
- In `EditCommand.execute`, after `createEditedPerson(...)` and before the duplicate check: `if (editedPerson.equals(personToEdit)) { throw new CommandException(MESSAGE_NO_CHANGES); }`.
- The AB3 test `execute_noFieldSpecifiedUnfilteredList_success` is flipped to assert failure with `MESSAGE_NO_CHANGES`.

---

## 3. Feature specification

Section 3 describes what the app must do from the user's point of view. Section 4 (the tasks) describes how to build it. When they seem to disagree, section 3 wins; report the conflict.

### 3.1 Fitness goal field

**Command format:** new optional prefix `g/GOAL` on `add` and `edit`.

```
add n/NAME p/PHONE [e/EMAIL] [a/ADDRESS] [g/GOAL] [t/TAG]...
```

Keep the current optional/required markers for the other fields. Only insert `[g/GOAL]`.

**Examples:**

```
add n/Priya Nair p/91234567 g/Fat loss
add n/Mdm Lim p/98765432 g/Build strength for hiking
edit 3 g/Marathon prep
edit 3 g/
```

| Aspect | Rule |
|---|---|
| Accepted value | 1 to 50 characters after trimming, no line breaks or other control characters, at least one letter or digit. |
| Whitespace | Leading and trailing spaces removed. Internal spacing preserved. |
| Case | Stored and displayed as entered. `find g/` ignores case. |
| Repetition | `g/` at most once per command. |
| Clearing | `edit INDEX g/` (empty value) removes the goal. On `add`, an empty `g/` is a validation error. |
| Error | `Goal must contain 1 to 50 characters, including at least one letter or digit.` |
| Duplicates | Goal is ignored by duplicate detection (`isSamePerson`). |
| Equality | Goal is included in `Person.equals`, `hashCode` and `toString`. |
| Display | `Goal: <value>` in the client card and client details panel. Hidden when absent. |
| Add success message | Unchanged. |
| Storage | `JsonAdaptedPerson` has a nullable `goal` string. Missing or null loads as no goal, so old data files still load. An invalid stored goal throws `IllegalValueException(Goal.MESSAGE_CONSTRAINTS)`, following AB3's existing illegal-value handling. |

### 3.2 Find clients

**Command format:**

```
find KEYWORD [MORE_KEYWORDS]...
find [n/NAME_KEYWORDS] [p/PHONE] [e/EMAIL_KEYWORDS] [g/GOAL_KEYWORDS]
```

- Form 1 (no prefixes) is treated exactly like `find n/KEYWORD [MORE_KEYWORDS]...`.
- Form 2 requires at least one prefix. Prefixes may appear in any order, each at most once.
- A non-empty preamble together with any prefix is a format error (for example `find alex p/9123`).

**Examples:**

```
find pri
find n/alice richards
find p/9123 4567
find e/example.com
find g/strength
find n/tan g/fat loss
```

| Prefix | Matching rule |
|---|---|
| `n/` | Split value on whitespace (`\s+`). Match if the name contains any keyword as a case-insensitive substring. |
| `p/` | Whole value is one query (not split). Remove spaces and hyphens from query and stored phone. Match if normalised phone contains normalised query. Query must contain at least one digit. |
| `e/` | Split on whitespace. Match if email contains any keyword, case-insensitive substring. No email never matches. |
| `g/` | Split on whitespace. Match if goal contains any keyword, case-insensitive substring. No goal never matches. |
| Across prefixes | OR. A client is listed if they match any supplied criterion, and appears once even if matching several. |

**Output on success:**

- The displayed list is filtered to matches, in existing list order.
- One or more matches: `Found {N} client(s).`
- Zero matches: `Found 0 client(s). Use list to show all clients.`
- Selection: if the client details panel has a selected client still in the filtered list, keep it; otherwise select the first match; if no matches, clear the panel. If the codebase has no selection model, keep AB3 behaviour and report this.

**Errors** (checked in this order):

| Situation | Message |
|---|---|
| No arguments | `Search keyword is required. Use: find KEYWORD... or find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]` |
| Preamble mixed with prefixes | `Invalid command format. Use: find KEYWORD... or find [n/NAME] [p/PHONE] [e/EMAIL] [g/GOAL]` |
| Repeated prefix | `The n/, p/, e/ and g/ prefixes may each be used only once.` |
| Empty prefix value, e.g. `find n/` | `The n/ search value cannot be empty.` (substitute the actual prefix) |
| Phone query with no digit | `Phone search must contain at least one digit.` |

Until Task 5, find messages must advertise only prefixes implemented in that branch: Task 1 advertises `n/` only,
and Task 2 advertises `n/`, `p/` and `e/`. Task 5 adds `g/` to the command usage, no-keyword error,
mixed-preamble error and repeated-prefix error together with actual goal filtering. Do not advertise `g/` before
Task 5 because AB3's tokenizer treats it as an unprefixed name search.

AB3's tokenizer does not detect unknown prefixes. `find x/abc` with no recognised prefix is treated as form 1, so `x/abc` becomes a name keyword. This is accepted behaviour.

### 3.3 Edit client

**Command format:**

```
edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [g/GOAL] [t/TAG]...
```

At least one field is required. Any parameter order is accepted.

**Examples:**

```
edit 2 p/91234568
edit 1 n/Priya Nair e/priya@example.com
edit 3 g/Marathon prep
edit 3 g/
edit 1 t/
```

| Aspect | Rule |
|---|---|
| Index | Positive whole number within the displayed list. Leading zeros allowed. |
| Field validation | Reuse the model classes (`Name`, `Phone`, `Email`, `Address`, `Tag`, `Goal`) and `ParserUtil`. No validation logic inside `EditCommand` or `EditCommandParser`. |
| Clearing fields | Empty `g/` clears the goal. Empty `t/` clears all tags. Email and address remain required, so empty `e/` or `a/` gives that field's normal validation error. |
| Required fields | Empty `n/` or `p/` gives that field's normal validation error. |
| Tags | Supplied tags replace the whole tag set. |
| Repetition | `n/`, `p/`, `e/`, `a/`, `g/` at most once; `t/` repeatable. |
| Owned data | Schedule, workout plan, workout logs, diet plan and any other field teammates added to `Person` are copied unchanged. |
| Position | The edited client keeps its position in the list. |

**Success:** save, display and select the edited client. Message: `Edited client: {NAME}; Phone: {PHONE}.`

**Errors:**

| Situation | Message |
|---|---|
| Missing index | `Client index is required. Use: edit INDEX [n/NAME] [p/PHONE] [e/EMAIL] [a/ADDRESS] [g/GOAL] [t/TAG]...` |
| No fields supplied | `At least one field to edit must be provided.` |
| Index not a positive whole number | `The client index must be a positive whole number shown in the client list.` |
| Index out of range | `No client exists at index {INDEX}.` |
| Repeated prefix | `The n/, p/, e/, a/ and g/ prefixes may each be used only once. Repeat t/ for multiple tags.` |
| Field invalid | That field class's `MESSAGE_CONSTRAINTS` |
| Would duplicate another client | `This person already exists in the address book.` |
| Nothing changed | `No changes made: the new values are the same as the current details.` |

Any failure leaves the client unchanged. Save failures are handled by `LogicManager` as in AB3.

**Duplicate rule:** use the existing AB3 `isSamePerson` rule (exact name). An edit is rejected only if the result
matches a different client. Changing only the case of the client's own name is allowed.

---

## 4. Tasks

Each task lists the issue's acceptance criteria first, then exactly what to change.

---

### Task 1: Search by name: support n/ prefix and OR-combined criteria in find

- **Closes:** issue #___ (see task index)
- **Spec:** 3.2, `n/` only
- **Depends on:** P1
- **Out of scope:** `p/`, `e/`, `g/` (Tasks 2 and 5), User Guide (Task 7)

**Issue acceptance criteria:**

- `find pri` behaves the same as `find n/pri`.
- `find n/KEYWORDS` filters by name using the partial matching from v1.2.
- Each prefix may be used at most once; repeats give a specific error.
- An empty prefix value, no arguments, or bare keywords mixed with prefixes each give a specific error message.
- Criteria from different prefixes are combined with OR, and a client appears at most once.
- The success message reports the number of matching clients.
- Parser and command tests cover each path and every error.

**Changes:**

1. `model/person/ClientMatchesAnyPredicate.java` (new)
   - Implements `Predicate<Person>`, holds an unmodifiable `List<Predicate<Person>>`.
   - `test` returns true if any predicate matches.
   - `equals` compares the lists; `hashCode` and `toString` accordingly.
   - This is the OR combination that Tasks 2 and 5 plug into.
2. `logic/parser/FindCommandParser.java`
   - Tokenize on `PREFIX_NAME` only.
   - Structure the code so adding a prefix later means adding it to one list of supported prefixes plus one predicate-building branch. For example, keep a `private static final Prefix[] SUPPORTED_PREFIXES` used for tokenizing, the repeated-prefix check and the empty-value check.
   - Apply the error checks in the order listed in 3.2 (skip the phone-digit check, which belongs to Task 2).
   - Repeated prefix: check `argMultimap.getAllValues(prefix).size() > 1` and throw `ParseException` with the 3.2 message. Do not use `verifyNoDuplicatePrefixesFor`, since its message differs.
   - Form 1 and form 2 both produce `new ClientMatchesAnyPredicate(List.of(namePredicate))`, so `find pri` equals `find n/pri`.
   - Predicates are always added in the fixed order name, phone, email, goal, regardless of input order.
3. `logic/commands/FindCommand.java`
   - Constructor takes `Predicate<Person>`.
   - Add `MESSAGE_FOUND = "Found %1$d client(s)."` and `MESSAGE_FOUND_NONE = "Found 0 client(s). Use list to show all clients."`, used in `execute`.
   - Update `MESSAGE_USAGE` to show both forms with `n/` only. Do not advertise prefixes owned by later tasks.
   - Do not edit `Messages.MESSAGE_PERSONS_LISTED_OVERVIEW` if other commands use it.
4. Selection behaviour per 3.2, if a selection model exists.

**Tests:**

- `ClientMatchesAnyPredicateTest`: one predicate, several (any true), none match, equals.
- `FindCommandParserTest`: bare keywords; `n/` alone; `find pri` equals `find n/pri`; no arguments; preamble with prefix; repeated `n/`; empty `n/`.
- `FindCommandTest`: partial match, multiple keywords (OR), zero matches with `MESSAGE_FOUND_NONE`, success message count.

---

### Task 2: Search by phone or email: support p/ and e/ in find

- **Closes:** issue #___
- **Spec:** 3.2, `p/` and `e/` rows
- **Depends on:** Task 1, P3, P4
- **Out of scope:** `g/` (Task 5), User Guide and Developer Guide (Task 8)

**Issue acceptance criteria:**

- `find p/9123 4567` and `find p/9123-4567` match the client with phone 91234567.
- `find e/example.com` matches clients whose email contains it, ignoring case.
- A phone query containing no digits is rejected with a specific error.
- Clients without an email never match `e/`.
- `p/` and `e/` combine with `n/` using OR.
- Parser and command tests cover each prefix, combinations, and the errors.

**Changes:**

1. `FindCommandParser`: add `PREFIX_PHONE` and `PREFIX_EMAIL` to the supported prefixes.
   - `p/`: the whole trimmed value is one query. If it contains no digit (`!value.matches(".*\\d.*")`), throw `Phone search must contain at least one digit.` Build `PhoneMatchesPredicate`.
   - `e/`: split on `\s+`, build `EmailContainsKeywordsPredicate`.
   - Keep the fixed predicate order (name, phone, email).
   - Update find command usage and parser error messages to advertise exactly `n/`, `p/` and `e/`.
2. No changes to `FindCommand` should be needed. If any are, report why.

**Tests:**

- `FindCommandParserTest`: `p/` alone, `e/` alone, `p/` with `n/` in both orders producing equal commands, repeated `p/`, repeated `e/`, empty `p/`, empty `e/`, `p/abc` digit error.
- `FindCommandTest`: spaced phone query, hyphenated phone query, case-insensitive email, a client without email not matching `e/` (only if email is optional in `Person`), `n/` plus `p/` returning the union.

---

### Task 3: Fitness goal: add optional goal to Person and g/ on add and edit

- **Closes:** issue #___
- **Spec:** 3.1
- **Depends on:** P2. Touches `Person` and `AddCommandParser`; keep changes to those files minimal.
- **Out of scope:** JSON storage, UI and sample data (Task 4), `find g/` (Task 5). After this task, goals work in commands but are not saved to disk or shown; that is expected.

**Issue acceptance criteria:**

- `add ... g/GOAL` stores the goal; omitting `g/` leaves it absent.
- `edit INDEX g/GOAL` updates the goal and `edit INDEX g/` clears it.
- An invalid goal rejects the whole command with the Goal constraint message; `g/` may appear at most once.
- Goal does not affect duplicate detection, which remains the existing name-based `isSamePerson` rule.
- Person equality includes the goal; `isSamePerson` does not.
- Tests cover parsing, validation errors, and equality.

**Changes:**

1. `logic/parser/CliSyntax.java`: add `PREFIX_GOAL = new Prefix("g/")`.
2. `model/person/Person.java`
   - Add a nullable `Goal goal` field with `public Optional<Goal> getGoal()`.
   - Follow the pattern teammates used when adding their fields to `Person`. If none exists, add a constructor that takes the goal and keep the existing constructor delegating with `null`, so other call sites still compile.
   - Include goal in `equals`, `hashCode`, `toString`. Not in `isSamePerson`.
3. `logic/parser/ParserUtil.java`: `parseGoal(String goal)` returns `Goal` (trims; throws `ParseException(Goal.MESSAGE_CONSTRAINTS)` if invalid).
4. `logic/parser/AddCommandParser.java`: tokenize `PREFIX_GOAL`, include it in the existing duplicate-prefix check, set the goal if present. Add `[g/GOAL]` to `AddCommand.MESSAGE_USAGE`.
5. `logic/commands/EditCommand.java` (`EditPersonDescriptor`)
   - Add `private Goal goal;` and `private boolean isGoalCleared;`.
   - `setGoal(Goal)` (also sets `isGoalCleared = false`), `clearGoal()` (sets goal to null, `isGoalCleared = true`), `getGoal()` returning `Optional<Goal>`, `isGoalCleared()`.
   - `isAnyFieldEdited` is true if a goal is set or `isGoalCleared` is true.
   - Copy constructor, `equals` and `toString` include both fields.
   - `createEditedPerson`: goal is `null` if cleared, else `descriptor.getGoal().orElse(personToEdit.getGoal().orElse(null))`.
6. `logic/parser/EditCommandParser.java`: tokenize `PREFIX_GOAL`, include in the duplicate check. Empty (after trim) `g/` calls `clearGoal()`; otherwise `setGoal(ParserUtil.parseGoal(...))`. Add `[g/GOAL]` to `EditCommand.MESSAGE_USAGE`.
7. Test utilities: `PersonBuilder.withGoal(String)`, `EditPersonDescriptorBuilder.withGoal(String)`. Do not add goals to existing `TypicalPersons` if that breaks other tests; create separate test persons instead.

**Tests:**

- `ParserUtilTest`: `parseGoal` valid, trimmed, invalid.
- `AddCommandParserTest`: with goal, without goal, invalid goal, repeated `g/`.
- `EditCommandParserTest`: set goal, clear with `g/`, invalid goal, repeated `g/`.
- `EditCommandTest`: setting and clearing a goal leaves other fields untouched.
- `PersonTest`: goal affects `equals`; `isSamePerson` ignores it.
- `EditPersonDescriptorTest`: equality covers goal and the cleared flag.

---

### Task 4: Fitness goal: persist and display goals

- **Closes:** issue #___
- **Spec:** 3.1, Storage and Display rows
- **Depends on:** Task 3
- **Out of scope:** `find g/` (Task 5), User Guide (Task 9)

**Issue acceptance criteria:**

- The goal is saved to and loaded from the data file.
- Existing data files without a goal still load, with no goal.
- An invalid stored goal is handled the same way as other invalid stored values.
- The goal is shown on the client card and details when present and hidden when absent.
- Storage tests cover valid, invalid, and missing goals.

**Changes:**

1. `storage/JsonAdaptedPerson.java`: add `@JsonProperty("goal") String goal` (nullable) to the creator constructor; set it from the model in the from-model constructor (`source.getGoal().map(g -> g.value).orElse(null)`). In `toModelType`, null gives no goal; an invalid value throws `IllegalValueException(Goal.MESSAGE_CONSTRAINTS)`.
2. UI: in `PersonCard.java` and `PersonListCard.fxml`, and the client details panel if one exists, add a label showing `Goal: <value>`. When absent, call `setVisible(false)` and `setManaged(false)` on the label so it takes no space.
3. `model/util/SampleDataUtil.java`: give two or three sample clients a goal.

**Tests:**

- `JsonAdaptedPersonTest`: valid goal, invalid goal (expects `Goal.MESSAGE_CONSTRAINTS`), null goal.
- `JsonSerializableAddressBookTest`: add `src/test/data/JsonSerializableAddressBookTest/noGoalAddressBook.json`, a valid data file with no `goal` keys, and assert it loads with every client having no goal.
- `JsonAddressBookStorageTest` (or equivalent): save then read a client with a goal; the goal is preserved.

---

### Task 5: Fitness goal: support g/ filter in find

- **Closes:** issue #___
- **Spec:** 3.2, `g/` row
- **Depends on:** Task 2, Task 3
- **Out of scope:** User Guide (Task 9)

**Issue acceptance criteria:**

- `find g/strength` lists clients whose goal contains "strength", ignoring case.
- Multiple goal keywords are OR.
- Clients without a goal never match `g/`.
- `g/` combines with `n/`, `p/` and `e/` using OR, and may appear at most once.
- Predicate, parser, and command tests cover goal matching, clients without goals, and combined prefixes.

**Changes:**

1. `model/person/GoalContainsKeywordsPredicate.java` (new): takes `List<String> keywords`; case-insensitive substring, OR across keywords; false if the client has no goal. Implements `equals`, `hashCode`, `toString`.
2. `FindCommandParser`: add `PREFIX_GOAL` to the supported prefixes; split value on `\s+`; add the predicate last in the fixed order.
3. Update `FindCommand.MESSAGE_USAGE` and the parser's no-keyword, mixed-preamble and repeated-prefix errors to
   advertise `n/`, `p/`, `e/` and `g/`.

**Tests:**

- `GoalContainsKeywordsPredicateTest`: match, case-insensitive match, OR, no match, client with no goal, equals.
- `FindCommandParserTest`: `g/` alone, `g/` with `n/` in both orders producing equal commands, repeated `g/`, empty `g/`.
- `FindCommandParserTest`: usage and parser errors include `g/` only after this task adds its parser support.
- `FindCommandTest`: `find g/strength`; a client without a goal is excluded; `find n/<name> g/<goal>` returns the union.

---

### Task 6: Edit client: align edit with AB3 validation and duplicate rules

- **Closes:** issue #___
- **Spec:** 3.3
- **Depends on:** Task 3, P5
- **Out of scope:** User Guide (Task 10)

**Issue acceptance criteria:**

- Edited fields reuse the current AB3 model validation; edit contains no separate validation logic.
- `g/` with an empty value clears the goal and `t/` clears all tags. Empty `n/`, `p/`, `e/`, and `a/` use their field class's normal validation error.
- An edit that would make the client a duplicate of another client under the existing name-based identity rule is rejected.
- Changing only the case of the client's own name is allowed.
- Schedule, workout plan, workout logs, and diet plan are unchanged after an edit.
- The success message shows the edited name and phone, and any failure leaves the client unchanged.
- Tests cover clearing fields, duplicates, and preservation of owned data.

**Changes:**

1. `EditCommandParser`
   - Missing index (empty preamble): 3.3 missing-index message.
   - Non-numeric, zero or negative index: 3.3 invalid-index message.
   - Repeated `n/`, `p/`, `e/`, `a/`, `g/`: 3.3 repeated-prefix message (check `getAllValues(prefix).size() > 1` manually).
   - Empty `n/`, `p/`, `e/`, or `a/`: delegate to that field's `ParserUtil` method so its AB3 validation message is shown.
   - No fields: `At least one field to edit must be provided.`
2. `EditCommand`
   - Out-of-range index: `No client exists at index {INDEX}.` using the one-based index typed.
   - Order in `execute`: index check, build edited person, no-change check (P5), duplicate check, update model.
   - Duplicate check: keep AB3's condition `!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)` and `MESSAGE_DUPLICATE_PERSON`.
   - `MESSAGE_EDIT_PERSON_SUCCESS = "Edited client: %1$s; Phone: %2$s."` with the edited name and phone values.
   - `createEditedPerson` passes every non-edited field through unchanged, including all fields teammates added to `Person`. Read the current `Person` constructor and list each field explicitly.
   - Select the edited client after success if a selection model exists.

**Tests:**

- `EditCommandParserTest`: missing index, invalid index, repeated prefixes, empty name, empty phone, clearing each optional field, no fields.
- `EditCommandTest`:
  - out-of-range index message
  - duplicate name rejected with `MESSAGE_DUPLICATE_PERSON`
  - case-only name change on the same client succeeds
  - a client with schedule, workout plan, logs and diet plan (whichever exist in `Person`) keeps all of them after editing the phone
  - failure leaves the model unchanged
  - success message format

---

### Docs tasks (7 to 10): shared instructions

- Edit only the sections named in the task. The command summary table is shared by all four docs tasks; change only the rows the task names.
- Every message, limit and example must match the code exactly. Copy messages from the source files, not from this document.
- Do not use em dashes.
- End the run by outputting the task's manual check list as a markdown checklist (`- [ ] ...`) for the developer to paste into the PR description.

**Documentation ownership:**

| Task | Owned documentation |
|---|---|
| 7 | User Guide `find` section and command-summary `find` row |
| 8 | User Guide `find` section and command-summary `find` row; Developer Guide `find` implementation section |
| 9 | User Guide `add`, goal sentence in `edit`, `find`, and command-summary `add`, `edit`, and `find` rows |
| 10 | Remaining User Guide `edit` behavior and command-summary `edit` row |

Each documentation PR must include that task's manual-check checklist in its PR description.

**Manual check setup (developer, once per docs task):**

1. Move `data/addressbook.json` (or the team's data file) somewhere safe so real data is not touched.
2. Run `./gradlew run`, then `clear`.
3. Add the test clients below. If the add command requires other fields, add them with any valid value.

```
add n/Priya Nair p/91234567 e/priya@example.com g/Build strength
add n/Marcus Tan p/9876 5432 e/marcus@gmail.com g/Fat loss
add n/Lim Wei Ling p/81112222 e/weiling@example.com
```

4. Run `list` before each check that uses an index.
5. When finished, close the app and move the original data file back.

---

### Task 7: Search by name: document and verify MVP behavior

- **Closes:** issue #___
- **Depends on:** Task 1

**Issue acceptance criteria:**

- The User Guide describes both find forms for names, partial and case-insensitive matching, and examples.
- Every message and example matches the code exactly.
- Manual checks pass: partial match, case difference, multiple keywords, no match, and an error case.

**Changes:**

- `docs/UserGuide.md`, `find` section: both forms, `n/` matching rule (partial, case-insensitive, OR across keywords), examples `find pri`, `find n/alice richards`, the success and zero-match messages.
- Command summary table: `find` row shows both forms.

**Manual checks:**

| Command | Expected |
|---|---|
| `find pri` | Priya Nair only; `Found 1 client(s).` |
| `find PRIYA` | Same as above |
| `find n/pri` | Same as above |
| `find priya marcus` | Priya and Marcus; `Found 2 client(s).` |
| `find zzz` | Empty list; `Found 0 client(s). Use list to show all clients.` |
| `find` | No-arguments error from 3.2 |
| `find pri n/tan` | Format error from 3.2 |
| `find n/` | `The n/ search value cannot be empty.` |

---

### Task 8: Search by phone or email: document and verify MVP behavior

- **Closes:** issue #___
- **Depends on:** Task 2

**Issue acceptance criteria:**

- The User Guide describes `p/` and `e/`, phone normalisation, OR combination, and examples.
- The Developer Guide explains how find combines predicates.
- Every message and example matches the code exactly.
- Manual checks pass: spaced and hyphenated phone queries, partial email, combined prefixes, and an error case.

**Changes:**

- `docs/UserGuide.md`, `find` section: `p/` and `e/` matching rules, that spaces and hyphens are ignored in phone search, OR across prefixes, examples `find p/9123 4567`, `find e/example.com`, `find n/tan p/9123`, and the phone-digit error.
- `docs/DeveloperGuide.md`: an implementation section for `find` explaining that `FindCommandParser` builds one predicate per supplied prefix and wraps them in `ClientMatchesAnyPredicate` (OR), and that predicates are added in a fixed order so equal inputs produce equal commands for testing. Add a sequence diagram for `find n/... p/...` only if the DG already uses diagrams for other commands; follow the existing PlantUML setup in `docs/diagrams/`.

**Manual checks:**

| Command | Expected |
|---|---|
| `find p/9123 4567` | Priya only |
| `find p/9123-4567` | Priya only |
| `find p/9876` | Marcus only |
| `find e/GMAIL` | Marcus only |
| `find e/example` | Priya and Lim Wei Ling |
| `find n/priya e/gmail` | Priya and Marcus |
| `find p/abc` | `Phone search must contain at least one digit.` |
| `find p/1 p/2` | Repeated-prefix error from 3.2 |

---

### Task 9: Fitness goal: document and verify MVP behavior

- **Closes:** issue #___
- **Depends on:** Tasks 3, 4, 5

**Issue acceptance criteria:**

- The add and edit formats and the command summary table include `[g/GOAL]`.
- The User Guide describes `find g/` with examples, and the goal's limits.
- Every message and example matches the code exactly.
- Manual checks pass: add with goal, edit and clear goal, find by goal, and loading an older data file.

**Changes:**

- `docs/UserGuide.md`:
  - `add` section: format includes `[g/GOAL]`; goal rules (1 to 50 characters, at least one letter or digit, trimmed); example with a goal.
  - `edit` section: format includes `[g/GOAL]`; `g/` with no value clears the goal. Only touch the format line and the goal sentence; Task 10 owns the rest of the edit section.
  - `find` section: `g/` matching rule, clients without a goal never match, examples `find g/strength` and `find n/tan g/fat loss`.
  - Command summary table: `add`, `edit` and `find` rows include `[g/GOAL]`.

**Manual checks:**

| Step | Expected |
|---|---|
| After setup, look at Priya's card | Shows `Goal: Build strength` |
| Look at Lim Wei Ling's card | No goal line, no blank gap |
| `edit 3 g/Marathon prep` | Lim's card shows `Goal: Marathon prep` |
| `edit 3 g/` | Goal line disappears |
| `add n/Test User p/90000000 g/!!!` | Goal constraint error; client not added |
| `find g/STRENGTH` | Priya only |
| `find g/fat strength` | Priya and Marcus |
| `find n/lim g/strength` | Priya and Lim Wei Ling |
| Close and reopen the app | Goals still shown |
| Close the app, delete every `"goal"` line from the data file, reopen | App loads all clients with no goals and no error |

---

### Task 10: Edit client: document and verify MVP behavior

- **Closes:** issue #___
- **Depends on:** Task 6

**Issue acceptance criteria:**

- The User Guide covers edit syntax, clearing optional fields, the duplicate rule, and the no-change message.
- Every message and example matches the code exactly.
- Manual checks pass: edit each field, clear an optional field, duplicate rejection, and edit of a client that has a schedule and plans.

**Changes:**

- `docs/UserGuide.md`, `edit` section: format, at least one field required, which fields can be cleared with an empty prefix, tags replace the whole set, the name-based duplicate rule, the no-change message, examples from 3.3. Leave the goal sentence written by Task 9 in place.
- Command summary table: `edit` row only, if its format changed.

**Manual checks:**

| Step | Expected |
|---|---|
| `edit 1 n/Priya Nair` | Success message with new name and phone |
| `edit 1 p/9123 4568` | Phone updated |
| `edit 1 e/priya.n@example.com` | Email updated |
| `edit 1 a/Blk 123 Clementi` | Address updated |
| `edit 1 t/vip` then `edit 1 t/` | Tag added, then all tags cleared |
| `edit 1 a/` | Address cleared (or address validation error, if address is still required) |
| `edit 1 n/priya nair` | Success (case-only change allowed) |
| `edit 1 n/Marcus Tan` | `This person already exists in the address book.`; Priya unchanged |
| `edit 2 p/98765432` (Marcus's current phone) | No-change message |
| `edit 2 n/` | Name validation error |
| `edit 9 p/90000000` | `No client exists at index 9.` |
| Give Marcus a schedule, workout plan and diet plan with teammates' commands, then `edit 2 p/90001111` | Phone changes; schedule, plans and logs all still present |

---

## 5. Open items for the developer (not for the agent)

- Fill in the issue numbers in the task index.
- The team must agree to add `[g/GOAL]` to `add`, since the add-client spec lists fitness fields as later scope.
- The team spec PDF does not yet cover find, edit or the goal field. Add sections matching section 3 so reviewers have a reference.
