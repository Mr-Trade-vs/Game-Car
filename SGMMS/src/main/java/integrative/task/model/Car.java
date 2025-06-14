package integrative.task.model;

import integrative.task.structures.Graph;
import integrative.task.structures.NodeGraph;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


public class Car implements Runnable {
    private final AutomataCar automataCar;
    private final Graph<Intersection> cityGraph;
    private NodeGraph<Intersection> currentNodeGraph;
    private final Set<NodeGraph<Intersection>> visitedNodeGraphs;
    private boolean running;
    private boolean collided;
    private boolean isMoving; // Flag to track if the car is currently moving
    private static final long MOVE_DELAY = 500; // milliseconds between moves
    private final String name; // Car name for identification
    private final String color; // Car color for console visualization

    public Car(AutomataCar automataCar, Graph<Intersection> cityGraph, 
               Intersection startIntersection, String name, String color) {
        this.automataCar = automataCar;
        this.cityGraph = cityGraph;
        this.visitedNodeGraphs = new HashSet<>();
        this.running = true;
        this.collided = false;
        this.isMoving = false;
        this.name = name;
        this.color = color;

        // Initialize starting position
        if (startIntersection != null) {
            this.currentNodeGraph = startIntersection.getTopLeft(); // Use topLeft node of intersection
            if (this.currentNodeGraph != null) {
                automataCar.setPosition(currentNodeGraph.getX(), currentNodeGraph.getY());
                System.out.println(getColoredText("Car " + name + " starting at " + startIntersection.getName()));
            } else {
                // Fallback to getting node from graph if topLeft is null
                this.currentNodeGraph = cityGraph.getNode(startIntersection);
                if (this.currentNodeGraph != null) {
                    automataCar.setPosition(currentNodeGraph.getX(), currentNodeGraph.getY());
                    System.out.println(getColoredText("Car " + name + " starting at " + startIntersection.getName() + " (fallback)"));
                }
            }
        }

        // No destination is set for random movement
    }

    @Override
    public void run() {
        System.out.println(getColoredText("Car " + name + " thread started"));

        while (running && currentNodeGraph != null && !collided) {
            try {
                // Add current node to visited
                visitedNodeGraphs.add(currentNodeGraph);

                // Get next node by randomly selecting a neighbor
                NodeGraph<Intersection> nextNodeGraph = getRandomNeighbor(currentNodeGraph);

                if (nextNodeGraph != null) {
                    // Determine direction to move
                    String direction = determineDirection(currentNodeGraph, nextNodeGraph);

                    // Print car's current position in the console with color
                    System.out.println(getColoredText("Car " + name + 
                                      " moving from " + currentNodeGraph.getData().getName() +
                                      " to " + nextNodeGraph.getData().getName() +
                                      " in direction " + direction));

                    // Use a final reference for the lambda
                    final NodeGraph<Intersection> finalNextNodeGraph = nextNodeGraph;

                    // Set moving flag to true
                    isMoving = true;

                    // Move the car with animation
                    automataCar.moveTo(nextNodeGraph.getX(), nextNodeGraph.getY(), direction, () -> {
                        System.out.println(getColoredText("Car " + name + 
                                          " arrived at " + finalNextNodeGraph.getData().getName()));
                        // Set moving flag to false when animation completes
                        isMoving = false;
                    });

                    // Update current node
                    currentNodeGraph = nextNodeGraph;

                    // No destination check for random movement
                } else {
                    // No unvisited neighbors, reset visited nodes except current
                    System.out.println(getColoredText("Car " + name + 
                                      " has no unvisited neighbors, resetting exploration"));
                    visitedNodeGraphs.clear();
                    visitedNodeGraphs.add(currentNodeGraph);
                }

                // Wait until the animation is complete before starting the next move
                while (isMoving) {
                    Thread.sleep(50); // Short sleep to avoid busy waiting
                }

                // Additional delay between moves if needed
                Thread.sleep(MOVE_DELAY);

            } catch (InterruptedException e) {
                System.out.println(getColoredText("Car " + name + " thread interrupted"));
                running = false;
            }
        }

        if (collided) {
            System.out.println(getColoredText("Car " + name + " stopped due to collision"));
        } else {
            System.out.println(getColoredText("Car " + name + " thread finished"));
        }
    }

    private String getColoredText(String text) {
        String ansiColor;
        switch (color.toLowerCase()) {
            case "red":
                ansiColor = "\u001B[31m";
                break;
            case "green":
                ansiColor = "\u001B[32m";
                break;
            case "yellow":
                ansiColor = "\u001B[33m";
                break;
            case "blue":
                ansiColor = "\u001B[34m";
                break;
            case "purple":
                ansiColor = "\u001B[35m";
                break;
            case "cyan":
                ansiColor = "\u001B[36m";
                break;
            default:
                ansiColor = "\u001B[37m"; // White
        }
        return ansiColor + text + "\u001B[0m"; // Reset color at the end
    }

    private NodeGraph<Intersection> getRandomNeighbor(NodeGraph<Intersection> nodeGraph) {
        if (nodeGraph == null) {
            return null;
        }

        // Get all neighbors of the node
        Map<String, NodeGraph<Intersection>> neighbors = cityGraph.getNeighbors(nodeGraph);

        if (neighbors.isEmpty()) {
            return null;
        }

        // Convert the values to a list for random selection
        List<NodeGraph<Intersection>> neighborNodeGraphs = new java.util.ArrayList<>(neighbors.values());

        // Select a random neighbor
        Random random = new Random();
        int randomIndex = random.nextInt(neighborNodeGraphs.size());

        return neighborNodeGraphs.get(randomIndex);
    }

    private String determineDirection(NodeGraph<Intersection> source, NodeGraph<Intersection> target) {
        if (source == null || target == null) {
            return "right"; // Default direction
        }

        // Check all directions from source to find the one that leads to target
        for (String direction : cityGraph.getDirections(source)) {
            NodeGraph<Intersection> neighbor = cityGraph.getNeighbor(source, direction);
            if (neighbor != null && neighbor.equals(target)) {
                return direction;
            }
        }

        // Fallback to geometric direction if not found in neighbors
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();

        if (Math.abs(dx) > Math.abs(dy)) {
            return dx > 0 ? "right" : "left";
        } else {
            return dy > 0 ? "down" : "up";
        }
    }

    public void stop() {
        running = false;
    }

    public void markAsCollided(Car otherCar) {
        if (!collided) {
            collided = true;
            System.out.println(getColoredText("COLISIÓN entre " + name + " y " + otherCar.getName() + 
                              " en el nodo " + currentNodeGraph.getData().getName()));
        }
    }

    public boolean hasCollided() {
        return collided;
    }

    public NodeGraph<Intersection> getCurrentNode() {
        return currentNodeGraph;
    }

    public String getName() {
        return name;
    }

    public AutomataCar getAutomataCar() {
        return automataCar;
    }
}
