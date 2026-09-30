---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

* _{List the sources of reused or adapted ideas, code, documentation, and third-party libraries here, with links to the originals.}_

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** given above explains the high-level design of the App.

The following provides a quick overview of the main components and their interactions.

**Main components of the architecture**

**`Main`** (consisting of classes [`Main`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/Main.java) and [`MainApp`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/MainApp.java)) is in charge of the app launch and shut down.
* At app launch, it initializes the other components in the correct sequence, and connects them up with each other.
* At shut down, it shuts down the other components and invokes cleanup methods where necessary.

The bulk of the app's work is done by the following four components:

* [**`UI`**](#ui-component): The UI of the App.
* [**`Logic`**](#logic-component): The command executor.
* [**`Model`**](#model-component): Holds the data of the App in memory.
* [**`Storage`**](#storage-component): Reads data from, and writes data to, the hard disk.

[**`Commons`**](#common-classes) represents a collection of classes used by multiple other components.

**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact with each other for the scenario where the user issues the command `delete 1`.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in [`Ui.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/Ui.java)

![Structure of the UI Component](images/UiClassDiagram.png)

The UI consists of a `MainWindow` and its parts, such as `CommandBox`, `ResultDisplay`, `PersonListPanel`, and `StatusBarFooter`. All of these, including `MainWindow`, inherit from the abstract `UiPart` class, which captures common behavior among classes that represent visible GUI parts.

The `UI` component uses the JavaFX UI framework. The layouts of these UI parts are defined in matching `.fxml` files in `src/main/resources/view`. For example, [`MainWindow.fxml`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/resources/view/MainWindow.fxml) specifies the layout of [`MainWindow`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/ui/MainWindow.java).

The `UI` component,

* executes user commands using the `Logic` component.
* listens for changes to `Model` data so that the UI can be updated with the modified data.
* keeps a reference to the `Logic` component, because the `UI` relies on the `Logic` to execute commands.
* depends on some classes in the `Model` component because it displays `Person` objects from the model.

### Logic component

**API** : [`Logic.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the interactions within the `Logic` component, taking `execute("delete 1")` API call as an example.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete a person).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object which is returned from `Logic`.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.

### Model component
**API** : [`Model.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="450" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* stores the `Person` objects selected by the current filter, such as search results, in a separate _filtered_ list. It exposes this list as an unmodifiable `ObservableList<Person>` that the UI can observe and bind to, so the UI updates when the list changes.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The alternative, arguably more object-oriented, design below keeps a unique list of tags in `AddressBook`, and each `Person` references tags from that list. This lets `AddressBook` maintain one `Tag` object per unique tag instead of each `Person` holding its own `Tag` objects.<br>

<img src="images/BetterModelClassDiagram.png" width="450" />

</div>


### Storage component

**API** : [`Storage.java`](https://github.com/se-edu/addressbook-level3/tree/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### \[Proposed\] Undo/redo feature

#### Proposed Implementation

The proposed undo/redo mechanism is facilitated by `VersionedAddressBook`. It extends `AddressBook` with an undo/redo history, stored internally as an `addressBookStateList` and `currentStatePointer`. Additionally, it implements the following operations:

* `VersionedAddressBook#commit()` — Saves the current address book state in its history.
* `VersionedAddressBook#undo()` — Restores the previous address book state from its history.
* `VersionedAddressBook#redo()` — Restores a previously undone address book state from its history.

These operations are exposed in the `Model` interface as `Model#commitAddressBook()`, `Model#undoAddressBook()` and `Model#redoAddressBook()` respectively.

Given below is an example usage scenario and how the undo/redo mechanism behaves at each step.

Step 1. The user launches the application for the first time. The `VersionedAddressBook` will be initialized with the initial address book state, and the `currentStatePointer` pointing to that single address book state.

![UndoRedoState0](images/UndoRedoState0.png)

Step 2. The user executes `delete 5` command to delete the 5th person in the address book. The `delete` command calls `Model#commitAddressBook()`, causing the modified state of the address book after the `delete 5` command executes to be saved in the `addressBookStateList`, and the `currentStatePointer` is shifted to the newly inserted address book state.

![UndoRedoState1](images/UndoRedoState1.png)

Step 3. The user executes `add n/David …​` to add a new person. The `add` command also calls `Model#commitAddressBook()`, causing another modified address book state to be saved into the `addressBookStateList`.

![UndoRedoState2](images/UndoRedoState2.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If a command fails its execution, it will not call `Model#commitAddressBook()`, so the address book state will not be saved into the `addressBookStateList`.

</div>

Step 4. The user now decides that adding the person was a mistake, and decides to undo that action by executing the `undo` command. The `undo` command will call `Model#undoAddressBook()`, which will shift the `currentStatePointer` once to the left, pointing it to the previous address book state, and restores the address book to that state.

![UndoRedoState3](images/UndoRedoState3.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index 0, pointing to the initial AddressBook state, then there are no previous AddressBook states to restore. The `undo` command uses `Model#canUndoAddressBook()` to check if this is the case. If so, it will return an error to the user rather
than attempting to perform the undo.

</div>

The following sequence diagram shows how an undo operation goes through the `Logic` component:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Logic.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `UndoCommand` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.

</div>

Similarly, how an undo operation goes through the `Model` component is shown below:

![UndoSequenceDiagram](images/UndoSequenceDiagram-Model.png)

The `redo` command does the opposite — it calls `Model#redoAddressBook()`, which shifts the `currentStatePointer` once to the right, pointing to the previously undone state, and restores the address book to that state.

<div markdown="span" class="alert alert-info">:information_source: **Note:** If the `currentStatePointer` is at index `addressBookStateList.size() - 1`, pointing to the latest address book state, then there are no undone AddressBook states to restore. The `redo` command uses `Model#canRedoAddressBook()` to check if this is the case. If so, it will return an error to the user rather than attempting to perform the redo.

</div>

Step 5. The user then decides to execute the command `list`. Commands that do not modify the address book, such as `list`, will usually not call `Model#commitAddressBook()`, `Model#undoAddressBook()` or `Model#redoAddressBook()`. Thus, the `addressBookStateList` remains unchanged.

![UndoRedoState4](images/UndoRedoState4.png)

Step 6. The user executes `clear`, which calls `Model#commitAddressBook()`. Since the `currentStatePointer` is not pointing at the end of the `addressBookStateList`, all address book states after the `currentStatePointer` will be purged. Reason: It no longer makes sense to redo the `add n/David …​` command. This is the behavior that most modern desktop applications follow.

![UndoRedoState5](images/UndoRedoState5.png)

The following activity diagram summarizes what happens when a user executes a new command:

<img src="images/CommitActivityDiagram.png" width="250" />

#### Design considerations:

**Aspect: How undo & redo execute:**

* **Alternative 1 (current choice):** Saves the entire address book.
  * Pros: Easy to implement.
  * Cons: May have performance issues in terms of memory usage.

* **Alternative 2:** Individual command knows how to undo/redo by
  itself.
  * Pros: Will use less memory (e.g. for `delete`, just save the person being deleted).
  * Cons: We must ensure that the implementation of each individual command is correct.

_{more aspects and alternatives to be added}_

### \[Proposed\] Data archiving

_{Explain here how the data archiving feature will be implemented}_


--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:
- **Name:** Marcus
- **Age:** 31
- **Job:** Freelance personal trainer
- **Experience:** 6 years
- **Education:** Diploma in Sports & Exercise Science
- **Location:** Commercial gym in Tampines
- **Clients:** 28 active clients
- **Workload:** ~32 sessions/week, starting at 6:30am
- **Packages:** 10 or 20 sessions
- **Payments:** Mostly PayNow
- **Admin:** Short breaks and late nights
- **Tech:** Laptop user, fast typist
<small>Marcus is a sample persona representing our target user group and their typical needs, behaviours, and workflows.</small>

**Value proposition**:
- **All-in-one client management:** Keep client goals, injuries, workout plans, diet plans, progress, sessions, and payments in one place.
- **Fast and efficient:** Quickly access and update information between sessions.
- **Progress tracking:** Easily monitor each client's fitness progress over time.
- **Session & payment management:** Track remaining sessions, payments, and unpaid balances.
- **Less admin work:** Reduce time spent searching through chats, notes, spreadsheets, and payment records.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

| Priority | As a …​      | I want to …​                                                   | So that I can …​                                                        |
| -------- | ------------ | -------------------------------------------------------------- | ----------------------------------------------------------------------- |
| `* * *`  | gym trainer  | add a client's contact details                                 | contact them easily                                                     |
| `* * *`  | gym trainer  | record a client's gym schedule                                 | schedule my business appropriately                                      |
| `* * *`  | gym trainer  | record the workout plan for each client                        | ensure consistent progress through a consistent workout plan            |
| `* * *`  | gym trainer  | log a completed workout session, including exercises, weights, sets, and repetitions | review the client's training history                    |
| `* * *`  | gym trainer  | record the diet plan for each client                           | track the impact of diet on their improvement                           |
| `* * *`  | gym trainer  | delete a former client                                         | keep my active roster uncluttered                                       |
| `* * *`  | gym trainer  | record when and where to meet each client                      | remind myself of upcoming client sessions                               |
| `* *`    | gym trainer  | track the body progress of each client                         | see how effective my plan is                                            |
| `* *`    | gym trainer  | track fee payments                                             | check which clients have paid their fees                                |
| `* *`    | gym trainer  | search for clients by name                                     | find a particular client quickly                                        |
| `* *`    | gym trainer  | filter clients by training goal                                | work with clients who have similar objectives                           |
| `* *`    | gym trainer  | record a client's dietary restrictions                         | ensure their diet plan is suitable for them                             |
| `* *`    | gym trainer  | list clients with unpaid fees                                  | quickly identify which payments need to be followed up                  |
| `* *`    | gym trainer  | check whether a proposed session time conflicts with another client | avoid accidentally double-booking myself                           |
| `* *`    | gym trainer  | view all clients scheduled for a particular day                | quickly prepare for the clients I will be training that day             |
| `* *`    | gym trainer  | generate a concise training summary for a client               | brief a cover trainer without sharing unnecessary personal information  |
| `*`      | gym trainer  | edit a client's personal details                               | correct outdated or inaccurate information                             |
| `*`      | gym trainer  | search for clients by phone number or email                    | identify a client using their contact information                       |
| `*`      | gym trainer  | record each client's training goal                             | tailor their training towards a clear objective                         |
| `*`      | gym trainer  | record a client's injuries and physical limitations            | avoid exercises that may be unsafe for them                             |
| `*`      | gym trainer  | view a client's latest workout session                         | quickly know what they did previously before starting the next session  |
| `*`      | gym trainer  | view a client's complete workout history                       | review how their training has changed over time                         |
| `*`      | gym trainer  | edit a previously logged workout session                       | correct mistakes in weights, sets, repetitions, or exercises            |
| `*`      | gym trainer  | record a client's starting body measurements                   | have a baseline against which future progress can be compared           |
| `*`      | gym trainer  | view the history of a client's body measurements               | see how their body measurements have changed over time                  |
| `*`      | gym trainer  | compare a client's latest progress with their starting measurements | clearly show the client how much progress they have made            |
| `*`      | gym trainer  | record the number of sessions purchased by a client            | keep track of their training package                                    |
| `*`      | gym trainer  | view the number of sessions remaining in a client's package    | know when a client is close to needing a renewal                        |
| `*`      | gym trainer  | record a completed session against a client's package          | keep the remaining number of sessions accurate                          |
| `*`      | gym trainer  | list clients whose packages are running low                    | ask them about renewal before their package runs out                    |
| `*`      | gym trainer  | list clients I have not trained recently                       | identify clients who may be becoming inactive                           |

*{More to be added}*

### Use cases

(For all use cases below, the **System** is `Fittix` and the **Actor** is the `trainer`, unless specified otherwise)

**Use case: UC01 - Add a client**

**MSS**

1.  Trainer requests to add a new client, giving the client's details.
2.  Fittix adds the client and shows the newly added client's details.

    Use case ends.

**Extensions**

* 1a. A compulsory detail is missing, or a detail is in an invalid format.

    * 1a1. Fittix shows an error message stating the expected format.

      Use case resumes at step 1.

* 1b. A client with the same name and phone number already exists.

    * 1b1. Fittix shows a duplicate client error and does not add the client.

      Use case ends.

**Use case: UC02 - Delete a client**

**MSS**

1.  Trainer requests to list clients.
2.  Fittix shows a list of clients.
3.  Trainer requests to delete a specific client in the list.
4.  Fittix deletes the client and shows the deleted client's details.

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. Fittix shows an error message.

      Use case resumes at step 2.

**Use case: UC03 - Find a client by name**

**MSS**

1.  Trainer requests to find clients whose names match a given keyword.
2.  Fittix shows the list of matching clients.

    Use case ends.

**Extensions**

* 1a. No keyword is given.

    * 1a1. Fittix shows an error message stating the expected format.

      Use case resumes at step 1.

* 2a. No client matches the keyword.

    * 2a1. Fittix shows an empty list and states that no client matched.

      Use case ends.

**Use case: UC04 - Log a completed workout session**

**MSS**

1.  Trainer requests to list clients.
2.  Fittix shows a list of clients.
3.  Trainer requests to log a completed session for a specific client in the list, giving the exercise, weight, sets and repetitions.
4.  Fittix records the session against that client and shows the logged entry.

    Use case ends.

**Extensions**

* 3a. The given index is invalid.

    * 3a1. Fittix shows an error message.

      Use case resumes at step 2.

* 3b. The weight, sets or repetitions is not a positive number.

    * 3b1. Fittix shows an error message stating the expected format.

      Use case resumes at step 3.

**Use case: UC05 - View a client's workout history**

**MSS**

1.  Trainer requests to list clients.
2.  Fittix shows a list of clients.
3.  Trainer requests to view the workout history of a specific client in the list.
4.  Fittix shows that client's logged sessions, most recent first.

    Use case ends.

**Extensions**

* 3a. The given index is invalid.

    * 3a1. Fittix shows an error message.

      Use case resumes at step 2.

* 4a. The client has no logged sessions.

    * 4a1. Fittix states that no sessions have been logged for that client.

      Use case ends.

**Use case: UC06 - Record a workout plan for a client**

**MSS**

1.  Trainer requests to list clients.
2.  Fittix shows a list of clients.
3.  Trainer requests to set the workout plan of a specific client in the list.
4.  Fittix saves the plan as that client's current workout plan and shows the updated plan.

    Use case ends.

**Extensions**

* 3a. The given index is invalid.

    * 3a1. Fittix shows an error message.

      Use case resumes at step 2.

* 3b. The client already has a current workout plan.

    * 3b1. Fittix replaces the existing plan and states that the previous plan was overwritten.

      Use case ends.

**Use case: UC07 - Record a diet plan for a client**

**MSS**

1.  Trainer requests to list clients.
2.  Fittix shows a list of clients.
3.  Trainer requests to set the diet plan of a specific client in the list.
4.  Fittix saves the plan as that client's current diet plan and shows the updated plan.

    Use case ends.

**Extensions**

* 3a. The given index is invalid.

    * 3a1. Fittix shows an error message.

      Use case resumes at step 2.

* 3b. The client has recorded dietary restrictions.

    * 3b1. Fittix saves the plan and displays the client's dietary restrictions alongside it.

      Use case ends.

**Use case: UC08 - Schedule a session with a client**

**MSS**

1.  Trainer requests to list clients.
2.  Fittix shows a list of clients.
3.  Trainer requests to schedule a session for a specific client in the list, giving the date, time and location.
4.  Fittix saves the appointment and shows the client's updated schedule.

    Use case ends.

**Extensions**

* 3a. The date or time is in an invalid format, or is in the past.

    * 3a1. Fittix shows an error message stating the expected format.

      Use case resumes at step 3.

* 3b. The requested time overlaps an existing appointment.

    * 3b1. Fittix shows a clash warning naming the conflicting client and does not save the appointment.

      Use case ends.

### Non-Functional Requirements

**Technical and environment**

1.  Should work on any _mainstream OS_ as long as it has Java `25` or above installed, without requiring any other installation.
2.  Should be delivered as a single JAR file of at most 100MB that runs without an installer.
3.  Should work fully offline, with no dependency on a remote server or network connection.

**Performance and capacity**

4.  Should be able to hold up to 1000 clients without noticeable sluggishness in performance for typical usage.
5.  Every command should return a visible response within 2 seconds when the data file holds 1000 clients.
6.  A user with above average typing speed for regular English text (i.e. not code, not system admin commands) should be able to accomplish most of the tasks faster using commands than using the mouse.
7.  Logging a completed workout session should require a single command line, so that a user with above average typing speed can complete it in under 15 seconds between sets.

**Data and reliability**

8.  Data should be saved to disk after every command that modifies it, so an unexpected shutdown loses at most the command in progress.
9.  Data should be stored locally in a human editable text file, with no database management system.
10. If the data file is missing or unreadable, the app should start with an empty data set rather than fail to launch.

**Usability and privacy**

11. Error messages should state what was rejected and the expected format, so the user can correct the command without consulting the user guide.
12. The GUI should be usable at a screen resolution of 1280x720 and above, and remain readable at 150% display scaling.
13. Dates and times should be displayed in one consistent, unambiguous format throughout the app.
14. A shareable client summary should exclude _private contact details_ and the client's home address.

**Product scope constraints**

15. The product is for a single user; it should not support concurrent access to the same data file by multiple users.
16. The product should not require the user to log in or create an account.

### Glossary

* **Mainstream OS**: Windows, Linux, Unix, or macOS
* **Private contact detail**: A contact detail that is not meant to be shared with others
* **Client**: A person the trainer trains, stored as a record in Fittix
* **Session**: One completed training appointment, logged with the exercises, weights, sets and repetitions performed
* **Package**: A block of prepaid sessions (typically 10 or 20) bought by a client
* **Workout plan**: The client's current prescribed set of exercises; a client has at most one at a time
* **Diet plan**: The client's current prescribed eating guidance; a client has at most one at a time

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Double-click the JAR file.<br>
      Expected: The GUI opens with a set of sample contacts. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by double-clicking the JAR file.<br>
       Expected: The most recent window size and location are retained.

1. _{ more test cases …​ }_

### Deleting a person

1. Deleting a person while all persons are being shown

   1. Prerequisites: List all persons using the `list` command, with multiple persons in the list.

   1. Test case: `delete 1`<br>
      Expected: The first contact is deleted from the list. The status message shows the deleted contact's details.

   1. Test case: `delete 0`<br>
      Expected: No person is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

1. _{ more test cases …​ }_

### Saving data

1. Dealing with missing/corrupted data files

   1. _{Explain how to simulate missing or corrupted data files and state the expected behavior.}_

1. _{ more test cases …​ }_
