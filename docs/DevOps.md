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

* **`shadowJar`**: Uses the Shadow plugin to create a platform-specific development fat JAR under `build/libs`. Use `universalJar` for the multi-platform release package.<br>
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

1. Update the version in [`MainApp.java`](https://github.com/AY2627S1-CS2103T-T14-1/tp/blob/master/src/main/java/seedu/address/MainApp.java), the guides and screenshots. Remove unfinished template text and check that feature descriptions match the app. Commit and merge these changes before selecting the release commit.
1. Use a clean checkout of that final commit. Run `java -version` and confirm Java 25. Run `./gradlew clean check coverage shadowJar universalJarSmokeTest` (`gradlew.bat clean check coverage shadowJar universalJarSmokeTest` on Windows). On headless Linux, prefix the Gradle command with `xvfb-run -a`.
1. Confirm CI passes on Windows, macOS and Linux. `shadowJar` performs the generic fat-JAR build described by the course guide; Fittix's `universalJarSmokeTest` also builds and tests the **multi-platform release file `build/libs/fittix-v1.jar`**. Use that universal JAR for the release.
1. In a separate writable folder, launch the JAR using `java -jar "fittix-v1.jar"`. Try sample commands, check saved data, exit, and relaunch from the same folder. Record the final commit and JAR checksum.
1. Tag the exact tested commit with the release version, for example `v1.5`, and push that tag. The tag, uploaded JAR and online guides must describe the same product version.
1. [Create the GitHub release](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository). Upload **`build/libs/fittix-v1.jar`** from that commit and include release notes and the current screenshot. Keep this filename so the User Guide launch command remains valid. Publish the release as the latest non-draft release so the website's download link selects it.
1. Download the uploaded asset into another fresh folder. Compare its checksum with the tested JAR and repeat a launch check. Confirm the website, UG and DG are deployed, then close or reschedule remaining milestone work and close the milestone.
1. If correcting an already published release, preserve the relationship between tag, source and asset. Changing only release text or uploading a different JAR does not update the tagged source.
