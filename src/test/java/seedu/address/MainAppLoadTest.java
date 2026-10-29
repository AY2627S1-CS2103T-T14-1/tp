@ResourceLock("java.util.TimeZone.default")
public class MainAppLoadTest {
    @TempDir
    public Path folder;

    @Test
    public void initModelManager_missingFile_usesSampleDataWithoutWriting() {
        StorageManager storage = storage();
        assertFalse(new MainApp().initModelManager(storage, new UserPrefs())
                .getAddressBook().getPersonList().isEmpty());
        assertFalse(Files.exists(storage.getAddressBookFilePath()));
    }

    @Test
    public void initModelManager_malformedFile_stopsAndPreservesOriginalBytes() throws Exception {
        StorageManager storage = storage();
        Path dataFile = storage.getAddressBookFilePath();
        Files.writeString(dataFile, "not json");
        byte[] original = Files.readAllBytes(dataFile);
        MainApp app = new MainApp();
        app.storage = storage;

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                app.initModelManager(storage, new UserPrefs()));
        app.stop();

        assertTrue(exception.getMessage().contains("protect the existing file"));
        assertTrue(exception.getMessage().contains(dataFile.toString()));
        assertArrayEquals(original, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_workoutBecomesFutureAfterZoneChange_preservesRecoverableRoster() throws Exception {
        TimeZone originalZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            LocalDate recordedDate = LocalDate.now();
            WorkoutSession session = new WorkoutSession(recordedDate, Optional.empty(),
                    List.of(new WorkoutLogEntry("Squat", new BigDecimal("60"), 3, 10)));
            AddressBook original = new AddressBook();
            original.addPerson(new PersonBuilder().withWorkoutSessions(List.of(session)).build());
            original.addPerson(new PersonBuilder().withName("Other Client").build());
            StorageManager storage = storage();
            storage.saveAddressBook(original);
            byte[] originalBytes = Files.readAllBytes(storage.getAddressBookFilePath());

            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Pago_Pago"));
            assertTrue(LocalDate.now().isBefore(recordedDate));
            assertThrows(IllegalStateException.class, () ->
                    new MainApp().initModelManager(storage, new UserPrefs()));
            assertArrayEquals(originalBytes, Files.readAllBytes(storage.getAddressBookFilePath()));

            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Kiritimati"));
            assertEquals(original, new MainApp().initModelManager(storage, new UserPrefs()).getAddressBook());
        } finally {
            TimeZone.setDefault(originalZone);
        }
    }

    @Test
    public void initModelManager_savedPhoneLongerThanFifteenDigits_stopsAndPreservesOriginalBytes() throws Exception {
        StorageManager storage = storage();
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withPhone("85355255").build());
        storage.saveAddressBook(original);
        Path dataFile = storage.getAddressBookFilePath();
        // A roster saved before the 15-digit limit can still hold a longer phone number.
        Files.writeString(dataFile, Files.readString(dataFile).replace("\"85355255\"", "\"1234567890123456\""));
        assertTrue(Files.readString(dataFile).contains("\"1234567890123456\""));
        byte[] legacyBytes = Files.readAllBytes(dataFile);

        assertThrows(IllegalStateException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));
        assertArrayEquals(legacyBytes, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_savedCaseOnlyDuplicateClients_stopsAndPreservesOriginalBytes() throws Exception {
        StorageManager storage = storage();
        Path dataFile = storage.getAddressBookFilePath();
        // A roster saved before names were compared without regard to case can hold such duplicates.
        Files.copy(Path.of("src", "test", "data", "JsonSerializableAddressBookTest",
                "caseOnlyDuplicatePersonAddressBook.json"), dataFile);
        byte[] legacyBytes = Files.readAllBytes(dataFile);

        assertThrows(IllegalStateException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));
        assertArrayEquals(legacyBytes, Files.readAllBytes(dataFile));
    }

    @Test
    public void initModelManager_savedSingleLabelEmailDomain_stopsAndPreservesOriginalBytes() throws Exception {
        StorageManager storage = storage();
        AddressBook original = new AddressBook();
        original.addPerson(new PersonBuilder().withEmail("amy@gmail.com").build());
        storage.saveAddressBook(original);
        Path dataFile = storage.getAddressBookFilePath();
        // A roster saved before the two-label rule can still hold an email such as alpha@example.
        Files.writeString(dataFile, Files.readString(dataFile).replace("\"amy@gmail.com\"", "\"alpha@example\""));
        assertTrue(Files.readString(dataFile).contains("\"alpha@example\""));
        byte[] legacyBytes = Files.readAllBytes(dataFile);

        assertThrows(IllegalStateException.class, () -> new MainApp().initModelManager(storage, new UserPrefs()));
        assertArrayEquals(legacyBytes, Files.readAllBytes(dataFile));
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(folder.resolve("clients.json")),
                new JsonUserPrefsStorage(folder.resolve("preferences.json")));
    }
}
