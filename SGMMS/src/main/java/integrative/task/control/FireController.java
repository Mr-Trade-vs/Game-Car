package integrative.task.control;

import integrative.task.model.Fire;
import integrative.task.model.FireThread;
import integrative.task.model.Intersection;
import integrative.task.structures.BinaryTreeIncident;

import java.util.ArrayList;
import java.util.List;

public class FireController {
    private Fire fire;

    private List<Intersection> intersections;
    private BinaryTreeIncident incidentTree;
    private FireThread fireThread; // Añadir referencia al thread
    private CarController carController; // Reference to the CarController for creating Help buttons

    public FireController(BinaryTreeIncident incidentTree) {
        this(incidentTree, new ArrayList<>(), null);
    }

    public FireController(BinaryTreeIncident incidentTree, List<Intersection> intersections) {
        this(incidentTree, intersections, null);
    }

    public FireController(BinaryTreeIncident incidentTree, List<Intersection> intersections, CarController carController) {
        this.incidentTree = incidentTree;
        this.intersections = new ArrayList<>(intersections);
        this.fire = new Fire("Fire");
        this.carController = carController;
    }

    public List<Fire> getFires() {
        if (fireThread != null) {
            return fireThread.getFires();
        }
        return new ArrayList<>();
    }

    public FireThread startFireThread() {
        // If we have intersections, use them instead of buildings
        if (intersections != null && !intersections.isEmpty()) {
            fireThread = new FireThread(fire, intersections, incidentTree, carController);
        }

        Thread thread = new Thread(fireThread);
        thread.start();
        return fireThread;
    }

    public void stopFireThread() {
        if (fireThread != null) {
            fireThread.stop();
        }
    }
}
