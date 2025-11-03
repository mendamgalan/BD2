import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CarParking {
    private static final int MAX_SIZE = 10;
    private Stack<Car> parking = new Stack<>();
    private Stack<Car> temp = new Stack<>();

    public List<String[]> input(String fileName) {
        List<String[]> commands = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2)
                    commands.add(parts);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return commands;
    }

    public void process(List<String[]> commands) {
        for (String[] command : commands) {
            String action = command[0];
            String carNum = command[1];
            Car car = new Car(carNum);

            if (action.equalsIgnoreCase("A")) {
                arrival(car);
            } else if (action.equalsIgnoreCase("D")) {
                departure(car);
            }
        }
    }

    private void arrival(Car car) {
        System.out.print("Arrival " + car.plate + " -> ");
        if (parking.size() < MAX_SIZE) {
            parking.push(car);
            System.out.println("There is room.");
        } else {
            System.out.println("Garage full, this car cannot enter.");
        }
    }

    private void departure(Car car) {
        System.out.print("Departure " + car.plate + " -> ");
        if (parking.isEmpty()) {
            System.out.println("Garage is empty.");
            return;
        }

        int moved = 0;
        boolean found = false;

        while (!parking.isEmpty()) {
            Car top = parking.pop();
            if (top.plate.equals(car.plate)) {
                found = true;
                break;
            } else {
                temp.push(top);
                moved++;
            }
        }

        if (found) {
            System.out.println(moved + " cars moved out.");
        } else {
            System.out.println("This car not in the garage.");
        }

        while (!temp.isEmpty()) {
            parking.push(temp.pop());
        }
    }

    public void output() {
        System.out.println("\nCars currently in garage:");
        if (parking.isEmpty()) {
            System.out.println("None.");
        } else {
            for (Car c : parking) {
                System.out.println(" - " + c.plate);
            }
        }
    }
}
