package seedu.address.launcher;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Standalone smoke test with no application or JavaFX dependencies on its initial classpath.
 * Loads the release manifest entry point, waits for a real JavaFX window, then closes it.
 */
public final class UniversalJarSmokeTest {
    private UniversalJarSmokeTest() {
    }

    /** Runs in a separate JVM so JavaFX startup and exit cannot affect the unit-test process. */
    public static void main(String[] args) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        CountDownLatch windowShown = new CountDownLatch(1);
        URL archive = Path.of(args[0]).toUri().toURL();
        try (URLClassLoader bootstrap = new URLClassLoader(new URL[] {archive},
                ClassLoader.getPlatformClassLoader())) {
            Thread launch = new Thread(() -> {
                try {
                    bootstrap.loadClass("seedu.address.launcher.UniversalLauncher")
                            .getMethod("main", String[].class).invoke(null, (Object) new String[0]);
                } catch (Throwable exception) {
                    failure.set(exception);
                }
            }, "fittix-release-smoke");
            launch.setDaemon(true);
            launch.start();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(45);
            while (windowShown.getCount() != 0 && System.nanoTime() < deadline && failure.get() == null) {
                for (Thread thread : Thread.getAllStackTraces().keySet()) {
                    if (thread.getName().equals("JavaFX Application Thread")) {
                        requestWindowClose(thread.getContextClassLoader(), windowShown, failure);
                    }
                }
                Thread.sleep(100);
            }
            if (failure.get() != null || windowShown.getCount() != 0) {
                failure.getAndUpdate(value -> value == null
                        ? new AssertionError("No application window opened") : value);
            } else {
                launch.join(10_000);
                if (launch.isAlive()) {
                    failure.set(new AssertionError("Application did not exit after its window closed"));
                }
            }
        }
        if (failure.get() != null) {
            failure.get().printStackTrace();
            System.exit(1);
        }
        System.out.println("Universal JAR smoke test passed: application window opened and closed.");
        System.exit(0);
    }

    private static void requestWindowClose(ClassLoader loader, CountDownLatch windowShown,
            AtomicReference<Throwable> failure) throws Exception {
        Class<?> platform = loader.loadClass("javafx.application.Platform");
        Class<?> window = loader.loadClass("javafx.stage.Window");
        platform.getMethod("runLater", Runnable.class).invoke(null, (Runnable) () -> {
            try {
                List<?> windows = new ArrayList<>((List<?>) window.getMethod("getWindows").invoke(null));
                if (!windows.isEmpty()) {
                    for (Object stage : windows) {
                        window.getMethod("hide").invoke(stage);
                    }
                    windowShown.countDown();
                }
            } catch (ReflectiveOperationException exception) {
                failure.set(exception);
            }
        });
    }
}
