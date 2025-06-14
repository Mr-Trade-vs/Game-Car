package integrative.task.model;

import java.util.HashMap;

public class AutomataPoliceCar extends AutomataCar {

    public AutomataPoliceCar(String nameObject) {
        // Call the parent constructor
        super(nameObject);

        System.out.println("[DEBUG] Creating AutomataPoliceCar: " + nameObject);

        // Override the animations with police car images
        animations = new HashMap<>();
        animations.put("right", new Animation(frameRight(), 50, 0, 35, 32));
        animations.put("left", new Animation(frameLeft(), 50, 0, 35, 32));
        animations.put("up", new Animation(frameUp(), 50, 0, 20, 35));
        animations.put("down", new Animation(frameDown(), 50, 0, 20, 35));

        System.out.println("[DEBUG] Police car animations created");
        System.out.println("[DEBUG] Right frame: " + frameRight());
        System.out.println("[DEBUG] Left frame: " + frameLeft());
        System.out.println("[DEBUG] Up frame: " + frameUp());
        System.out.println("[DEBUG] Down frame: " + frameDown());

        // Explicitly set the image for the police car
        setImage(frameRight());
        System.out.println("[DEBUG] Police car image set to: " + frameRight());

        // Set initial direction
        super.changeDirection("right");
        System.out.println("[DEBUG] Police car initial direction set to: right");
        System.out.println("[DEBUG] Police car creation complete");
    }

    @Override
    public String frameUp() {
        return "/integrative/police/PoliceUp.png";
    }

    @Override
    public String frameDown() {
        return "/integrative/police/PoliceDown.png";
    }

    @Override
    public String frameLeft() {
        return "/integrative/police/PoliceLeft.png";
    }

    @Override
    public String frameRight() {
        return "/integrative/police/PoliceRight.png";
    }
}
