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

public class BombThread implements Runnable {
    private Bomb bomb;
    private List<Intersection> intersections;
    private BinaryTreeIncident incidentTree;
    private List<Bomb> bombs;
    private volatile boolean running;
    private Random random;
    private long lastBombCreationTime;
    private CarController carController; // Reference to the CarController for creating Help buttons

    // Thread-safe list of bombs
    private final List<Bomb> threadSafeBombs;


    public BombThread(Bomb bomb, List<Intersection> intersections, BinaryTreeIncident incidentTree, CarController carController) {
        this.bomb = bomb;
        this.intersections = intersections;
        this.incidentTree = incidentTree;
        this.carController = carController;
        this.threadSafeBombs = new CopyOnWriteArrayList<>();
        this.bombs = Collections.synchronizedList(new ArrayList<>());
        this.bombs.add(bomb);
        this.threadSafeBombs.add(bomb);
        this.running = true;
        this.random = new Random();
        this.lastBombCreationTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        System.out.println("BombThread started");
        placeBomb();

        while (running) {
            try {
                long currentTime = System.currentTimeMillis();

                // Create a new bomb every 20 seconds
                if (currentTime - lastBombCreationTime >= 20000) {
                    placeBomb();
                    lastBombCreationTime = currentTime;
                }

                // Sleep for better response
                Thread.sleep(100);

            } catch (InterruptedException e) {
                System.out.println("BombThread interrupted");
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Error in BombThread: " + e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("BombThread stopped");
    }

    public void placeBomb() {
        try {
            Rectangle bounds = findRandomLocation();
            bomb.setxImage(bounds.getX());
            bomb.setyImage(bounds.getY());
            bomb.setWidthImage(bounds.getWidth());
            bomb.setHeightImage(bounds.getHeight());

            System.out.println("Bomb placed at: (" + bounds.getX() + ", " + bounds.getY() + ")");

            // Create incident in the JavaFX thread
            Platform.runLater(() -> {
                try {
                    Incident incident = new Incident(Priority.HIGH, TypeIncident.BOMBS, LocalDateTime.now());
                    incidentTree.insert(incident);
                    System.out.println("Bomb incident created: " + incident);

                    // Create a Help button for the bomb if CarController is available
                    createHelpButtonForBomb(incident);
                } catch (Exception e) {
                    System.err.println("Error creating bomb incident: " + e.getMessage());
                }
            });

        } catch (Exception e) {
            System.err.println("Error placing bomb: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Rectangle findRandomLocation() {
        if (intersections != null && !intersections.isEmpty()) {
            // Select a random intersection
            int number = random.nextInt(intersections.size());
            Intersection randomIntersection = intersections.get(number);

            // Get a random node from the intersection (topLeft, topRight, bottomLeft, bottomRight)
            NodeGraph<Intersection> randomNode = null;
            int nodeChoice = random.nextInt(4);
            
            switch (nodeChoice) {
                case 0:
                    randomNode = randomIntersection.getTopLeft();
                    break;
                case 1:
                    randomNode = randomIntersection.getTopRight();
                    break;
                case 2:
                    randomNode = randomIntersection.getBottomLeft();
                    break;
                case 3:
                    randomNode = randomIntersection.getBottomRight();
                    break;
            }

            if (randomNode != null) {
                // Get the coordinates of the random node
                double x = randomNode.getX();
                double y = randomNode.getY();

                // Add small random offsets to make placement look more natural
                double xOffset = random.nextInt(20) - 10; // -10 to 10 pixels
                double yOffset = random.nextInt(20) - 10; // -10 to 10 pixels

                double xBomb = x + xOffset;
                double yBomb = y + yOffset;
                double widthBomb = 30;
                double heightBomb = 30;

                System.out.println("Placing bomb at node of intersection " + 
                                  randomIntersection.getName() + " at (" + xBomb + ", " + yBomb + ")");

                return new Rectangle(xBomb, yBomb, widthBomb, heightBomb);
            }
        }

        // Default values if intersections are not available
        return new Rectangle(100, 100, 30, 30);
    }

    private void createHelpButtonForBomb(Incident incident) {
        if (carController == null) {
            System.out.println("CarController not available, cannot create Help button for bomb");
            return;
        }

        // Get the last bomb's position
        if (bombs.isEmpty()) {
            System.out.println("No bombs available to create Help button");
            return;
        }

        Bomb lastBomb = bombs.get(bombs.size() - 1);
        double bombX = lastBomb.getxImage();
        double bombY = lastBomb.getyImage();

        // Find the closest intersection node to the bomb
        if (intersections != null && !intersections.isEmpty()) {
            Intersection closestIntersection = null;
            NodeGraph<Intersection> closestNode = null;
            double minDistance = Double.MAX_VALUE;

            // Find the closest intersection to the bomb
            for (Intersection intersection : intersections) {
                // Check all four nodes of the intersection
                NodeGraph<Intersection>[] nodes = new NodeGraph[]{
                    intersection.getTopLeft(),
                    intersection.getTopRight(),
                    intersection.getBottomLeft(),
                    intersection.getBottomRight()
                };
                
                for (NodeGraph<Intersection> node : nodes) {
                    if (node != null) {
                        double dx = node.getX() - bombX;
                        double dy = node.getY() - bombY;
                        double distance = Math.sqrt(dx*dx + dy*dy);

                        if (distance < minDistance) {
                            minDistance = distance;
                            closestIntersection = intersection;
                            closestNode = node;
                        }
                    }
                }
            }

            // Create a Help button at the closest intersection node
            if (closestNode != null) {
                carController.createBombHelpButton(closestNode, incident);
                System.out.println("Created Help button for bomb at closest intersection " + closestIntersection.getName() + 
                                  " (distance: " + minDistance + ")");
            } else {
                System.out.println("No suitable intersection found for bomb Help button");
            }
        }
    }

    public List<Bomb> getBombs() {
        return new ArrayList<>(threadSafeBombs);
    }

    public void stop() {
        System.out.println("Stopping BombThread...");
        running = false;
    }
}