package integrative.task.model;

import integrative.task.control.CarController;
import integrative.task.structures.BinaryTreeIncident;
import integrative.task.structures.NodeGraph;
import javafx.application.Platform;
import javafx.scene.shape.Rectangle;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class FireThread implements Runnable {
    private Fire fire;
    private List<Intersection> intersections;
    private BinaryTreeIncident incidentTree;
    private List<Fire> fires;
    private volatile boolean running;
    private Random random;
    private long lastPropagationTime;
    private long lastAnimationUpdate;
    private boolean useIntersections;
    private CarController carController; // Reference to the CarController for creating Help buttons

    // Usar CopyOnWriteArrayList para thread safety
    private final List<Fire> threadSafeFires;


    // Constructor for intersections (new implementation)
    public FireThread(Fire fire, List<Intersection> intersections, BinaryTreeIncident incidentTree) {
        this(fire, intersections, incidentTree, null);
    }

    // Constructor with CarController for creating Help buttons
    public FireThread(Fire fire, List<Intersection> intersections, BinaryTreeIncident incidentTree, CarController carController) {
        this.fire = fire;
        this.intersections = intersections;
        this.useIntersections = true;
        this.incidentTree = incidentTree;
        this.carController = carController;
        this.threadSafeFires = new CopyOnWriteArrayList<>();
        this.fires = Collections.synchronizedList(new ArrayList<>());
        this.fires.add(fire);
        this.threadSafeFires.add(fire);
        this.running = true;
        this.random = new Random();
        this.lastPropagationTime = System.currentTimeMillis();
        this.lastAnimationUpdate = System.currentTimeMillis();
    }

    @Override
    public void run() {
        System.out.println("FireThread started");
        startFire();

        while (running) {
            try {
                long currentTime = System.currentTimeMillis();

                // Actualizar animaciones cada 100ms
                if (currentTime - lastAnimationUpdate >= 100) {
                    updateAllFireAnimations();
                    lastAnimationUpdate = currentTime;
                }

                // Propagar fuego cada 10 segundos (reducido para testing)
                if (currentTime - lastPropagationTime >= 10000) {
                    propagateFire();
                    lastPropagationTime = currentTime;
                }

                // Sleep más corto para mejor respuesta
                Thread.sleep(50);

            } catch (InterruptedException e) {
                System.out.println("FireThread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in FireThread: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("FireThread stopped");
    }

    private void updateAllFireAnimations() {
        try {
            for (Fire f : threadSafeFires) {
                if (f != null) {
                    f.updateAnimation();
                }
            }
        } catch (Exception e) {
            System.err.println("Error updating fire animations: " + e.getMessage());
        }
    }

    public void startFire() {
        try {
            Rectangle bounds = checkBoundsToFire();
            fire.setxImage(bounds.getX());
            fire.setyImage(bounds.getY());
            fire.setWidthImage(bounds.getWidth());
            fire.setHeightImage(bounds.getHeight());

            System.out.println("Initial fire started at: (" + bounds.getX() + ", " + bounds.getY() + ")");

            // Crear incidente inicial
            Platform.runLater(() -> {
                try {
                    Incident incident = new Incident(Priority.HIGH, TypeIncident.FIRE, LocalDateTime.now());
                    incidentTree.insert(incident);
                    System.out.println("Initial fire incident created: " + incident);

                    // Create a Help button for the fire if CarController is available
                    createHelpButtonForFire(incident);
                } catch (Exception e) {
                    System.err.println("Error creating initial fire incident: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Error starting fire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void propagateFire() {
        try {
            Rectangle bounds = checkBoundsToFire();
            Fire newFire = new Fire("Fire" + (fires.size() + 1));
            newFire.setxImage(bounds.getX());
            newFire.setyImage(bounds.getY());
            newFire.setWidthImage(bounds.getWidth());
            newFire.setHeightImage(bounds.getHeight());

            fires.add(newFire);
            threadSafeFires.add(newFire);

            System.out.println("New fire created at: (" + bounds.getX() + ", " + bounds.getY() + "). Total fires: " + fires.size());

            // Crear incidente en el hilo principal de JavaFX
            Platform.runLater(() -> {
                try {
                    Incident incident = new Incident(Priority.HIGH, TypeIncident.FIRE, LocalDateTime.now());
                    incidentTree.insert(incident);
                    System.out.println("Fire incident created: " + incident);

                    // Create a Help button for the fire if CarController is available
                    createHelpButtonForFire(incident);
                } catch (Exception e) {
                    System.err.println("Error creating fire incident: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Error propagating fire: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Rectangle checkBoundsToFire() {
        if (useIntersections && intersections != null && !intersections.isEmpty()) {
            // Use intersections - select a random intersection's topright node
            int number = random.nextInt(intersections.size());
            Intersection randomIntersection = intersections.get(number);

            // Get the topright node of the intersection
            NodeGraph<Intersection> topRightNode = randomIntersection.getTopRight();

            if (topRightNode != null) {
                // Get the coordinates of the topright node
                double x = topRightNode.getX();
                double y = topRightNode.getY();

                // Add offsets to position the fire on a building
                // These offsets place the fire slightly to the right and up from the intersection
                double xOffset = 20 ; // Random offset between 20-50 pixels
                double yOffset = -70; // Random negative offset between -20 to -50 pixels

                double xFire = x + xOffset;
                double yFire = y + yOffset;
                double widthFire = 40;
                double heightFire = 40;

                System.out.println("Placing fire at topright node of intersection " + 
                                  randomIntersection.getName() + " at (" + xFire + ", " + yFire + ")");

                return new Rectangle(xFire, yFire, widthFire, heightFire);
            }
        }

        // Default values if neither intersections nor buildings are available
        return new Rectangle(100, 100, 40, 40);
    }

    public List<Fire> getFires() {
        return new ArrayList<>(threadSafeFires);
    }

    public void stop() {
        System.out.println("Stopping FireThread...");
        running = false;
    }

    /**
     * Creates a Help button for a fire incident if CarController is available
     * @param incident The fire incident
     */
    private void createHelpButtonForFire(Incident incident) {
        if (carController == null) {
            System.out.println("CarController not available, cannot create Help button for fire");
            return;
        }

        // Get the last fire's position
        if (fires.isEmpty()) {
            System.out.println("No fires available to create Help button");
            return;
        }

        Fire lastFire = fires.get(fires.size() - 1);
        double fireX = lastFire.getxImage();
        double fireY = lastFire.getyImage();

        // Find the closest intersection node to the fire
        if (useIntersections && intersections != null && !intersections.isEmpty()) {
            Intersection closestIntersection = null;
            NodeGraph<Intersection> closestNode = null;
            double minDistance = Double.MAX_VALUE;

            // Find the closest intersection to the fire
            for (Intersection intersection : intersections) {
                NodeGraph<Intersection> topRightNode = intersection.getTopRight();
                if (topRightNode != null) {
                    double dx = topRightNode.getX() - fireX;
                    double dy = topRightNode.getY() - fireY;
                    double distance = Math.sqrt(dx*dx + dy*dy);

                    if (distance < minDistance) {
                        minDistance = distance;
                        closestIntersection = intersection;
                        closestNode = topRightNode;
                    }
                }
            }

            // Create a Help button at the closest intersection's topright node
            if (closestNode != null) {
                carController.createFireHelpButton(closestNode, incident);
                System.out.println("Created Help button for fire at closest intersection " + closestIntersection.getName() + 
                                  " (distance: " + minDistance + ")");
            } else {
                System.out.println("No suitable intersection found for fire Help button");
            }
        }
    }
}
