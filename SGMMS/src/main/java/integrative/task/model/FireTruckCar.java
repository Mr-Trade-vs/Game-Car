package integrative.task.model;

public class FireTruckCar extends AutomataPoliceCar {

    public FireTruckCar(String nameObject) {
        super(nameObject);
    }

    @Override
    public String frameUp() {
        return "/integrative/fireTruck/TruckUp.png";
    }

    @Override
    public String frameDown() {
        return "/integrative/fireTruck/TruckDown.png";
    }

    @Override
    public String frameLeft() {
        return "/integrative/fireTruck/TruckLeft.png";
    }

    @Override
    public String frameRight() {
        return "/integrative/fireTruck/TruckRight.png";
    }
}