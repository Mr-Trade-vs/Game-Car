package integrative.task.control;

import integrative.task.model.Bomb;
import integrative.task.model.BombThread;
import integrative.task.model.Intersection;
import integrative.task.structures.BinaryTreeIncident;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for managing bomb incidents
 */
public class BombController {
    private Bomb bomb;
    private List<Intersection> intersections;
    private BinaryTreeIncident incidentTree;
    private BombThread bombThread;
    private CarController carController; // Reference to the CarController for creating Help buttons

    public BombController(BinaryTreeIncident incidentTree) {
        this(incidentTree, new ArrayList<>(), null);
    }

    public BombController(BinaryTreeIncident incidentTree, List<Intersection> intersections) {
        this(incidentTree, intersections, null);
    }

    public BombController(BinaryTreeIncident incidentTree, List<Intersection> intersections, CarController carController) {
        this.incidentTree = incidentTree;
        this.intersections = new ArrayList<>(intersections);
        this.bomb = new Bomb("Bomb");
        this.carController = carController;
    }

    public List<Bomb> getBombs() {
        if (bombThread != null) {
            return bombThread.getBombs();
        }
        return new ArrayList<>();
    }

    public BombThread startBombThread() {
        // Create and start the bomb thread
        if (intersections != null && !intersections.isEmpty()) {
            bombThread = new BombThread(bomb, intersections, incidentTree, carController);
        }

        Thread thread = new Thread(bombThread);
        thread.start();
        return bombThread;
    }

    public void stopBombThread() {
        if (bombThread != null) {
            bombThread.stop();
        }
    }
}