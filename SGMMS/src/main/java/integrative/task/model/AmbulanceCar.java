package integrative.task.model;

public class AmbulanceCar extends AutomataPoliceCar {

    public AmbulanceCar(String nameObject) {
        super(nameObject);
    }

    @Override
    public String frameUp() {
        return "/integrative/Ambulance/AmbulanceUp.png";
    }

    @Override
    public String frameDown() {
        return "/integrative/Ambulance/AmbulanceDown.png";
    }

    @Override
    public String frameLeft() {
        return "/integrative/Ambulance/AmbulanceLeft.png";
    }

    @Override
    public String frameRight() {
        return "/integrative/Ambulance/AmbulanceRight.png";
    }
}