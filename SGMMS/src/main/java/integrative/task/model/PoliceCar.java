package integrative.task.model;

import integrative.task.algorithms.BFS;
import integrative.task.algorithms.Dijkstra;
import integrative.task.structures.Graph;
import integrative.task.structures.NodeGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PoliceCar implements Runnable {
    private final AutomataPoliceCar automataPoliceCar;
    private final Graph<Intersection> cityGraph;
    private NodeGraph<Intersection> currentNodeGraph;
    private NodeGraph<Intersection> destinationNodeGraph;
    private final Set<NodeGraph<Intersection>> visitedNodeGraphs;
    private boolean running;
    private boolean isMoving; // Flag to track if the car is currently moving
    private boolean hasDestination; // Flag to track if the car has a destination
    private static final long MOVE_DELAY = 500; // milliseconds between moves
    private final String name; // Car name for identification
    private final String color = "blue"; // Police car color for console visualization

    // Algorithm selection flag (true = BFS, false = Dijkstra)
    public static boolean useBFS = false; // Use BFS algorithm to respect node directions

    // Collision information
    private Car collidedCar1;
    private Car collidedCar2;
    private long arrivalTime = 0; // Time when police car arrived at collision

    public PoliceCar(AutomataPoliceCar automataPoliceCar, Graph<Intersection> cityGraph, 
                    Intersection startIntersection, String name) {
        this.automataPoliceCar = automataPoliceCar;
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
                automataPoliceCar.setPosition(currentNodeGraph.getX(), currentNodeGraph.getY());
                System.out.println(getColoredText("Police car " + name + " stationed at " + startIntersection.getName()));
            } else {
                // Fallback to getting node from graph if topLeft is null
                this.currentNodeGraph = cityGraph.getNode(startIntersection);
                if (this.currentNodeGraph != null) {
                    automataPoliceCar.setPosition(currentNodeGraph.getX(), currentNodeGraph.getY());
                    System.out.println(getColoredText("Police car " + name + " stationed at " + startIntersection.getName() + " (fallback)"));
                }
            }
        }
    }

    @Override
    public void run() {
        System.out.println(getColoredText("Police car " + name + " thread started"));
        System.out.println("[DEBUG] Police car " + name + " thread started");
        System.out.println("[DEBUG] Police car " + name + " automataPoliceCar: " + automataPoliceCar);
        System.out.println("[DEBUG] Police car " + name + " currentNodeGraph: " + currentNodeGraph);
        System.out.println("[DEBUG] Police car " + name + " hasDestination: " + hasDestination);
        System.out.println("[DEBUG] Police car " + name + " destinationNodeGraph: " + destinationNodeGraph);

        while (running && currentNodeGraph != null) {
            try {
                if (hasDestination && destinationNodeGraph != null) {
                    // Debug output for destination
                    System.out.println(getColoredText("Police car " + name + 
                                      " has destination: " + destinationNodeGraph.getData().getName() +
                                      " at position (" + destinationNodeGraph.getX() + ", " + destinationNodeGraph.getY() + ")"));

                    // Choose algorithm based on flag
                    List<NodeGraph<Intersection>> path;
                    if (useBFS) {
                        // Use the class BFS existente
                        System.out.println(getColoredText("Police car " + name + " using BFS algorithm"));

                        // Obtener el camino usando BFS
                        List<NodeGraph<Intersection>> visited = BFS.traverse(cityGraph, currentNodeGraph.getData());

                        // Construir el camino desde los nodos visitados
                        path = new ArrayList<>();

                        // Añadir nodos al camino hasta encontrar el destino
                        for (NodeGraph<Intersection> nodeGraph : visited) {
                            path.add(nodeGraph);
                            if (nodeGraph.equals(destinationNodeGraph)) {
                                break;
                            }
                        }

                        // Si el destino no está en el camino, buscar el nodo más cercano
                        if (!path.contains(destinationNodeGraph)) {
                            System.out.println(getColoredText("Destination not found in BFS path, finding closest node"));
                            // Código para encontrar el nodo más cercano (mantener el código existente)
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
                        System.out.println(getColoredText("Police car " + name + " using Dijkstra algorithm"));
                        path = Dijkstra.findShortestPath(cityGraph, currentNodeGraph, destinationNodeGraph);

                        // Debug output for path
                        System.out.println(getColoredText("Dijkstra path has " + path.size() + " nodes"));
                    }

                    if (!path.isEmpty() && path.size() > 1) {
                        // Get the next node in the path (index 1 because index 0 is the current node)
                        NodeGraph<Intersection> nextNodeGraph = path.get(1);

                        // Determine direction to move
                        String direction = determineDirection(currentNodeGraph, nextNodeGraph);

                        // Print police car's current position in the console with color
                        System.out.println(getColoredText("Police car " + name + 
                                          " moving from " + currentNodeGraph.getData().getName() +
                                          " to " + nextNodeGraph.getData().getName() +
                                          " in direction " + direction));

                        // Use a final reference for the lambda
                        final NodeGraph<Intersection> finalNextNodeGraph = nextNodeGraph;

                        // Set moving flag to true
                        isMoving = true;

                        // Move the police car with animation
                        automataPoliceCar.moveTo(nextNodeGraph.getX(), nextNodeGraph.getY(), direction, () -> {
                            System.out.println(getColoredText("Police car " + name + 
                                              " arrived at " + finalNextNodeGraph.getData().getName()));
                            // Set moving flag to false when animation completes
                            isMoving = false;
                        });

                        // Update current node
                        currentNodeGraph = nextNodeGraph;

                        // Check if we've reached the destination
                        if (currentNodeGraph.equals(destinationNodeGraph)) {
                            System.out.println(getColoredText("Police car " + name + 
                                              " reached collision location at " + destinationNodeGraph.getData().getName()));
                            hasDestination = false;
                            destinationNodeGraph = null;

                            // Record arrival time
                            arrivalTime = System.currentTimeMillis();
                        }
                    } else if (currentNodeGraph != null && destinationNodeGraph != null) {
                        // Fallback to step-by-step movement if BFS didn't find a path
                        System.out.println(getColoredText("Police car " + name + 
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

                                System.out.println(getColoredText("Police car " + name + 
                                                  " moving from " + currentNodeGraph.getData().getName() +
                                                  " to " + nextNodeGraph.getData().getName() +
                                                  " in direction " + bestDirection));

                                // Set moving flag to true
                                isMoving = true;

                                // Use a final reference for the lambda
                                final NodeGraph<Intersection> finalNextNodeGraph = nextNodeGraph;
                                final String finalDirection = bestDirection;

                                // Move to the next node
                                automataPoliceCar.moveTo(nextNodeGraph.getX(), nextNodeGraph.getY(), bestDirection, () -> {
                                    System.out.println(getColoredText("Police car " + name + 
                                                      " arrived at " + finalNextNodeGraph.getData().getName() +
                                                      " using direction " + finalDirection));
                                    // Set moving flag to false when animation completes
                                    isMoving = false;
                                });

                                // Update current node
                                currentNodeGraph = nextNodeGraph;

                                // Check if we've reached the destination
                                if (currentNodeGraph.equals(destinationNodeGraph)) {
                                    System.out.println(getColoredText("Police car " + name + 
                                                      " reached collision location at " + destinationNodeGraph.getData().getName()));
                                    hasDestination = false;
                                    destinationNodeGraph = null;

                                    // Record arrival time
                                    arrivalTime = System.currentTimeMillis();
                                }
                            } else {
                                // No valid direction found, stay in place
                                System.out.println(getColoredText("Police car " + name + 
                                                  " couldn't find a valid direction to move, staying in place"));
                                hasDestination = false;
                                destinationNodeGraph = null;
                            }
                        } else {
                            // No available directions, stay in place
                            System.out.println(getColoredText("Police car " + name + 
                                              " has no available directions to move, staying in place"));
                            hasDestination = false;
                            destinationNodeGraph = null;
                        }
                    } else {
                        // No path found or already at destination
                        hasDestination = false;
                        destinationNodeGraph = null;
                        System.out.println(getColoredText("Police car " + name + 
                                          " couldn't find path to destination or already at destination"));
                    }
                } else if (arrivalTime > 0) {
                    // Check if 3 seconds have passed since arrival
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - arrivalTime >= 3000) {
                        System.out.println(getColoredText("Police car " + name + 
                                          " has completed its mission and is being removed"));

                        // Reset arrival time
                        arrivalTime = 0;

                        // Signal that this police car is done and can be removed
                        running = false;
                    }
                } else {
                    // No destination, police car remains stationary
                    // System.out.println(getColoredText("Police car " + name + " waiting for collision..."));
                }

                // Wait until the animation is complete before starting the next move
                while (isMoving) {
                    Thread.sleep(50); // Short sleep to avoid busy waiting
                }

                // Additional delay between moves if needed
                Thread.sleep(MOVE_DELAY);

            } catch (InterruptedException e) {
                System.out.println(getColoredText("Police car " + name + " thread interrupted"));
                running = false;
            }
        }

        System.out.println(getColoredText("Police car " + name + " thread finished"));
    }

    private String getColoredText(String text) {
        return "\u001B[34m" + text + "\u001B[0m"; // Blue color for police car
    }

    private String determineDirection(NodeGraph<Intersection> source, NodeGraph<Intersection> target) {
        if (source == null || target == null) {
            return "right"; // Default direction
        }

        // Check all directions from source to find the one that leads to target
        for (String direction : cityGraph.getDirections(source)) {
            NodeGraph<Intersection> neighbor = cityGraph.getNeighbor(source, direction);
            if (neighbor != null && neighbor.equals(target)) {
                System.out.println(getColoredText("Police car " + name + " found direction: " + direction + 
                                  " from " + source.getData().getName() + 
                                  " to " + target.getData().getName()));
                return direction;
            }
        }

        // If we get here, there's no direct connection in the graph
        System.out.println(getColoredText("WARNING: Police car " + name + 
                          " could not find a direct connection from " + source.getData().getName() + 
                          " to " + target.getData().getName() + 
                          ". Using random direction from available options."));

        // Instead of using geometric calculation, select a random available direction
        Set<String> directions = cityGraph.getDirections(source);
        if (!directions.isEmpty()) {
            List<String> directionList = new ArrayList<>(directions);
            java.util.Random random = new java.util.Random();
            String randomDirection = directionList.get(random.nextInt(directionList.size()));
            System.out.println(getColoredText("Police car " + name + " selected random direction: " + randomDirection));
            return randomDirection;
        }

        // Last resort fallback if no directions are available
        return "right";
    }

    public void setDestination(NodeGraph<Intersection> destinationNodeGraph) {
        System.out.println("[DEBUG] Setting destination for police car: " + name);
        System.out.println("[DEBUG] Destination node: " + destinationNodeGraph);
        System.out.println("[DEBUG] Destination position: (" + destinationNodeGraph.getX() + ", " + destinationNodeGraph.getY() + ")");

        this.destinationNodeGraph = destinationNodeGraph;
        this.hasDestination = true;
        System.out.println(getColoredText("Police car " + name + 
                          " dispatched to " + destinationNodeGraph.getData().getName() +
                          " at position (" + destinationNodeGraph.getX() + ", " + destinationNodeGraph.getY() + ")" +
                          " using " + (useBFS ? "BFS" : "Dijkstra") + " algorithm"));

        // Print current position for debugging
        System.out.println(getColoredText("Police car " + name + 
                          " current position: (" + currentNodeGraph.getX() + ", " + currentNodeGraph.getY() + ")" +
                          " at intersection " + currentNodeGraph.getData().getName()));

        System.out.println("[DEBUG] Police car " + name + " hasDestination set to: " + hasDestination);
        System.out.println("[DEBUG] Police car " + name + " automataPoliceCar: " + automataPoliceCar);
        System.out.println("[DEBUG] Police car " + name + " automataPoliceCar position: (" + 
                          automataPoliceCar.getxImage() + ", " + automataPoliceCar.getyImage() + ")");
    }

    public void setCollidedCars(Car car1, Car car2) {
        this.collidedCar1 = car1;
        this.collidedCar2 = car2;
    }

    public Car[] getCollidedCars() {
        return new Car[] { collidedCar1, collidedCar2 };
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

    public AutomataPoliceCar getAutomataPoliceCar() {
        return automataPoliceCar;
    }
}
