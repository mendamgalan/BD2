import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CarParkingTest {
    private CarParking carParking;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;

    @BeforeEach
    public void setUp() {
        carParking = new CarParking();
        outputStream = new ByteArrayOutputStream();
        originalOut = System.out;
        System.setOut(new PrintStream(outputStream));
    }

    @Test
    public void testCarCreation() {
        Car car = new Car("ABC123");
        assertEquals("ABC123", car.plate);
        assertEquals("ABC123", car.toString());
    }

    @Test
    public void testArrivalWithRoom() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "ABC123"});

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Arrival ABC123"));
        assertTrue(output.contains("There is room"));
    }

    @Test
    public void testArrivalGarageFull() {
        List<String[]> commands = new ArrayList<>();

        // Fill the garage with 10 cars
        for (int i = 0; i < 10; i++) {
            commands.add(new String[]{"A", "CAR" + i});
        }
        // Try to add 11th car
        commands.add(new String[]{"A", "EXTRA"});

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Arrival EXTRA"));
        assertTrue(output.contains("Garage full, this car cannot enter"));
    }

    @Test
    public void testDepartureCarAtTop() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "ABC123"});
        commands.add(new String[]{"A", "DEF456"});
        commands.add(new String[]{"D", "DEF456"}); // Top car

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Departure DEF456"));
        assertTrue(output.contains("0 cars moved out"));
    }

    @Test
    public void testDepartureCarInMiddle() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "CAR1"});
        commands.add(new String[]{"A", "CAR2"});
        commands.add(new String[]{"A", "CAR3"});
        commands.add(new String[]{"D", "CAR2"}); // Middle car

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Departure CAR2"));
        assertTrue(output.contains("1 cars moved out"));
    }

    @Test
    public void testDepartureCarNotInGarage() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "ABC123"});
        commands.add(new String[]{"D", "XYZ999"}); // Not present

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Departure XYZ999"));
        assertTrue(output.contains("This car not in the garage"));
    }

    @Test
    public void testDepartureFromEmptyGarage() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"D", "ABC123"});

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Departure ABC123"));
        assertTrue(output.contains("Garage is empty"));
    }

    @Test
    public void testOutputEmptyGarage() {
        carParking.output();
        String output = outputStream.toString();

        assertTrue(output.contains("Cars currently in garage:"));
        assertTrue(output.contains("None."));
    }

    @Test
    public void testOutputWithCars() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "CAR1"});
        commands.add(new String[]{"A", "CAR2"});

        carParking.process(commands);
        outputStream.reset(); // Clear previous output
        carParking.output();
        String output = outputStream.toString();

        assertTrue(output.contains("Cars currently in garage:"));
        assertTrue(output.contains("CAR1"));
        assertTrue(output.contains("CAR2"));
    }

    @Test
    public void testInputFromFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary file with test commands
        Path testFile = tempDir.resolve("test_cars.txt");
        String content = "A ABC123\nD DEF456\nA GHI789\n";
        Files.writeString(testFile, content);

        List<String[]> commands = carParking.input(testFile.toString());

        assertEquals(3, commands.size());
        assertArrayEquals(new String[]{"A", "ABC123"}, commands.get(0));
        assertArrayEquals(new String[]{"D", "DEF456"}, commands.get(1));
        assertArrayEquals(new String[]{"A", "GHI789"}, commands.get(2));
    }

    @Test
    public void testInputInvalidFile() {
        List<String[]> commands = carParking.input("nonexistent_file.txt");

        assertTrue(commands.isEmpty());
        String output = outputStream.toString();
        assertTrue(output.contains("Error reading file"));
    }

    @Test
    public void testMultipleArrivalsAndDepartures() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "CAR1"});
        commands.add(new String[]{"A", "CAR2"});
        commands.add(new String[]{"A", "CAR3"});
        commands.add(new String[]{"D", "CAR1"}); // Bottom car, 2 moved
        commands.add(new String[]{"A", "CAR4"});
        commands.add(new String[]{"D", "CAR4"}); // Top car, 0 moved

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("2 cars moved out"));
        assertTrue(output.contains("0 cars moved out"));
    }

    @Test
    public void testCaseInsensitiveCommands() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"a", "CAR1"}); // lowercase 'a'
        commands.add(new String[]{"d", "CAR1"}); // lowercase 'd'

        carParking.process(commands);
        String output = outputStream.toString();

        assertTrue(output.contains("Arrival CAR1"));
        assertTrue(output.contains("Departure CAR1"));
    }

    @Test
    public void testExactlyTenCars() {
        List<String[]> commands = new ArrayList<>();

        // Add exactly 10 cars
        for (int i = 0; i < 10; i++) {
            commands.add(new String[]{"A", "CAR" + i});
        }

        carParking.process(commands);
        String output = outputStream.toString();

        // All 10 should be accepted
        for (int i = 0; i < 10; i++) {
            assertTrue(output.contains("Arrival CAR" + i));
        }
        assertFalse(output.contains("Garage full"));
    }

    @Test
    public void testDepartureRestoresOrder() {
        List<String[]> commands = new ArrayList<>();
        commands.add(new String[]{"A", "CAR1"}); // Bottom
        commands.add(new String[]{"A", "CAR2"});
        commands.add(new String[]{"A", "CAR3"}); // Top
        commands.add(new String[]{"D", "CAR1"}); // Remove bottom

        carParking.process(commands);
        outputStream.reset();
        carParking.output();
        String output = outputStream.toString();

        // CAR2 and CAR3 should still be present
        assertTrue(output.contains("CAR2"));
        assertTrue(output.contains("CAR3"));
        assertFalse(output.contains("CAR1"));
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }
}