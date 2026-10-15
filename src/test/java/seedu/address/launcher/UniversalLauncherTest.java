package seedu.address.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class UniversalLauncherTest {
    @TempDir
    public Path temporaryDirectory;

    @Test
    public void platformFor_supportedOperatingSystems_selectsMatchingNatives() {
        assertEquals("win", UniversalLauncher.platformFor("Windows 11", "amd64"));
        assertEquals("win", UniversalLauncher.platformFor("Windows 10", "x86_64"));
        assertEquals("mac", UniversalLauncher.platformFor("Mac OS X", "x86_64"));
        assertEquals("mac-aarch64", UniversalLauncher.platformFor("Mac OS X", "aarch64"));
        assertEquals("mac-aarch64", UniversalLauncher.platformFor("Darwin", "arm64"));
        assertEquals("linux", UniversalLauncher.platformFor("Linux", "amd64"));
        assertEquals("linux-aarch64", UniversalLauncher.platformFor("Linux", "aarch64"));
    }

    @Test
    public void platformFor_unsupportedOperatingSystems_rejectsInsteadOfLoadingWrongNatives() {
        assertThrows(IllegalStateException.class, () -> UniversalLauncher.platformFor("Windows 11", "aarch64"));
        assertThrows(IllegalStateException.class, () -> UniversalLauncher.platformFor("Linux", "x86"));
        assertThrows(IllegalStateException.class, () -> UniversalLauncher.platformFor("FreeBSD", "amd64"));
    }

    @Test
    public void extractLibraries_selectsOnlyCommonAndHostPlatform_inDirectoryWithSpaces() throws Exception {
        Path archive = archive(List.of("lib/common/fittix-app.jar", "lib/common/jackson-core.jar",
                "lib/win/javafx-graphics.jar", "lib/mac/javafx-graphics.jar", "lib/linux/javafx-graphics.jar"));
        Path destination = Files.createDirectory(temporaryDirectory.resolve("runtime with spaces"));
        var libraries = UniversalLauncher.extractLibraries(archive, destination, "win");
        assertEquals(3, libraries.size());
        assertEquals("lib/win/javafx-graphics.jar", Files.readString(destination.resolve("javafx-graphics.jar")));
        assertTrue(libraries.stream().allMatch(url -> url.toString().contains("runtime%20with%20spaces")));
    }

    @Test
    public void extractLibraries_missingApplicationOrPlatform_failsClearly() throws Exception {
        Path missingPlatform = archive(List.of("lib/common/fittix-app.jar", "lib/mac/javafx.jar"));
        Path destination = Files.createDirectory(temporaryDirectory.resolve("missing-platform"));
        assertThrows(IOException.class, () -> UniversalLauncher.extractLibraries(missingPlatform, destination, "win"));
        Path missingApplication = archive(List.of("lib/common/jackson-core.jar", "lib/win/javafx.jar"));
        Path secondDestination = Files.createDirectory(temporaryDirectory.resolve("missing-app"));
        assertThrows(IOException.class, () -> UniversalLauncher.extractLibraries(
                missingApplication, secondDestination, "win"));
    }

    private Path archive(List<String> entries) throws IOException {
        Path archive = Files.createTempFile(temporaryDirectory, "release-", ".jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(archive))) {
            for (String name : entries) {
                output.putNextEntry(new JarEntry(name));
                output.write(name.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                output.closeEntry();
            }
        }
        return archive;
    }
}
