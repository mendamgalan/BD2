public class Car {
    public String plate;

    public Car(String plate) {
        this.plate = plate;
    }

    @Override
    public String toString() {
        return this.plate;
    }
}
