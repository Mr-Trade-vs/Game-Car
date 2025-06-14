package integrative.task.model;

import integrative.task.algorithms.BFS;
import integrative.task.algorithms.Dijkstra;
import integrative.task.structures.Graph;
import integrative.task.structures.NodeGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Thread class for FireTruck emergency vehicles
 */
public class FireTruckThread implements Runnable {
    private final FireTruckCar fireTruckCar;
    private final Graph<Intersection> cityGraph;
    private NodeGraph<Intersection> currentNodeGraph;
    private NodeGraph<Intersection> destinationNodeGraph;
    private final Set<NodeGraph<Intersection>> visitedNodeGraphs;
    private boolean running;
    private boolean isMoving; // Flag to track if the car is currently moving
    private boolean hasDestination; // Flag to track if the car has a destination
    private boolean drawPath = true; // Flag to enable/disable path drawing
    private static final long MOVE_DELAY = 500; // milliseconds between moves
    private final String name; // Car name for identification
    private final String color = "red"; // Fire truck color for console visualization

    // Algorithm selection flag (shared with PoliceCar)
    private boolean useBFS = PoliceCar.useBFS;

    // Mission completion tracking
    private long arrivalTime = 0; // Time when fire truck arrived at fire

    public FireTruckThread(FireTruckCar fireTruckCar, Graph<Intersection> cityGraph, 
                    Intersection startIntersection, String name) {
        this.fireTruckCar = fireTruckCar;
        this.cityGraph = cityGraph;
        this.visitedNodeGraphs = new HashSet<>();
        this.running = true;
        this.isMoving = false;
        this.hasDestination = false;
        this.name = name;

        // Initialize starting position
        if (startIntersection != null) {
            this.currentNodeGraph = startIntersection.getTopLeft(); // Use topLeft node of intersection
            if (this.currentNodeGraph != null) {
                fireTruckCar.setPosition(currentNodeGraph.getX(), currentNodeGraph.getY());
                System.out.println(getColoredText("Fire truck " + name + " stationed at " + startIntersection.getName()));
            } else {
                // Fallback to getting node from graph if topLeft is null
                this.currentNodeGraph = cityGraph.getNode(startIntersection);
                if (this.currentNodeGraph != null) {
                    fireTruckCar.setPosition(currentNodeGraph.getX(), currentNodeGraph.getY());
                    System.out.println(getColoredText("Fire truck " + name + " stationed at " + startIntersection.getName() + " (fallback)"));
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println(getColoredText("Fire truck " + name + " thread started"));

        while (running && currentNodeGraph != null) {
            try {
                if (hasDestination && destinationNodeGraph != null) {
                    // Debug output for destination
                    System.out.println(getColoredText("Fire truck " + name + 
                                      " has destination: " + destinationNodeGraph.getData().getName() +
                                      " at position (" + destinationNodeGraph.getX() + ", " + destinationNodeGraph.getY() + ")"));

                    // Choose algorithm based on flag
                    List<NodeGraph<Intersection>> path;
                    if (useBFS) {
                        // Use BFS algorithm
                        System.out.println(getColoredText("Fire truck " + name + " using BFS algorithm"));

                        // Get path using BFS
                        List<NodeGraph<Intersection>> visited = BFS.traverse(cityGraph, currentNodeGraph.getData());

                        // Build path from visited nodes
                        path = new ArrayList<>();

                        // Add nodes to path until destination is found
                        for (NodeGraph<Intersection> nodeGraph : visited) {
                            path.add(nodeGraph);
                            if (nodeGraph.equals(destinationNodeGraph)) {
                                break;
                            }
                        }

                        // If destination is not in path, find closest node
                        if (!path.contains(destinationNodeGraph)) {
                            System.out.println(getColoredText("Destination not found in BFS path, finding closest node"));
                            NodeGraph<Intersection> closestNodeGraph = null;
                            double minDistance = Double.MAX_VALUE;

                            for (NodeGraph<Intersection> nodeGraph : visited) {
                                // Calculate distance to destination
                                double dx = nodeGraph.getX() - destinationNodeGraph.getX();
                                double dy = nodeGraph.getY() - destinationNodeGraph.getY();
                                double distance = Math.sqrt(dx*dx + dy*dy);

                                // If this is the closest node so far, remember it
                                if (distance < minDistance) {
                                    minDistance = distance;
                                    closestNodeGraph = nodeGraph;
                                }
                            }

                            if (closestNodeGraph != null && !path.contains(closestNodeGraph)) {
                                path.add(closestNodeGraph);
                            }
                        }

                        System.out.println(getColoredText("Path constructed with " + path.size() + " nodes"));
                    } else {
                        // Use Dijkstra's algorithm
                        System.out.println(getColoredText("Fire truck " + name + " using Dijkstra algorithm"));
                        path = Dijkstra.findShortestPath(cityGraph, currentNodeGraph, destinationNodeGraph);

                        // Debug output for path
                        System.out.println(getColoredText("Dijkstra path has " + path.size() + " nodes"));
                    }

                    if (!path.isEmpty() && path.size() > 1) {
                        // Get the next node in the path (index 1 because index 0 is the current node)
                        NodeGraph<Intersection> nextNodeGraph = path.get(1);

                        // Determine direction to move
                        String direction = determineDirection(currentNodeGraph, nextNodeGraph);

                        // Print fire truck's current position in the console with color
                        System.out.println(getColoredText("Fire truck " + name + 
                                          " moving from " + currentNodeGraph.getData().getName() +
                                          " to " + nextNodeGraph.getData().getName() +
                                          " in direction " + direction));

                        // Use a final reference for the lambda
                        final NodeGraph<Intersection> finalNextNodeGraph = nextNodeGraph;

                        // Set moving flag to true
                        isMoving = true;

                        // Move the fire truck with animation
                        fireTruckCar.moveTo(nextNodeGraph.getX(), nextNodeGraph.getY(), direction, () -> {
                            System.out.println(getColoredText("Fire truck " + name + 
                                              " arrived at " + finalNextNodeGraph.getData().getName()));
                            // Set moving flag to false when animation completes
                            isMoving = false;
                        });

                        // Update current node
                        currentNodeGraph = nextNodeGraph;

                        // Check if we've reached the destination
                        if (currentNodeGraph.equals(destinationNodeGraph)) {
                            System.out.println(getColoredText("Fire truck " + name + 
                                              " reached fire location at " + destinationNodeGraph.getData().getName()));
                            hasDestination = false;
                            destinationNodeGraph = null;

                            // Record arrival time
                            arrivalTime = System.currentTimeMillis();
                        }
                    } else if (currentNodeGraph != null && destinationNodeGraph != null) {
                        // Fallback to step-by-step movement if BFS didn't find a path
                        System.out.println(getColoredText("Fire truck " + name + 
                                          " using step-by-step movement to destination"));

                        // Get available directions from current node
                        Set<String> availableDirections = cityGraph.getDirections(currentNodeGraph);
                        if (!availableDirections.isEmpty()) {
                            // Choose a direction that gets us closer to the destination
                            String bestDirection = null;
                            double minDistance = Double.MAX_VALUE;

                            for (String direction : availableDirections) {
                                NodeGraph<Intersection> neighbor = cityGraph.getNeighbor(currentNodeGraph, direction);
                                if (neighbor != null) {
                                    // Calculate distance to destination
                                    double dx = neighbor.getX() - destinationNodeGraph.getX();
                                    double dy = neighbor.getY() - destinationNodeGraph.getY();
                                    double distance = Math.sqrt(dx*dx + dy*dy);

                                    if (distance < minDistance) {
                                        minDistance = distance;
                                        bestDirection = direction;
                                    }
                                }
                            }

                            if (bestDirection != null) {
                                // Get the next node in the chosen direction
                                NodeGraph<Intersection> nextNodeGraph = cityGraph.getNeighbor(currentNodeGraph, bestDirection);

                                System.out.println(getColoredText("Fire truck " + name + 
                                                  " moving from " + currentNodeGraph.getData().getName() +
                                                  " to " + nextNodeGraph.getData().getName() +
                                                  " in direction " + bestDirection));

                                // Set moving flag to true
                                isMoving = true;

                                // Use a final reference for the lambda
                                final NodeGraph<Intersection> finalNextNodeGraph = nextNodeGraph;
                                final String finalDirection = bestDirection;

                                // Move to the next node
                                fireTruckCar.moveTo(nextNodeGraph.getX(), nextNodeGraph.getY(), bestDirection, () -> {
                                    System.out.println(getColoredText("Fire truck " + name + 
                                                      " arrived at " + finalNextNodeGraph.getData().getName() +
                                                      " using direction " + finalDirection));
                                    // Set moving flag to false when animation completes
                                    isMoving = false;
                                });

                                // Update current node
                                currentNodeGraph = nextNodeGraph;

                                // Check if we've reached the destination
                                if (currentNodeGraph.equals(destinationNodeGraph)) {
                                    System.out.println(getColoredText("Fire truck " + name + 
                                                      " reached fire location at " + destinationNodeGraph.getData().getName()));
                                    hasDestination = false;
                                    destinationNodeGraph = null;

                                    // Record arrival time
                                    arrivalTime = System.currentTimeMillis();
                                }
                            } else {
                                // No valid direction found, stay in place
                                System.out.println(getColoredText("Fire truck " + name + 
                                                  " couldn't find a valid direction to move, staying in place"));
                                hasDestination = false;
                                destinationNodeGraph = null;
                            }
                        } else {
                            // No available directions, stay in place
                            System.out.println(getColoredText("Fire truck " + name + 
                                              " has no available directions to move, staying in place"));
                            hasDestination = false;
                            destinationNodeGraph = null;
                        }
                    } else {
                        // No path found or already at destination
                        hasDestination = false;
                        destinationNodeGraph = null;
                        System.out.println(getColoredText("Fire truck " + name + 
                                          " couldn't find path to destination or already at destination"));
                    }
                } else if (arrivalTime > 0) {
                    // Check if 2 seconds have passed since arrival
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - arrivalTime >= 2000) {
                        System.out.println(getColoredText("Fire truck " + name + 
                                          " has completed its mission and is being removed"));

                        // Reset arrival time
                        arrivalTime = 0;

                        // Signal that this fire truck is done and can be removed
                        running = false;
                    }
                } else {
                    // No destination, fire truck remains stationary
                    // System.out.println(getColoredText("Fire truck " + name + " waiting for fire..."));
                }

                // Wait until the animation is complete before starting the next move
                while (isMoving) {
                    Thread.sleep(50); // Short sleep to avoid busy waiting
                }

                // Additional delay between moves if needed
                Thread.sleep(MOVE_DELAY);

            } catch (InterruptedException e) {
                System.out.println(getColoredText("Fire truck " + name + " thread interrupted"));
                running = false;
            }
        }

        System.out.println(getColoredText("Fire truck " + name + " thread finished"));
    }

    private String getColoredText(String text) {
        return "\u001B[31m" + text + "\u001B[0m"; // Red color for fire truck
    }

    private String determineDirection(NodeGraph<Intersection> source, NodeGraph<Intersection> target) {
        if (source == null || target == null) {
            return "right"; // Default direction
        }

        // Check all directions from source to find the one that leads to target
        for (String direction : cityGraph.getDirections(source)) {
            NodeGraph<Intersection> neighbor = cityGraph.getNeighbor(source, direction);
            if (neighbor != null && neighbor.equals(target)) {
                System.out.println(getColoredText("Fire truck " + name + " found direction: " + direction + 
                                  " from " + source.getData().getName() + 
                                  " to " + target.getData().getName()));
                return direction;
            }
        }

        // If we get here, there's no direct connection in the graph
        System.out.println(getColoredText("WARNING: Fire truck " + name + 
                          " could not find a direct connection from " + source.getData().getName() + 
                          " to " + target.getData().getName() + 
                          ". Using random direction from available options."));

        // Instead of using geometric calculation, select a random available direction
        Set<String> directions = cityGraph.getDirections(source);
        if (!directions.isEmpty()) {
            List<String> directionList = new ArrayList<>(directions);
            java.util.Random random = new java.util.Random();
            String randomDirection = directionList.get(random.nextInt(directionList.size()));
            System.out.println(getColoredText("Fire truck " + name + " selected random direction: " + randomDirection));
            return randomDirection;
        }

        // Last resort fallback if no directions are available
        return "right";
    }

    public void setDestination(NodeGraph<Intersection> destinationNodeGraph) {
        this.destinationNodeGraph = destinationNodeGraph;
        this.hasDestination = true;
        System.out.println(getColoredText("Fire truck " + name + 
                          " dispatched to " + destinationNodeGraph.getData().getName() +
                          " at position (" + destinationNodeGraph.getX() + ", " + destinationNodeGraph.getY() + ")" +
                          " using " + (useBFS ? "BFS" : "Dijkstra") + " algorithm"));

        // Print current position for debugging
        System.out.println(getColoredText("Fire truck " + name + 
                          " current position: (" + currentNodeGraph.getX() + ", " + currentNodeGraph.getY() + ")" +
                          " at intersection " + currentNodeGraph.getData().getName()));
    }

    public boolean hasMissionCompleted() {
        return arrivalTime > 0 && System.currentTimeMillis() - arrivalTime >= 3000;
    }

    public void stop() {
        running = false;
    }

    public NodeGraph<Intersection> getCurrentNode() {
        return currentNodeGraph;
    }

    public String getName() {
        return name;
    }

    public FireTruckCar getFireTruckCar() {
        return fireTruckCar;
    }

    public List<NodeGraph<Intersection>> getCurrentPath() {
        if (!hasDestination || destinationNodeGraph == null || currentNodeGraph == null) {
            return new ArrayList<>();
        }

        // Get the path using the current algorithm
        if (useBFS) {
            // Use BFS algorithm
            List<NodeGraph<Intersection>> visited = BFS.traverse(cityGraph, currentNodeGraph.getData());

            // Build path from visited nodes
            List<NodeGraph<Intersection>> path = new ArrayList<>();

            // Add nodes to path until destination is found
            for (NodeGraph<Intersection> nodeGraph : visited) {
                path.add(nodeGraph);
                if (nodeGraph.equals(destinationNodeGraph)) {
                    break;
                }
            }

            return path;
        } else {
            // Use Dijkstra's algorithm
            return Dijkstra.findShortestPath(cityGraph, currentNodeGraph, destinationNodeGraph);
        }
    }

    public boolean isDrawPath() {
        return drawPath;
    }
}
