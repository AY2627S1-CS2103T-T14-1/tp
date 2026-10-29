# Fittix

[![Java CI](https://github.com/AY2627S1-CS2103T-T14-1/tp/actions/workflows/gradle.yml/badge.svg)](https://github.com/AY2627S1-CS2103T-T14-1/tp/actions/workflows/gradle.yml)

Fittix is a desktop application for gym trainers to manage their clients and training work in one place. It is designed for trainers who need quick access to client information between sessions and prefer a command-driven workflow.

Fittix brings together:

* Client contact details, fitness goals, health information, and dietary restrictions
* Weekly workout plans and logs of completed sessions, including exercises, weights, sets, and repetitions
* Training appointments and meetings, scheduling conflict checks, and inactive-client monitoring
* Diet targets, notes, and body progress records
* Membership plans, remaining sessions, and payment records
* Undo and redo for changes to saved client and membership data

![Fittix workspace with demonstration client records](docs/images/Ui.png)

The screenshot shows the current development workspace using demonstration client records.

## Getting started

1. Install Java **25**. Mac users should follow the [course's Java installation instructions](https://se-education.org/guides/tutorials/javaInstallationMac.html).
2. Download the JAR from the **Assets** section of the [latest release](https://github.com/AY2627S1-CS2103T-T14-1/tp/releases/latest).
3. Place it in a folder where you have write permission and rename it to `fittix.jar`.
4. Open a terminal in that folder and run:

   ```text
   java -jar fittix.jar
   ```

Fittix stores its `data` folder and `preferences.json` in the terminal's current folder. Start it from the same folder each time to use the same client records. Follow the [User Guide's quick start](https://ay2627s1-cs2103t-t14-1.github.io/tp/UserGuide.html#quick-start) for the interface and example commands.

## Documentation

* [Product website](https://ay2627s1-cs2103t-t14-1.github.io/tp/)
* [User Guide](https://ay2627s1-cs2103t-t14-1.github.io/tp/UserGuide.html): installation, commands, examples, and troubleshooting
* [Developer Guide](https://ay2627s1-cs2103t-t14-1.github.io/tp/DeveloperGuide.html): setup, architecture, implementation, and testing
* [About Us](https://ay2627s1-cs2103t-t14-1.github.io/tp/AboutUs.html): the Fittix team

## Acknowledgements

This project is based on the AddressBook Level 3 project created by the [SE-EDU initiative](https://se-education.org).
