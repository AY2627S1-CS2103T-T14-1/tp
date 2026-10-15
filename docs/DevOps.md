---
layout: page
title: DevOps guide
---

* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## Build automation

This project uses Gradle for **build automation and dependency management**. **We recommend reading [this Gradle tutorial from se-edu/guides](https://se-education.org/guides/tutorials/gradle.html).**


The following commands perform common Gradle tasks.


* **`clean`**: Deletes the files created during the previous build tasks (e.g. files in the `build` folder).<br>
  For example: `./gradlew clean`

* **`shadowJar`**: Uses the Shadow plugin to create the fat JAR file `build/libs/addressbook.jar`.<br>
  For example: `./gradlew shadowJar`

* **`universalJar`**: Creates the release JAR `build/libs/fittix-v1.jar` for Windows x64, macOS Intel/Apple silicon, and Linux x64/ARM64.<br>
  Run `./gradlew universalJar` (`gradlew.bat universalJar` on Windows), then `java -jar build/libs/fittix-v1.jar`.
  The launcher source is in `src/main/java/seedu/address/launcher/UniversalLauncher.java`; packaging is defined in `gradle/universal-jar.gradle`.
  The release bundles JavaFX **25** for each supported platform, selecting only the host platform at startup. Development tasks retain JavaFX **17.0.7** and their existing test setup. Both require Java 25 or later.

* **`universalJarSmokeTest`**: Builds the universal JAR, opens its actual application window using only the bundled runtime, then closes it.<br>
  Run `./gradlew universalJarSmokeTest` (`gradlew.bat universalJarSmokeTest` on Windows). Linux without a desktop requires `xvfb-run -a ./gradlew universalJarSmokeTest`.
  Test client data is isolated in `build/universal-smoke/`.

* **`run`**: Builds and runs the application.<br>
  **`runShadow`**: Builds the application as a fat JAR, then runs it.

* **`checkstyleMain`**: Runs the code style check for the main code base.<br>
  **`checkstyleTest`**: Runs the code style check for the test code base.

* **`test`**
  * `./gradlew test`: Runs all tests.
  * `./gradlew clean test`: Cleans the project before running all tests

--------------------------------------------------------------------------------------------------------------------

## Continuous integration (CI)

This project uses GitHub Actions for CI. The necessary workflow configuration files are in `.github/workflows`. No further setup is required.

### Code coverage

As part of CI, Gradle generates JaCoCo coverage reports from the tests, and CI uploads the coverage data to Codecov. Codecov then provides information about test coverage.

However, because Codecov is known to run into intermittent problems (e.g., report upload fails) due to issues on the Codecov service side, the CI is configured to pass even if the Codecov task failed. Therefore, developers are advised to check the code coverage levels periodically and take corrective actions if the coverage level falls below desired levels.

To enable Codecov for forks of this project, follow the steps given in [this se-edu guide](https://se-education.org/guides/tutorials/codecov.html).

### Repository-wide checks

In addition to Gradle checks, CI runs repository-wide checks. Unlike Gradle checks, which cover files used in the build, these checks cover every repository file and enforce rules that are hard to apply on development machines, such as line-ending requirements.

These checks are implemented as POSIX shell scripts, and thus can only be run on POSIX-compliant operating systems such as macOS and Linux. To run all checks locally on these operating systems, execute the following in the repository root directory:

`./.github/run-checks.sh`

Any warnings or errors will be printed out to the console.

**If adding new checks:**

* Checks are implemented as executable `check-*` scripts within the `.github` directory. The `run-checks.sh` script will automatically pick up and run files named as such. That is, you can add more such files if you need and the CI will do the rest.

* Check scripts should print out errors in the format `SEVERITY:FILENAME:LINE: MESSAGE`
  * SEVERITY is either ERROR or WARN.
  * FILENAME is the path to the file relative to the current directory.
  * LINE is the line of the file where the error occurred and MESSAGE is the message explaining the error.

* Check scripts must exit with a non-zero exit code if any errors occur.

--------------------------------------------------------------------------------------------------------------------

## Making a release

Here are the steps to create a new release.

1. Update the version number in [`MainApp.java`](https://github.com/AY2627S1-CS2103T-T14-1/tp/blob/master/src/main/java/seedu/address/MainApp.java).
1. Run `./gradlew clean check universalJarSmokeTest` (`gradlew.bat clean check universalJarSmokeTest` on Windows). On headless Linux, prefix the command with `xvfb-run -a`.
1. Confirm the GitHub CI checks pass on Windows, macOS and Linux. Manually try the JAR with sample commands before publishing.
1. Commit the source and packaging scripts, then tag that commit with the release version, for example `v1.3`.
1. [Create a new release using GitHub](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository). Upload **`build/libs/fittix-v1.jar`** from that commit and include the release notes and screenshots. The JAR's filename is kept stable so the user guide's launch command remains valid.
1. If replacing an earlier release, ensure the release tag points to the commit containing the packaging task and launcher. Changing only the release description or asset does not update the tagged source. Download the uploaded asset and repeat a launch check.
