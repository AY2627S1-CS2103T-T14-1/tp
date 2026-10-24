package seedu.address.launcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Starts the release with only the native JavaFX libraries for the current platform.
 * Keeping platform JARs separate prevents identically named native libraries from overwriting each other.
 */
public final class UniversalLauncher {
    private UniversalLauncher() {
    }

    /** Extracts the bundled runtime and forwards all arguments to the application entry point. */
    public static void main(String[] args) throws Exception {
        String platform = platformFor(System.getProperty("os.name"), System.getProperty("os.arch"));
        Path archive = Path.of(UniversalLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        Path directory = Files.createTempDirectory("fittix-libs-");
        directory.toFile().deleteOnExit();
        List<URL> libraries = extractLibraries(archive, directory, platform);
        // This loader must stay open while JavaFX is running: resources and classes load lazily.
        URLClassLoader loader = new URLClassLoader(libraries.toArray(URL[]::new), ClassLoader.getPlatformClassLoader());
        // Configure application assertions before any application classes are initialized.
        loader.setPackageAssertionStatus("seedu.address", true);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> closeLoader(loader), "fittix-runtime-cleanup"));
        Thread.currentThread().setContextClassLoader(loader);
        try {
            Class.forName("seedu.address.Main", true, loader).getMethod("main", String[].class)
                    .invoke(null, (Object) args);
        } catch (InvocationTargetException exception) {
            if (exception.getCause() instanceof Exception cause) {
                throw cause;
            }
            throw exception;
        }
    }

    static String platformFor(String osName, String osArch) {
        String os = osName.toLowerCase(Locale.ROOT);
        String arch = osArch.toLowerCase(Locale.ROOT);
        boolean arm = arch.equals("aarch64") || arch.equals("arm64");
        boolean x64 = arch.equals("amd64") || arch.equals("x86_64");
        if (os.startsWith("windows") && x64) {
            return "win";
        }
        if ((os.startsWith("mac") || os.equals("darwin")) && (arm || x64)) {
            return arm ? "mac-aarch64" : "mac";
        }
        if (os.equals("linux") && (arm || x64)) {
            return arm ? "linux-aarch64" : "linux";
        }
        throw new IllegalStateException("Unsupported Fittix platform: " + osName + " (" + osArch + ")");
    }

    static List<URL> extractLibraries(Path archive, Path directory, String platform) throws IOException {
        List<URL> libraries = new ArrayList<>();
        boolean hasPlatformLibraries = false;
        boolean hasApplication = false;
        try (JarFile jar = new JarFile(archive.toFile())) {
            for (JarEntry entry : jar.stream().toList()) {
                String name = entry.getName();
                boolean common = name.startsWith("lib/common/");
                boolean nativeLibrary = name.startsWith("lib/" + platform + "/");
                if (entry.isDirectory() || !name.endsWith(".jar") || !(common || nativeLibrary)) {
                    continue;
                }
                // Use only the basename, so archive paths cannot escape the temporary directory.
                Path target = directory.resolve(Path.of(name).getFileName());
                try (var input = jar.getInputStream(entry)) {
                    Files.copy(input, target);
                }
                target.toFile().deleteOnExit();
                libraries.add(target.toUri().toURL());
                hasPlatformLibraries |= nativeLibrary;
                hasApplication |= name.equals("lib/common/fittix-app.jar");
            }
        }
        if (!hasApplication || !hasPlatformLibraries) {
            throw new IOException("Incomplete Fittix runtime for " + platform + " in " + archive);
        }
        return libraries;
    }

    private static void closeLoader(URLClassLoader loader) {
        try {
            loader.close();
        } catch (IOException exception) {
            System.err.println("Could not close the Fittix runtime: " + exception.getMessage());
        }
    }
}
